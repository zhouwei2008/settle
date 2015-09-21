package settle

import groovy.sql.Sql

import ismp.TradeWithdrawn
import ismp.CmCustomerBankAccount
import ismp.CmCustomer
import boss.BoInnerAccount
import account.AcAccount
import autowithdraw.GenerateNextTime



class AutoWithdrawJob {
    static triggers = {
        //simple name:'testTrigger', startDelay:0, repeatInterval: 300000000, repeatCount: 10
        cron name: 'autoWithdrawTrigger', cronExpression: "0 0 * * * ?"  //  "0/10 * * * * ?"      //
    }

    def dataSource_boss
    def dataSource_ismp
    def accountClientService


    def execute() {
        log.info "Starting AutoWithdraw Job ..."

        //当前时间
        def now = new Date()
        //星期 0..6  //小时 0..23  //日   1..31 //月  0..11
        //test
//        log.info "Day: [0,1,2,14], 2012-2-3 15, 1:" + GenerateNextTime.get("[17]", now, 0,1)
////        log.info "Week: [5#0], 2012-2-3 15, 1:" + generateNextTimeByWeek("[1#0]", new Date(112, 1, 1, 15, 0, 0), 0)
////        log.info "Month:[31#1]" + generateNextTimeByMonth("[31#1", new Date(112, 1, 29, 15, 0, 0), 0)
//        return

//        log.info GenerateNextTime.get("[1#1,2#1,3#1,4#1,5#1,6#1,7#1,8#1,9#1,10#15,11#16,12#17,13#18,14#19,15#20,16#21,17#22,18#23,19#0,20#0,21#23,22#22,23#21,24#20,25#19,26#18,27#17,28#16,29#15,30#18,31#13]",
//                new Date(112, 1, 29, 22, 0, 0), 0, 3)
//        return
        //初始化
        initialization(now)

        //分页读取,减少对内存的压力   后续版本可考虑使用多线程进行处理
//        def pageSize = 50
//        def pageMin = 0
        def sql = new Sql(dataSource_boss)

        def cur = (new Date(now.year, now.month, now.date, now.hours, 0, 0)).format("yyyy-MM-dd HH:mm:ss")

//        while (true) {
//            def query = '''SELECT * FROM (
//                SELECT A.*, ROWNUM RN FROM (
//                    SELECT ID,AMOUNT_TYPE,CUSTOMER_NO,CYCLE_EXPR,CYCLE_TIMES,CYCLE_TYPE,HOLIDAY_WITHDRAW,
//                        KEEP_AMOUNT,LAST_FOOT_DATE,NEXT_FOOT_DATE,WITHDRAW_AMOUNT,WITHDRAW_TYPE
//                    FROM BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
//                    WHERE (NEXT_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss') AND WITHDRAW_TYPE=0)
//                        OR WITHDRAW_TYPE=1
//                    ORDER BY CUSTOMER_NO) A
//                WHERE ROWNUM <= ? )
//                WHERE RN > ? '''
//            def awd = sql.rows(query, [cur, pageSize + pageMin, pageMin])
//            if (awd.size() == 0) {
//                break
//            }
//
//            //解析并处理
//            awd.each {row ->
//                try {
//                    parseRow row, now
//                } catch (Exception e) {
//                    log.error "客户[${row.CUSTOMER_NO}]自动提现出现错误，错误原因：${e.message}"
//                }
//
//            }
//
//            pageMin += pageSize
//        }

        def query = '''SELECT ID,AMOUNT_TYPE,CUSTOMER_NO,CYCLE_EXPR,CYCLE_TIMES,CYCLE_TYPE,HOLIDAY_WITHDRAW,
                        KEEP_AMOUNT,LAST_FOOT_DATE,NEXT_FOOT_DATE,WITHDRAW_AMOUNT,WITHDRAW_TYPE
                    FROM BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                    WHERE (NEXT_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss') AND WITHDRAW_TYPE=0)
                        OR WITHDRAW_TYPE=1
                    ORDER BY CUSTOMER_NO '''
            def awd = sql.rows(query, [cur])

            //解析并处理
            awd.each {row ->
                try {
                    parseRow row, now
                } catch (Exception e) {
                    log.error "客户[${row.CUSTOMER_NO}]自动提现出现错误，错误原因：${e.message}"
                }

            }

        log.info "AutoWithdraw Job Completed."
    }

