package settle

import boss.BoCustomerService
import ismp.CmCustomer
import javax.jms.Queue
import javax.jms.QueueSender

class MessageService {

  static transactional = false
  static exposes = ["jms"]
  static destination = "settle"

  def accountClientService

  def liquidateService

  def onMessage(msg) {

    log.info 'info new msg' + msg.dump()

    //参数校验
    if (!msg.srvCode || !msg.tradeCode || !msg.customerNo || !msg.amount instanceof Number || !msg.seqNo || !msg.tradeDate
        || !msg.billDate) {
      log.warn('msg params error! ')
      return
    }
    //交易查重
    def preTrade = FtTrade.countBySrvCodeAndSeqNo(msg.srvCode, msg.seqNo)
    if (preTrade > 0) {
      log.warn('msg duplicated, ignore')
      return
    }
    //创建交易日志记录
    def trade = new FtTrade()
    trade.srvCode = msg.srvCode
    trade.tradeCode = msg.tradeCode
    trade.customerNo = msg.customerNo
    trade.amount = msg.amount
    trade.seqNo = msg.seqNo
    trade.channelCode = msg.channel
    try {
      trade.tradeDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', msg.tradeDate)
      trade.billDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', msg.billDate)
    } catch (Exception e) {
      log.warn('msg parse date error! ')
      return
    }
    //查询业务类型信息，交易类型和结算设置
    def srvType = FtSrvType.findBySrvCode(msg.srvCode)
    //业务类型不匹配不进行处理
    if (!srvType) {
      log.warn('srvType not found! ')
      return
    }
    //查询交易类型，不匹配不进行处理
    def tradeType = FtSrvTradeType.findBySrvAndTradeCode(srvType, msg.tradeCode)
    if (!tradeType) {
      log.warn('tradeType not found! ')
      return
    }
    //查询客户信息
    def customer = CmCustomer.findByCustomerNo(msg.customerNo)
    if (!customer) {
      log.warn('customer not found! ')
      return
    }

    //查询系统手续费账户
    def sysFeeAcc = boss.BoInnerAccount.findByKey('feeAcc').accountNo

    //查询结算设置
    def footSetting = FtSrvFootSetting.findWhere([srv: srvType, tradeType: tradeType, customerNo: msg.customerNo])
    //如果结算设置不存在或者非实时结算，保存交易日志，设置为非实时结算交易
    if (!footSetting || footSetting.footType != 0) {
      log.warn("footSetting:${footSetting} , footType:${footSetting?.footType}, save only")
      trade.realtimeSettle = 0
      try {
        trade.save(failOnError: true)
      } catch (Exception e) {
        log.warn('save trade false', e)
      }
      return
    } else {
      //实时结算
      trade.realtimeSettle = 1
      trade.liqDate = new Date()

      //计算交易净额
      def netAmount = msg.amount * tradeType.netWeight
      trade.netAmount = netAmount

      //查询手续费设置
      def feeSetting

      //guonan update
        def billDate = Date.parse('yyyy-MM-dd', msg.billDate)
         def billDay = billDate.format("yyyy-MM-dd")
          feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customer.customerNo, msg.channel,billDate)
          if (!feeSetting) {
             feeSetting =liquidateService.getFeeSetting(srvType, tradeType, customer.customerNo,null,billDate)
          }

      // 不存在 无法实时结算
      if (!feeSetting) {
        log.warn('feeSetting is null, save only')
        try {
          trade.realtimeSettle = 0
          trade.liqDate = null
          trade.save(failOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
        return
      }
      // 计费模式为包年包月 无法实时结算
      if (feeSetting.feeModel == 1) {
        log.warn('feeModel is 1, save only')
        try {
          trade.realtimeSettle = 0
          trade.liqDate = null
          trade.save(failOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
        return
      }
      //guonan update channel
      trade.channelCodeCal = feeSetting.channelCode
      def service = BoCustomerService.findWhere([customerId: customer.id, serviceCode: msg.srvCode, isCurrent: true,enable: true])
      // 不存在 无法实时结算
      if (!service) {
        log.warn('service not found, save only')
        try {
          trade.realtimeSettle = 0
          trade.liqDate = null
          trade.save(failOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
        return
      }

      // 费率计算
         //guonan update


      def feeAmount = liquidateService.calcuFeeUpgrade(feeSetting,msg.channel, msg.srvCode, msg.tradeCode, Math.abs(msg.amount), 1,trade.billDate)

       if (feeAmount == null) {
        log.warn("feeAmount is null, step is not start, tradeTime=${trade.billDate} ,firstLiqDate=${feeSetting.firstLiqDate} ,save only")
        try {
          trade.realtimeSettle = 0
          trade.liqDate = null
          trade.save(failOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
        return
      }

      feeAmount = new BigDecimal(feeAmount.toString()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue()

      log.info "netAmount is ${netAmount}, fee is ${feeAmount}"
      //手续费转账
      def cmdList = null
      try {
//        def service = BoCustomerService.findByCustomerIdAndServiceCode(customer.id, msg.srvCode)

        //判断即收还是后返
        if (feeSetting.fetchType == 0) { //即收
          //设置交易手续费
          trade.preFee = feeAmount
          trade.postFee = 0
          trade.feeType = 0
          //把交易净额减去手续费的金额从客户服务账户转到客户现金帐户，如果为负则反向
          def settleAmount = netAmount - feeAmount
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, settleAmount, 'settle', msg.seqNo, '0', "实时结算交易净额")

          //把手续费从客户服务帐户扣除到系统手续费帐户，如果为负则反向转账
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, sysFeeAcc, feeAmount, 'fee', msg.seqNo, '0', "实时结算即扣手续费")

        } else { //后返
          //设置交易手续费
          trade.postFee = feeAmount
          trade.preFee=0
          trade.feeType = 1
          //把交易净额从客户服务账户转到客户现金帐户，如果为负则反向
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, netAmount, 'settle', msg.seqNo, '0', "实时结算交易净额")
          //把手续费从服务后返手续费帐户转到系统应收手续费帐户，如果手续费为负则反向
          //查询系统应收手续费账户
          def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo

          cmdList = accountClientService.buildTransfer(cmdList, service.feeAccNo, sysFeeAdvAcc, feeAmount, 'fee', msg.seqNo, '0', "实时结算后收手续费")
        }
      } catch (Exception e) {
        log.warn("gen cmdList false", e)
        return
      }
      trade.redo = false
      trade.save(failOnError:true)

      //转账
      boolean redo = false //转账失败，是否重做
      try {
        def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
        if (transResult.result != 'true') {
          log.warn("实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
          //帐户余额不足或者账务系统故障需要重新转账
          if (transResult.errorCode == '03' || transResult.errorCode == 'ff') {
            redo = true
          }
        }
      } catch (Exception e) {
        log.warn("balance trans faile,cmdList:${cmdList}", e)
        redo = true
      }

      if (redo) {
        trade.redo = true
        trade.save(failOnError:true)
      }

    }
    return
  }
}

