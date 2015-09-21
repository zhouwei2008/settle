package settle

import grails.test.*
import groovy.sql.Sql

class MessageServiceTests extends GrailsUnitTestCase {

     def dataSource_settle

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {

        def billDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', "2012-05-06 12:45:09.678")
         def billDay = billDate.format("yyyy-MM-dd")
           println billDay

      BigDecimal  feeAmount = new BigDecimal(0.toString()).multiply(new BigDecimal('100'))
         println       feeAmount

         //如果手续费率不为0，手续费最小为1分钱
          if (feeAmount <1 && 0 != 0) {
            feeAmount = 1
          }
          feeAmount = feeAmount.multiply(new BigDecimal(10.toString()))
           println       feeAmount

//      BigDecimal feeAmountFact = new BigDecimal(3.3.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(123000.toString()))
//       println       feeAmountFact
//      BigDecimal  deFeeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, 10.toBigDecimal().multiply(new BigDecimal("100"))), 30.toBigDecimal().multiply(new BigDecimal("100"))).toString())
//      feeAmount = feeAmount.add(deFeeAmount)
//      feeAmount = feeAmount.setScale(1, BigDecimal.ROUND_HALF_UP)
//
//
//
//        println       deFeeAmount
//        println       feeAmount
//
//        feeAmount = -feeAmount
//        println       feeAmount
//
//
//
        def settleDate = Date.parse('yyyy-MM-dd', '2012-04-01')
        def seteDate = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', '2012-05-01 00:00:00.000')

        Calendar calendar = Calendar.getInstance()
              calendar.setTime(seteDate)
              def calBegin = Calendar.getInstance()
              calBegin.setTime(calendar.getTime())
              if(settleDate>=seteDate){
                  //有改动
                  while (calendar.getTime() <= settleDate) {
                      calBegin.setTime(calendar.getTime())
                      if (0 == 0) {
                          // 包月
                          calendar.add(Calendar.MONTH, 2)
                      } else {
                          // 包年
                          calendar.add(Calendar.YEAR, 2)
                      }
                  }
              }

              long dateCount = calendar.getTime() - calBegin.getTime()
           println       calendar.getTime()
         println       calBegin.getTime()
          println       dateCount




    }
}
