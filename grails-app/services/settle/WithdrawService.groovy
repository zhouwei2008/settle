package settle

import ismp.CmCustomer
import ismp.CmCustomerBankAccount
import boss.BoInnerAccount
import ismp.TradeWithdrawn

class WithdrawService {

  static transactional = true
  def accountClientService

  def run() {
    checkFoots()
    withdrawFoots()
  }

  //审核所有需要自动审核的结算单，只有单个业务下所有结算单的结算金额之和大于0，才进行审核
  def checkFoots() {
    log.info("start auto check foots")
    def footLs = FtFoot.findAllWhere([type: 0, feeType: 0, checkStatus: 0, autoCheck: 1])
    def groupFoot = footLs.groupBy {
      [customerNo: it.customerNo, srvCode: it.srvCode]
    }
    groupFoot.each {
      def customerNo = it.key.customerNo
      def srvCode = it.key.srvCode
      def footLsByCustomer = it.value
      //检查如果商户的可结算金额之和大于0，才进行自动结算
      def footAmount = 0
      footLsByCustomer.each {
        footAmount += it.amount - it.preFee
      }
      log.info("check customerNo:${customerNo}, srvCode:${srvCode}, total footAmount:${footAmount}")
      if (footAmount >= 0) {
        //按结算金额排序，先结金额大于0的
        def sortedFootLs = footLsByCustomer.sort {-it.amount}
        sortedFootLs.each {
          checkFoot(it)
        }
      }
    }
    log.info("auto check foots success")
  }

  //自动提现所有需要提现的结算单，只有单个业务下所有结算单的可提现金额大于0，才进行提现
  def withdrawFoots() {
    log.info("start auto withdraw foots")
    def footLs = FtFoot.findAllWhere([type: 0, feeType: 0, checkStatus: 1, autoWithdraw: 1, withdraw: 0])
    def groupFoot = footLs.groupBy {
      [customerNo: it.customerNo, srvCode: it.srvCode]
    }
    groupFoot.each {
      def customerNo = it.key.customerNo
      def srvCode = it.key.srvCode
      def customer = CmCustomer.findByCustomerNo(customerNo)
      def footLsByCustomer = it.value
      //检查如果商户的可结算金额之和大于0，才进行自动提现
      def footAmount = 0
      if (footLsByCustomer && footLsByCustomer.size() > 0) {
        footLsByCustomer.each {
          footAmount += it.amount - it.preFee
        }
        log.info("withdraw customerNo:${customerNo}, srvCode:${srvCode}, total footAmount:${footAmount}")
        if (footAmount >= 0) {
          def result = withdraw(footAmount, customer, footLsByCustomer[0].footNo)
          if (result) {
            footLsByCustomer.each {
              it.withdraw = 1
              it.save()
            }
          }
        }
      }
    }
    log.info("auto withdraw foots success")
  }

  /**
   * 审核结算单
   * @param foot
   * @return
   */
  def checkFoot(FtFoot foot) {
    log.info('check foot')
    try {
      def customer = ismp.CmCustomer.findByCustomerNo(foot.customerNo)
//      def customService = boss.BoCustomerService.findByCustomerIdAndServiceCode(customer.id, foot.srvCode)
      def customService = boss.BoCustomerService.findWhere([customerId: customer.id, serviceCode: foot.srvCode, isCurrent: true,enable: true])
      def oughtAcc = boss.BoInnerAccount.findByKey("feeInAdvance").accountNo
      def alreadyAcc = boss.BoInnerAccount.findByKey("feeAcc").accountNo
      def cmdList = []

      //解冻资金账户金额
      if (foot.freezeAmount && foot.freezeAmount > 0) {
        cmdList = accountClientService.buildUnfreeze(cmdList, customer.accountNo, foot.freezeAmount, 'unfrozen', foot.footNo, '0', '结算解冻金额')
      }

      cmdList = accountClientService.buildTransfer(cmdList, customService.srvAccNo, customer.accountNo, foot.amount - foot.preFee, "settle", "0", "0", "净额结算完成")
      if (foot.preFee != 0) {
        cmdList = accountClientService.buildTransfer(cmdList, oughtAcc, alreadyAcc, foot.preFee, "settle", "0", "0", "手续费结算完成")
      }
      def commandNo = UUID.randomUUID().toString().replaceAll('-', '')
      def transResult = accountClientService.batchCommand(commandNo, cmdList)

      println "commandNo=${commandNo},cmdList=${cmdList},transResult.result=${transResult.result}"

      if (transResult.result == 'true') {
        foot.checkStatus = 1
        foot.checkOpId = 0
        foot.checkDate = new Date()
        foot.save(failOnError: true)
        log.info("auto check foot success, ${foot.dump()}")

      }
    } catch (Exception e) {
      log.warn("auto check foot fails, keep not check, foot:${foot.dump()}", e)
    }
  }

