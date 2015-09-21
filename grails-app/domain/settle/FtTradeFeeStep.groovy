package settle
/**
 * 交易费率设置
 */
class FtTradeFeeStep extends FtTradeFee {

    //阶梯1起始交易额
    Double step1From
    //阶梯1终止交易额
    Double step1To
    //阶梯1费率分类
    Integer step1FeeType
    //阶梯1费率
    Double step1FeeValue
    //阶梯1最低手续费
    Double step1FeeMin
    //阶梯1最高手续费
    Double step1FeeMax
    //阶梯2起始交易额
    Double step2From
    //阶梯2终止交易额
    Double step2To
    //阶梯2费率分类
    Integer step2FeeType
    //阶梯2费率
    Double step2FeeValue
    //阶梯2最低手续费
    Double step2FeeMin
    //阶梯2最高手续费
    Double step2FeeMax
    //阶梯1起始交易额
    Double step3From
    //阶梯3终止交易额
    Double step3To
    //阶梯3费率分类
    Integer step3FeeType
    //阶梯3费率
    Double step3FeeValue
    //阶梯3最低手续费
    Double step3FeeMin
    //阶梯3最高手续费
    Double step3FeeMax
    //阶梯1起始交易额
    Double step4From
    //阶梯4终止交易额
    Double step4To
    //阶梯4费率分类
    Integer step4FeeType
    //阶梯4费率
    Double step4FeeValue
    //阶梯4最低手续费
    Double step4FeeMin
    //阶梯4最高手续费
    Double step4FeeMax
    //阶梯1起始交易额
    Double step5From
    //阶梯5终止交易额
    Double step5To
    //阶梯5费率分类
    Integer step5FeeType
    //阶梯5费率
    Double step5FeeValue
    //阶梯5最低手续费
    Double step5FeeMin
    //阶梯5最高手续费
    Double step5FeeMax

    //上一累计金额
    Long lastStepAmount

    //上一累计金额更新时间
    Date updateStepDate

    static mapping = {
    }

    static constraints = {
        step1From maxsize: 15, nullable: true
        step1To maxsize: 15, nullable: true
        step1FeeType maxsize: 5, nullable: true
        step1FeeValue maxsize: 12, nullable: true
        step1FeeMin maxsize: 12, nullable: true
        step1FeeMax maxsize: 12, nullable: true

        step2From maxsize: 15, nullable: true
        step2To maxsize: 15, nullable: true
        step2FeeType maxsize: 5, nullable: true
        step2FeeValue maxsize: 12, nullable: true
        step2FeeMin maxsize: 12, nullable: true
        step2FeeMax maxsize: 12, nullable: true

        step3From maxsize: 15, nullable: true
        step3To maxsize: 15, nullable: true
        step3FeeType maxsize: 5, nullable: true
        step3FeeValue maxsize: 12, nullable: true
        step3FeeMin maxsize: 12, nullable: true
        step3FeeMax maxsize: 12, nullable: true

        step4From maxsize: 15, nullable: true
        step4To maxsize: 15, nullable: true
        step4FeeType maxsize: 5, nullable: true
        step4FeeValue maxsize: 12, nullable: true
        step4FeeMin maxsize: 12, nullable: true
        step4FeeMax maxsize: 12, nullable: true

        step5From maxsize: 15, nullable: true
        step5To maxsize: 15, nullable: true
        step5FeeType maxsize: 5, nullable: true
        step5FeeValue(nullable: true)
        step5FeeMin maxsize: 12, nullable: true
        step5FeeMax maxsize: 12, nullable: true
        lastStepAmount(nullable: true)
        updateStepDate(nullable: true)
    }
}
