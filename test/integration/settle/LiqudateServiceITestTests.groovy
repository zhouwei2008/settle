package settle

import grails.test.*
import groovy.sql.Sql

class LiqudateServiceITestTests extends GroovyTestCase {

    def dataSource_settle

     def liquidateService


    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {
          def billDate = Date.parse('yyyy-MM-dd', "2012-05-18")
//          def feeSetting =   liquidateService.getFeeSetting("7", "8", "8", "7",billDate)
//         println    feeSetting
//        def settleDay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date() - 1)
//      def  tradeSums =  liquidateService.getTradeGroups(settleDay);
//        for (row in tradeSums) {
//            def amount = row.amount
//        def realtimeSettle = row.realtime_settle
//
//            println     amount  instanceof Long
//            println     realtimeSettle   instanceof BigDecimal
//
//            if(realtimeSettle==0) {
//                println "wrwerwerwe"
//            }
//
//        }
//        def srvType = FtSrvType.findBySrvCode("agentpay")
//        println     srvType.id
//        def tradeType = FtSrvTradeType.findBySrvAndTradeCode(srvType, "pay_b_succ")
//        println     tradeType.id
//        def  feeSetting = liquidateService.getFeeSetting(srvType, tradeType, "100000000001226", null,billDate)
//        println      feeSetting?.feeModel
//         println      feeSetting?.feeType
//         println      feeSetting?.channelCode
//         println      feeSetting?.feeValue
//        println      feeSetting?.feeMax
//        println      feeSetting?.feeMin
//        println      feeSetting?.firstLiqDate
//        println      feeSetting?.packType
//        println      feeSetting?.packLen
        def liqNo = liquidateService.getLiqNo()
       def channelSql
       def channel = null  ,realtimeSettle=1
        if(channel){
            channelSql=" and t.channelCode='"+channel+"'"
        } else{
           channelSql=" and t.channelCode is null"
        }
        def realTimeSql
        if(realtimeSettle==0){
            realTimeSql=" and t.realtimeSettle=0"
        } else{
           realTimeSql=" and t.realtimeSettle=1 and feeType=1 "
        }
       def updatsSql="update FtTrade t set t.liquidateNo='${liqNo}',t.liqDate=? where t.customerNo='100000000001226' and t.srvCode='agentpay'  and t.tradeCode='pay_c_fail' and t.billDate>=?   and t.billDate<?  and t.liquidateNo is null " +channelSql+realTimeSql
       println  updatsSql

        FtTrade.executeUpdate(updatsSql,[new Date(), billDate, billDate.plus(1)])


//        FtTrade.executeUpdate("update FtTrade t set t.liquidateNo='${liqNo}',t.liqDate=?,t.channelCode=? where t.customerNo='100000000001226' and t.srvCode='agentpay' and t.tradeCode='pay_c_fail' and t.channelCode is null and t.billDate>=? and t.billDate<? and t.realtimeSettle=1 and feeType=1 and t.liquidateNo is null", [new Date(),null, billDate, billDate.plus(1)])

//        def ftTread = liquidateService.getTradeDetails(srvType.srvCode, tradeType.tradeCode, "100000000000422","1106",billDate,1)
//                println      ftTread?.get(0)?.amount


//        def settleDate = Date.parse('yyyy-MM-dd', '2012-05-02')
//        def seteDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', '2012-04-01 00:00:00.000')
//
//        Calendar calendar = Calendar.getInstance()
//              calendar.setTime(seteDate)
//              def calBegin = Calendar.getInstance()
//              calBegin.setTime(calendar.getTime())
//              if(settleDate>=seteDate){
//                  //有改动
//                  while (calendar.getTime() <= settleDate) {
//                      calBegin.setTime(calendar.getTime())
//                      if (0 == 0) {
//                          // 包月
//                          calendar.add(Calendar.MONTH, 2)
//                      } else {
//                          // 包年
//                          calendar.add(Calendar.YEAR, 2)
//                      }
//                  }
//              }
//        def ftTrade = liquidateService.getStepFeeTotal(srvType.srvCode, tradeType.tradeCode, "100000000000422", "1106",settleDate,calBegin.getTime(),1)
//         def totalAmount = ftTrade.get(0).get("total_amount")
//          println     totalAmount


    }


    void testJj(){
        def billDate = Date.parse('yyyy-MM-dd', "2012-05-15")
        def srvType = FtSrvType.findBySrvCode("online")
        def tradeType = FtSrvTradeType.findBySrvAndTradeCode(srvType, "payment")
       def  feeSetting = liquidateService.getFeeSetting(srvType, tradeType, "100000000000422", "1106",billDate)
        println      feeSetting?.feeModel
    }
}
