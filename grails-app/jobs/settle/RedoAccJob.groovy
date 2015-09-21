package settle

import boss.BoCustomerService
import ismp.CmCustomer


class RedoAccJob {

  def concurrent = false

  def accountClientService

  static triggers = {
    simple name: 'RodoJobTrigger', startDelay: 60000, repeatInterval: 3600000
  }


  def execute() {
    log.info "start redo trade"
    def tradeRedo = FtTrade.findAllByRealtimeSettleAndRedo(1, true)
    log.info "trade to redo : ${tradeRedo.size()}"
    tradeRedo.each {trade ->
      def cmdList = null

      def customer = CmCustomer.findByCustomerNo(trade.customerNo)
//      def service = BoCustomerService.findByCustomerIdAndServiceCode(customer.id, trade.srvCode)
      def service = BoCustomerService.findWhere([customerId: customer.id, serviceCode: trade.srvCode, isCurrent: true,enable: true])
      if (trade.feeType==0) { //即扣手续费
        def sysFeeAcc = boss.BoInnerAccount.findByKey('feeAcc').accountNo
        //净额转账
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, trade.netAmount - trade.preFee, 'settle', trade.seqNo, '0', "重做实时结算交易净额")
        //手续费转账
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, sysFeeAcc, trade.preFee, 'fee', trade.seqNo, '0', "重做实时结算即扣手续费")
      } else if (trade.feeType==1) { //后返手续费
        def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo
        //净额转账
        cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, customer.accountNo, trade.netAmount, 'settle', trade.seqNo, '0', "重做实时结算交易净额")
        //手续费转账
        cmdList = accountClientService.buildTransfer(cmdList, service.feeAccNo, sysFeeAdvAcc, trade.postFee, 'fee', trade.seqNo, '0', "重做实时结算后收手续费")
      }

      boolean redo = false //转账失败，是否重做
      try {
        def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
        if (transResult.result != 'true') {
          log.warn("重做实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
          //帐户余额不足或者账务系统故障需要重新转账
          if (transResult.errorCode == '03' || transResult.errorCode == 'ff') {
            redo = true
          }
        }
      } catch (Exception e) {
        log.warn("balance trans faile,cmdList:${cmdList}", e)
        redo = true
      }

      if (!redo) {
        trade.redo = false
        trade.redoTime = new Date()
        trade.save()
      }
    }
    log.info "redo trade finished"


    log.info "start redo liquidate"
    def liqRedo = FtLiquidate.findAllByRedo(true)
    log.info "liq to redo : ${liqRedo.size()}"
    liqRedo.each {liq ->
      //查询系统应收手续费账户
      def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo
      //查询客户信息
      def customer = CmCustomer.findByCustomerNo(liq.customerNo)
      //查询服务信息
//      def service = BoCustomerService.findByCustomerIdAndServiceCode(customer.id, liq.srvCode)
      def service = BoCustomerService.findWhere([customerId: customer.id, serviceCode: liq.srvCode, isCurrent: true,enable: true])
      def cmdList = null
      if (liq.settleType == 0) { //非实时清算单
        if (liq.feeType == 0) { //即收手续费
          cmdList = accountClientService.buildTransfer(cmdList, service.srvAccNo, sysFeeAdvAcc, liq.preFee, 'fee', liq.liquidateNo, '0', "重做定时清算即扣手续费")
        } else if (liq.feeType == 1) {//后返手续费
          cmdList = accountClientService.buildTransfer(cmdList, service.feeAccNo, sysFeeAdvAcc, liq.postFee, 'fee', liq.liquidateNo, '0', "重做定时清算后收手续费")
        }
      }

      boolean redo = false //转账失败，是否重做
      try {
        def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
        if (transResult.result != 'true') {
          log.warn("重做实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg},cmdList:${cmdList}")
          //帐户余额不足或者账务系统故障需要重新转账
          if (transResult.errorCode == '03' || transResult.errorCode == 'ff') {
            redo = true
          }
        }
      } catch (Exception e) {
        log.warn("balance trans faile,cmdList:${cmdList}", e)
        redo = true
      }

      if (!redo) {
        liq.redo = false
        liq.redoTime = new Date()
        liq.save()
      }
    }


  }


}