    def initialization(now) {
        def sql = new Sql(dataSource_boss)
        def query = '''
                SELECT ID,AMOUNT_TYPE,CUSTOMER_NO,CYCLE_EXPR,CYCLE_TIMES,CYCLE_TYPE,HOLIDAY_WITHDRAW,
                        KEEP_AMOUNT,LAST_FOOT_DATE,NEXT_FOOT_DATE,WITHDRAW_AMOUNT,WITHDRAW_TYPE
                FROM BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                    WHERE (NEXT_FOOT_DATE IS NULL OR NEXT_FOOT_DATE < TO_DATE(?,'yyyy-MM-dd HH24:mi:ss'))
                        AND WITHDRAW_TYPE = ?
                        AND CYCLE_EXPR IS NOT NULL
                    ORDER BY CUSTOMER_NO
            '''
        def awd = sql.rows(query, [now.format("yyyy-MM-dd HH:mm:ss"), 0])

        sql.withBatch {stm ->
            awd.each {row ->
                row.NEXT_FOOT_DATE = GenerateNextTime.get(row.CYCLE_EXPR, now, row.HOLIDAY_WITHDRAW, row.CYCLE_TYPE)
                stm.addBatch("""UPDATE BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                SET NEXT_FOOT_DATE = to_date('${row.NEXT_FOOT_DATE.format("yyyy-MM-dd HH:mm:ss")}','yyyy-MM-dd HH24:mi:ss')
                WHERE ID=${row.ID}""".toString())
            }
        }
    }

    def parseRow(row, now) {

        log.info "客户[${row.CUSTOMER_NO}]：开始处理……"


        def exAmount = 0
        def preAmount = 0

        //预留金额
        if (row.AMOUNT_TYPE == 1) {   //'0': '全部提现', '1': '预留提现'
            exAmount = (row.KEEP_AMOUNT * 100)//.toLong()
        }

        //起提金额
        if (row.WITHDRAW_TYPE == 1) {        //['0': '按时间提现', '1': '按金额提现']
            preAmount = (row.WITHDRAW_AMOUNT * 100)//.toLong()
        }

        //提现
        try {
            createWithdraw row.CUSTOMER_NO, exAmount, preAmount
        } catch (Exception e) {
            log.error "客户[${row.CUSTOMER_NO}]自动提现失败，错误原因：${e.message}"
        }

        //更新本次提现时间及下次提现时间
        def lastTime = now.format("yyyy-MM-dd HH:mm:ss")

        def sql = new Sql(dataSource_boss)

        if (row.WITHDRAW_TYPE == 0) {

            try {
                def nextTime = GenerateNextTime.get(row.CYCLE_EXPR, now, row.HOLIDAY_WITHDRAW, row.CYCLE_TYPE).format("yyyy-MM-dd HH:mm:ss")
                def query = """
                Update BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                    SET LAST_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss'),
                        NEXT_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss')
                    WHERE ID = ?
            """
                sql.executeUpdate(query, [lastTime, nextTime, row.ID])
            } catch (Exception e) {
                log.warn "客户[${row.CUSTOMER_NO}]生成下次提现时间失败。"

                def query = """
                Update BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                    SET LAST_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss'),
                        NEXT_FOOT_DATE = NULL
                    WHERE ID = ?
            """
                sql.executeUpdate(query, [lastTime, row.ID])
            }

        } else {
            def query = """
                Update BOSS.BO_CUSTOMER_WITHDRAW_CYCLE
                    SET LAST_FOOT_DATE = TO_DATE(?,'yyyy-MM-dd HH24:mi:ss'),
                        NEXT_FOOT_DATE = NULL
                    WHERE ID = ?
            """
            sql.executeUpdate(query, [lastTime, row.ID])
        }


    }

