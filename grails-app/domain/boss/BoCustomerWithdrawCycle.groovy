package boss

class BoCustomerWithdrawCycle {

    static mapping = {
        id generator: 'sequence', params: [sequence: 'seq_bo_customer_withdraw_cycle']
    }

    String customerNo
    Integer withdrawType
    BigDecimal withdrawAmount
    Integer cycleType
    Integer cycleTimes
    String cycleExpr
    Integer holidayWithdraw
    Integer amountType
    BigDecimal keepAmount
    Date lastFootDate
    Date nextFootDate

    static constraints = {
        customerNo(size: 1..20, blank: false)
        withdrawType()
        withdrawAmount(nullable: true)
        cycleType(nullable: true)
        cycleTimes(nullable: true)
        cycleExpr(size: 1..100, nullable: true)
        holidayWithdraw(nullable: true)
        amountType(nullable: true)
        keepAmount(nullable: true)
        lastFootDate(nullable: true)
        nextFootDate(nullable: true)
    }

    def static withdrawTypeMap = ['0': '按时间提现', '1': '按金额提现']
    def static cycleTypeMap = ['1': '按日', '2': '按周', '3': '按月']
    def static holidayWithdrawMap = ['0': '否', '1': '是']
    def static amountTypeMap = ['0': '全部提现', '1': '预留提现']
}
