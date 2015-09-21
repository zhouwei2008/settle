package settle

import ismp.CmCustomer
import grails.converters.JSON
import boss.BoCustomerService

class RpcController {

  def accountClientService

  def liquidateService

  /**
   * 同步清结算调用，需要参数：{srvCode: '',tradeCode:'', customerNo:'', amount:amount, seqNo:'', tradeDate:'yyyy-MM-dd HH:mm:ss.SSS', billDate:'yyyy-MM-dd HH:mm:ss.SSS'} accountNo 账户账号,必须参数；
   * 返回 : {result: 'true or false', errorMsg: ''}* result: true为成功， false 为失败,
   * errorMsg: 当result为false时，返回误原因,
   */
  def trade = {

    def param = request.JSON

    log.info 'info new MSG' + param.dump()

    try {
      process(param)
      def result = [result: 'true']
      render result as JSON
      return
    } catch (Exception e) {
      log.warn('process trade error', e)
      def result = [result: 'false', errorMsg: e.getMessage()]
      render result as JSON
      return
    }
  }

  def process(param) throws Exception {

    //参数校验
    if (!param.srvCode || !param.tradeCode || !param.customerNo || !param.amount instanceof Number || !param.seqNo || !param.tradeDate
        || !param.billDate) {
      log.warn('MSG params error! ' + param.dump())
      return
    }
    //交易查重
    def preTrade = FtTrade.countBySrvCodeAndSeqNo(param.srvCode, param.seqNo)
    if (preTrade > 0) {
      log.warn('MSG duplicated, ignore')
      return
    }
    //创建交易日志记录
    def trade = new FtTrade()
    trade.srvCode = param.srvCode
    trade.tradeCode = param.tradeCode
    trade.customerNo = param.customerNo
    trade.amount = param.amount
    trade.seqNo = param.seqNo
    trade.channelCode = param.channel
    try {
      trade.tradeDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', param.tradeDate)
      trade.billDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', param.billDate)
    } catch (Exception e) {
      log.warn('MSG parse date error! ')
      return
    }
    //查询业务类型信息，交易类型和结算设置
    def srvType = FtSrvType.findBySrvCode(param.srvCode)
    //业务类型不匹配不进行处理
    if (!srvType) {
      log.warn('srvType not found! ')
      return
    }
    //查询交易类型，不匹配不进行处理
    def tradeType = FtSrvTradeType.findBySrvAndTradeCode(srvType, param.tradeCode)
    if (!tradeType) {
      log.warn('tradeType not found! ')
      return
    }
    //查询客户信息
    def customer = CmCustomer.findByCustomerNo(param.customerNo)
    if (!customer) {
      log.warn('customer not found! ')
      return
    }
    //查询服务信息
//    def service = BoCustomerService.findByCustomerIdAndServiceCode(customer.id, param.srvCode)
    def service = BoCustomerService.findWhere([customerId: customer.id, serviceCode: param.srvCode, isCurrent: true,enable: true])

    //查询系统手续费账户
    def sysFeeAcc = boss.BoInnerAccount.findByKey('feeAcc').accountNo

    //查询结算设置
    def footSetting = FtSrvFootSetting.findWhere([srv: srvType, tradeType: tradeType, customerNo: param.customerNo])

    //计算交易净额
    def netAmount = param.amount * tradeType.netWeight
    trade.netAmount = netAmount

    //如果结算设置不存在或者非实时结算，保存交易日志，设置为非实时结算交易
    if (!footSetting || footSetting.footType != 0) {
      log.warn("footSetting:${footSetting} , footType:${footSetting?.footType}, save only")
      trade.realtimeSettle = 0

      //如果为负净额交易，且服务帐户余额不足，从资金账户冻结差额，将冻结金额记在交易明细里，结算时候解冻
      if (netAmount < 0) {
        //查询服务帐户余额
        def srvAccInfo = accountClientService.queryAcc(service.srvAccNo)
        def srvAccBalance = srvAccInfo.balance
        //查询商户现金帐户余额
        def accInfo = accountClientService.queryAcc(customer.accountNo)
        def balance = accInfo.balance
        log.info("negative trade, netAmount:${netAmount},srvAccBalance:${srvAccBalance},balance:${balance}")
        //如果资金账户加服务帐户余额小于负交易净额，交易失败
        if ((srvAccBalance > 0 && srvAccBalance + balance + netAmount < 0) || (srvAccBalance <= 0 && balance + netAmount < 0)) {
          //资金帐户余额不足，结算失败
          log.warn("负净额交易，资金和服务账户余额不足，netAmount:${netAmount}, balance:${balance}, srvAccBalance:${srvAccBalance}")
          throw new Exception('负净额交易，资金和服务帐户余额不足')
        } else {
          def freezeAmount = 0
          if (srvAccBalance + netAmount >= 0) {
            //服务帐户余额足够，交易成功
            try {
              trade.redo = false
              trade.save(failOnError: true)
            } catch (Exception e) {
              log.warn('save trade false', e)
            }
            return
          } else {
            if (srvAccBalance > 0) {
              //服务帐户大于0，冻结服务帐户减去交易净额的资金
              freezeAmount = -(srvAccBalance + netAmount)
            } else {
              //服务帐户小于0，从资金账户冻结交易净额的资金
              freezeAmount = -netAmount
            }
          }
          trade.freezeAmount = freezeAmount

          //从资金账户里冻结金额
          def cmdList = accountClientService.buildFreeze(null, customer.accountNo, freezeAmount, 'frozen', param.seqNo, '0', "负净额交易冻结资金余额")
          log.info("freeze amount, cmdList:${cmdList}")
          //转账
          try {
            def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
            if (transResult.result == 'true') {
              trade.redo = false
              trade.save(failOnError: true)
            } else {
              log.warn("负净额交易冻结资金余额，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
              throw new Exception("负净额交易冻结资金余额，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
            }
          } catch (Exception e) {
            log.warn("balance trans faile,cmdList:${cmdList}", e)
            throw e
          }
        }
      } else {
        try {
          trade.redo = false
          trade.save(failOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
      }
      return
    } else {
      //实时结算
      trade.realtimeSettle = 1
      trade.liqDate = new Date()

      //查询手续费设置
      def feeSetting
         //guonan update
        def billDate = Date.parse('yyyy-MM-dd', param.billDate)
         def billDay = billDate.format("yyyy-MM-dd")
          feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customer.customerNo, param.channel,billDate)
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

      // 费率计算
      def feeAmount = liquidateService.calcuFeeUpgrade(feeSetting,param.channel, param.srvCode, param.tradeCode, Math.abs(param.amount), 1,trade.billDate)
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
      def settleAmount
      //手续费转账
      def cmdList = null

      //判断即收还是后返
      if (feeSetting.fetchType == 0) { //即收
        //设置交易手续费
        trade.preFee = feeAmount
        trade.postFee = 0
        trade.feeType = 0
        //把交易净额减去手续费的金额从客户服务账户转到客户现金帐户，如果为负则反向
        settleAmount = netAmount - feeAmount
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, settleAmount, 'settle', param.seqNo, '0', "实时结算交易净额")

        //把手续费从客户服务帐户扣除到系统手续费帐户，如果为负则反向转账
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, sysFeeAcc, feeAmount, 'fee', param.seqNo, '0', "实时结算即扣手续费")

      } else { //后返
        settleAmount = netAmount
        //设置交易手续费
        trade.postFee = feeAmount
        trade.preFee=0
        trade.feeType = 1
        //把交易净额从客户服务账户转到客户现金帐户，如果为负则反向
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, netAmount, 'settle', param.seqNo, '0', "实时结算交易净额")
        //把手续费从服务后返手续费帐户转到系统应收手续费帐户，如果手续费为负则反向
        //查询系统应收手续费账户
        def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo
        if (!service) {
          log.warn("service not found")
          return
        }
        cmdList = accountClientService.buildTransfer(cmdList, service.feeAccNo, sysFeeAdvAcc, feeAmount, 'fee', param.seqNo, '0', "实时结算后收手续费")
      }

      //判断负净额交易是否资金帐户余额足够
      if (settleAmount < 0) {
        //查询商户现金帐户余额
        def accInfo = accountClientService.queryAcc(customer.accountNo)
        def balance = accInfo.balance
        if (settleAmount + balance < 0) {
          //资金帐户余额不足，结算失败
          log.warn("资金账户余额不足，settleAmount:${settleAmount}, balance:${balance}")
          throw new Exception('资金帐户余额不足')
        }
      }

      //转账
      try {
        def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
        if (transResult.result == 'true') {
          trade.redo = false
          trade.save(failOnError: true)
        } else {
          log.warn("实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
          throw new Exception("实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
        }
      } catch (Exception e) {
        log.warn("balance trans faile,cmdList:${cmdList}", e)
        throw e
      }
    }
  }
}