    def createWithdraw(customerNo, exAmount, preAmount) {
        //创建提现申请
        //param:    customerNo   客户NO
        //          exAmount    提现预留金额
        //          preAmount   起提金额
        def cmCustomer = CmCustomer.findByCustomerNo(customerNo)
        if (!cmCustomer) {
            log.error "客户[${customerNo}]不存在，自动提现失败。"
            return false
        }
        //Long amount = params.amount.toDouble() * 100
        def cmCustomerBankAccount = CmCustomerBankAccount.findByCustomerAndIsDefault(cmCustomer, true)
        if (!cmCustomerBankAccount) {
            log.error "客户[${customerNo} - ${cmCustomer.name}]未设定提现银行，自动提现失败。"
            return
        }
        //1.检查账户余额
        def payerAccount = AcAccount.findByAccountNo(cmCustomer.accountNo)
        if (!payerAccount) {
            log.info "客户[${customerNo} - ${cmCustomer.name}]账户不存在，自动提现取消。"
            return false
        }

        def payerBalance = payerAccount.balance
        if (payerBalance == 0) {
            log.info "客户[${customerNo} - ${cmCustomer.name}]账户余额为0，自动提现取消。"
            return false
        }

        //余额小于起提金额,失败
        if (payerBalance < preAmount) {
            log.info "客户[${customerNo} - ${cmCustomer.name}]账户余额小于最低提现触发额度，自动提现取消。"
            return false
        }

        //余额小于预留金额，失败
        if (payerBalance <= exAmount) {
            log.info "客户[${customerNo} - ${cmCustomer.name}]账户余额小于等于预留金额，自动提现取消。"
            return false
        }

        //计算提现金额
        def amount =(payerBalance - exAmount).toLong()

        //2.写提现交易表
        //交易号
        def prefix = '12'
        def middle = new java.text.SimpleDateFormat('yyMMdd').format(new Date()) // yymmdd

        def sql = new Sql(dataSource_ismp)
        def seq = sql.firstRow('select seq_trade_no.NEXTVAL from DUAL')['NEXTVAL']
        def tradeNo = prefix + middle + seq.toString().padLeft(7, '0')

        def boInnerAccount = BoInnerAccount.findByKey("middleAcc")
        //log.info "boInnerAccount=" + boInnerAccount
        def commandNo = UUID.randomUUID().toString().replaceAll('-', '')
        def cmdList = accountClientService.buildTransfer(null, payerAccount.accountNo, boInnerAccount.accountNo, amount, 'withdrawn', tradeNo, '0', "自动提现")
        def transResult = accountClientService.batchCommand(commandNo, cmdList)

        if (transResult.result != 'true') {
            log.error "客户[${customerNo} - ${cmCustomer.name}]自动提现失败，错误码：${transResult.errorCode} ,错误信息：${transResult.errorMsg.toString().replaceAll("\"", "")}，${cmdList}"
            return false
        }

        def today = new Date()
        def trade = new TradeWithdrawn()
        trade.rootId = null
        trade.originalId = null
        trade.tradeNo = tradeNo
        trade.tradeType = 'withdrawn'
        trade.partnerId = null
        trade.payerId = cmCustomer.id
        trade.payerName = cmCustomer.name
        trade.payerCode = cmCustomer.customerNo
        trade.payerAccountNo = cmCustomer.accountNo
        trade.payeeName = boInnerAccount.note
        trade.payeeCode = boInnerAccount.key
        trade.payeeAccountNo = boInnerAccount.accountNo
        trade.outTradeNo = null
        trade.amount = amount
        trade.currency = 'CNY'
        trade.subject = null
        trade.status = 'processing'
        trade.tradeDate = new java.text.SimpleDateFormat('yyyyMMdd').format(today) as Integer
        trade.note = "自动提现"
        //提现表属性
        trade.submitType = 'manual'
        trade.customerOperId = 0
        trade.submitter = '自动提现'
        trade.transferFee = 0
        trade.realTransferAmount = 0
        trade.customerBankAccountId = cmCustomerBankAccount.id
        trade.customerBankCode = cmCustomerBankAccount.bankCode
        trade.customerBankNo = cmCustomerBankAccount.bankNo
        trade.customerBankAccountName = cmCustomerBankAccount.bankAccountName
        trade.customerBankAccountNo = cmCustomerBankAccount.bankAccountNo
        trade.isCorporate = cmCustomerBankAccount.isCorporate
        trade.handleStatus = 'waiting'
        trade.accountProvince= cmCustomerBankAccount.accountProvince
        trade.accountCity=cmCustomerBankAccount.accountCity
        trade.save flush: true, failOnError: true

        log.info "客户[${customerNo} - ${cmCustomer.name}]自动提现成功。"

        return true
    }
}
