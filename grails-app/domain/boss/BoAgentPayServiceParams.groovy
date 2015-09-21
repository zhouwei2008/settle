package boss

class BoAgentPayServiceParams extends  BoCustomerService
{

    //收费方式（0：按笔收费   1：按批次收费    2：按流量收费 ）
    String gatherWay
    //手续费（对公每笔、每批次、费率）
    Double procedureFee
    //手续费（对私每笔）
    Double perprocedureFee
    //单笔限额
    Double limitMoney
    //日交易限额（笔数）
    Long dayLimitTrans
    //日交易限额（金额）
    Double dayLimitMoney
    //月交易限额（笔数）
    Long monthLimitTrans
    //月交易限额（金额）
    Double monthLimitMoney
    //年费
    Double yearlyPayment
    //风险保证金
    Double dangerMoney
    //结算方式（0：即扣  1：后返）
    String settWay
    //是否退还手续费  0：是  1：否
    String backFee
    //结算周期  0：T+?天结算  1：按通知结算
    String settCycle
    //T+结算天数
    String settCycleDay
    //批次版本号
    String remark

    String batchVersion  = "00"

    static constraints = {
         gatherWay(nullable:true)
         procedureFee(nullable:true)
         perprocedureFee(nullable:true)
         limitMoney(nullable:true)
         dayLimitTrans(nullable:true)
         dayLimitMoney(nullable:true)
         monthLimitTrans(nullable:true)
         monthLimitMoney(nullable:true)
         yearlyPayment(nullable:true)
         dangerMoney(nullable:true)
         settWay(nullable:true)
         backFee(nullable:true)
         settCycle(nullable:true)
         settCycleDay(nullable:true)
         remark(nullable:true)
         batchVersion(nullable:true)
    }
    def static serviceParamsMap = ['0': '是', '1': '否']
}
