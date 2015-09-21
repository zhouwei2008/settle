package account

import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

class SettleClientService {

  static transactional = true

  def accountClientService
  def liquidateService

  /**
   * 清结算交易请求同步调用
   * @param srvCode 业务编码
   * @param tradeCode 交易类型编码
   * @param customerNo 客户号
   * @param amount 交易金额，整数
   * @param seqNo 交易流水号
   * @param tradeDate 交易时间，格式为：yyyy-MM-dd HH:mm:ss.SSS
   * @param billDate 入账时间，格式为：yyyy-MM-dd HH:mm:ss.SSS
   * @return { result : 'true or false', errorMsg: ''}
   * result: true为成功， false 为失败,
   * errorMsg: 当result为false时，返回误原因
   * @throws Exception
   */
  def trade(srvCode, tradeCode, customerNo, amount, seqNo, tradeDate, billDate, channel) throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.settle.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/trade'
      body = [srvCode: srvCode, tradeCode: tradeCode, customerNo: customerNo, amount: amount, seqNo: seqNo, tradeDate: tradeDate, billDate: billDate, channel: channel]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }

    // test
    def test1(amount, channel) {
        println "################# channel  " + channel
        Random random = new Random()
        def seqNo = 101202170030000 + Math.abs(random.nextInt()%10000)
        def settleDate = new Date()
        def customerNo = '100000000001524'

        def srvType = settle.FtSrvType.findBySrvCode('online')
        def tradeType = settle.FtSrvTradeType.findBySrvAndTradeCode(srvType, 'payment')
//        def feeSetting = settle.FtTradeFee.findWhere([srv: srvType, tradeType:tradeType, customerNo: '100000000001524', channelCode:channel])
        def feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customerNo, channel,settleDate)

        if (!feeSetting) {
//            feeSetting = settle.FtTradeFee.findWhere([srv: srvType, tradeType:tradeType, customerNo: '100000000001524', channelCode:null])
            feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customerNo,null,settleDate)
        }

        //创建交易日志记录
        def trade = new settle.FtTrade()
        trade.srvCode = 'online'
        trade.tradeCode = 'payment'
        trade.customerNo = '100000000001524'
        trade.amount = amount
        trade.seqNo = seqNo
        trade.channelCode = channel
        trade.realtimeSettle = 1
        trade.liqDate = new Date()
        trade.tradeDate = new Date()
        trade.billDate = new Date()

      // 计费模式为包年包月 无法实时结算
      if (feeSetting.feeModel == 1) {
        log.warn('feeModel is 1, save only')
        try {
          trade.realtimeSettle = 0
          trade.liqDate = null
          trade.save(faleOnError: true)
        } catch (Exception e) {
          log.warn('save trade false', e)
        }
        return
      }
      // 交易日期超出结算有效期 无法实时结算
      if (feeSetting.feeModel != 1) {
        def settleDay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())
        def dateBegin = new java.text.SimpleDateFormat("yyyy-MM-dd").format(feeSetting.dateBegin)
        def dateEnd = new java.text.SimpleDateFormat("yyyy-MM-dd").format(feeSetting.dateEnd)
        if((dateBegin > settleDay) || (dateEnd < settleDay)) {
            log.warn('(dateBegin > settleDay) || (dateEnd < settleDay), save only')
            try {
              trade.realtimeSettle = 0
              trade.liqDate = null
              trade.save(faleOnError: true)
            } catch (Exception e) {
              log.warn('save trade false', e)
            }
            return
        }
      }
        def feeAmount = liquidateService.calcuFeeUpgrade(feeSetting, null, 'online', 'payment', amount, 1, settleDate)
        println "=========================================="
        println feeAmount
        println "=========================================="

      feeAmount = new BigDecimal(feeAmount.toString()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue()

    //查询系统手续费账户
    def sysFeeAcc = boss.BoInnerAccount.findByKey('feeAcc').accountNo

      //计算交易净额
      def netAmount = amount * tradeType.netWeight
      trade.netAmount = netAmount
      if (feeAmount == null) {
        feeAmount = new BigDecimal(0)
      }
      log.info "netAmount is ${netAmount}, fee is ${feeAmount}"
      //手续费转账
      def cmdList = null
      try {
        def customer = ismp.CmCustomer.findByCustomerNo(customerNo)
        def service = boss.BoCustomerService.findByCustomerIdAndServiceCode(customer.id, 'online')
        //判断即收还是后返
        if (feeSetting.fetchType == 0) { //即收
          //设置交易手续费
          trade.preFee = feeAmount
          trade.feeType = 0

          //把交易净额减去手续费的金额从客户服务账户转到客户现金帐户，如果为负则反向
          def settleAmount = netAmount - feeAmount
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, settleAmount, 'settle', seqNo, '0', "实时结算交易净额")

          //把手续费从客户服务帐户扣除到系统手续费帐户，如果为负则反向转账
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, sysFeeAcc, feeAmount, 'fee', seqNo, '0', "实时结算即扣手续费")

        } else { //后返
          //设置交易手续费
          trade.postFee = feeAmount
          trade.feeType = 1
          //把交易净额从客户服务账户转到客户现金帐户，如果为负则反向
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, netAmount, 'settle', seqNo, '0', "实时结算交易净额")
          //把手续费从服务后返手续费帐户转到系统应收手续费帐户，如果手续费为负则反向
          //查询系统应收手续费账户
          def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo
          if (!service) {
            log.warn("service not found")
            return
          }
          cmdList = accountClientService.buildTransfer(cmdList, service.feeAccNo, sysFeeAdvAcc, feeAmount, 'fee',seqNo, '0', "实时结算后收手续费")
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


    // 创建交易
    def testTrade(amount, channel) {
        println "################# channel  " + channel
        Random random = new Random()
        def seqNo = 101202170030000 + Math.abs(random.nextInt()%10000)
        def settleDate = new Date()
        def customerNo = '100000000001524'

        def srvType = settle.FtSrvType.findBySrvCode('online')
        def tradeType = settle.FtSrvTradeType.findBySrvAndTradeCode(srvType, 'payment')
//        def feeSetting = settle.FtTradeFee.findWhere([srv: srvType, tradeType:tradeType, customerNo: '100000000001524', channelCode:channel])
        def feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customerNo, channel,settleDate)

        if (!feeSetting) {
//            feeSetting = settle.FtTradeFee.findWhere([srv: srvType, tradeType:tradeType, customerNo: '100000000001524', channelCode:null])
            feeSetting = liquidateService.getFeeSetting(srvType, tradeType, customerNo,null,settleDate)
        }

        //创建交易日志记录
        def trade = new settle.FtTrade()
        trade.srvCode = 'online'
        trade.tradeCode = 'payment'
        trade.customerNo = '100000000001524'
        trade.amount = amount
        trade.seqNo = seqNo
        trade.channelCode = channel
        trade.realtimeSettle = 0
        trade.liqDate = new Date()
        trade.tradeDate = new Date()
        trade.billDate = new Date()
        trade.liqDate = null
        trade.save(faleOnError: true)

//      // 计费模式为包年包月 无法实时结算
//      if (feeSetting.feeModel == 1) {
//        log.warn('feeModel is 1, save only')
//        try {
//          trade.realtimeSettle = 0
//          trade.liqDate = null
//          trade.save(faleOnError: true)
//        } catch (Exception e) {
//          log.warn('save trade false', e)
//        }
//        return
//      }
//      // 交易日期超出结算有效期 无法实时结算
//      if (feeSetting.feeModel != 1) {
//        def settleDay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())
//        def dateBegin = new java.text.SimpleDateFormat("yyyy-MM-dd").format(feeSetting.dateBegin)
//        def dateEnd = new java.text.SimpleDateFormat("yyyy-MM-dd").format(feeSetting.dateEnd)
//        if((dateBegin > settleDay) || (dateEnd < settleDay)) {
//            log.warn('(dateBegin > settleDay) || (dateEnd < settleDay), save only')
//            try {
//              trade.realtimeSettle = 0
//              trade.liqDate = null
//              trade.save(faleOnError: true)
//            } catch (Exception e) {
//              log.warn('save trade false', e)
//            }
//            return
//        }
//      }
    }
}