  //自动提现
  def withdraw(footAmount, CmCustomer customer, tradeNo) {
    log.info('auto withdraw')
    try {
      if (footAmount <= 0) {
        log.warn('提现金额小于等于0，不进行提现')
        return false
      }
      def cmCustomerBankAccount = CmCustomerBankAccount.findByCustomerAndIsDefault(customer, true)
      if (!cmCustomerBankAccount) {
        log.warn("客户没有设置提现银行，无法生成提现申请。客户号：" + customer.customerNo)
        return false
      }
      def boInnerAccount = BoInnerAccount.findByKey("middleAcc")
      def cmdList = []
      cmdList = accountClientService.buildTransfer(null, customer.accountNo, boInnerAccount.accountNo, footAmount, "withdrawn", "0", "0", "结算自动提现")

      def commandNo = UUID.randomUUID().toString().replaceAll('-', '')
      def transResult = accountClientService.batchCommand(commandNo, cmdList)

      println "commandNo=${commandNo},cmdList=${cmdList},transResult.result=${transResult.result}"

      if (transResult.result == 'true') {
        //写提现交易表
        def trade = new TradeWithdrawn()
        trade.rootId = null
        trade.originalId = null
        trade.tradeNo = tradeNo
        trade.tradeType = 'withdrawn'
        trade.partnerId = null
        trade.payerId = customer.id
        trade.payerName = customer.name
        trade.payerCode = customer.customerNo
        trade.payerAccountNo = customer.accountNo
        //trade.payeeId=0
        trade.payeeName = boInnerAccount.note
        trade.payeeCode = boInnerAccount.key
        trade.payeeAccountNo = boInnerAccount.accountNo
        trade.outTradeNo = null
        trade.amount = footAmount
        trade.currency = 'CNY'
        trade.subject = null
        trade.status = 'completed'
        trade.tradeDate = new java.text.SimpleDateFormat('yyyyMMdd').format(new Date()) as Integer
        trade.note = '结算自动提现'
        //提现表属性
        trade.submitType = 'automatic'
        trade.customerOperId = 0
        trade.submitter = '结算系统'
        trade.transferFee = 0
        trade.realTransferAmount = 0
        trade.customerBankAccountId = cmCustomerBankAccount.id
        trade.customerBankCode = cmCustomerBankAccount.bankCode
        trade.customerBankNo = cmCustomerBankAccount.bankNo
        trade.customerBankAccountName = cmCustomerBankAccount.bankAccountName
        trade.customerBankAccountNo = cmCustomerBankAccount.bankAccountNo
        trade.isCorporate = cmCustomerBankAccount.isCorporate
        trade.handleStatus = 'waiting'
        trade.save flush: true, failOnError: true
        log.info("auto withdraw success, customer:${customer.customerNo}, footAmount:${footAmount}")
        return true
      } else {
        log.warn('自动提现失败')
        return false
      }
    } catch (Exception e) {
      log.warn('自动提现失败', e)
      return false
    }
  }
}
