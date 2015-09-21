package settle
/**
 * 交易日志
 */
class FtTrade {
  //服务类型编码
  String srvCode
  //交易类型编码
  String tradeCode
  //交易金额 ，单位分
  Long amount
  //即收手续费金额
  Long preFee
  //后收手续费金额
  Long postFee
  //是否实时结算交易 ,0为非实时，1为实时
  Integer realtimeSettle
  //如果实时结算，手续费类型,0为即收手续费，1为后返手续费
  Integer feeType
  //客户号
  String customerNo
  //交易流水号
  String seqNo
  //交易发生时间
  Date tradeDate
  //入帐时间
  Date billDate
  //清算时间
  Date liqDate
  //生成时间
  Date dateCreated
  //清算批次号
  String liquidateNo
  //是否转账失败，true为失败，需重转账
  Boolean redo
  //重作成功时间
  Date redoTime
  //交易净额
  Long netAmount
  //资金帐户冻结金额
  Long freezeAmount
  //交易通道号
  String channelCode
   //计算交易通道号
  String  channelCodeCal

  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_trade']
  }

  static constraints = {
    srvCode(maxSize: 20, nullable: false)
    tradeCode(maxSize: 20, nullable: false)
    customerNo(maxSize: 24, nullable: false)
    seqNo(maxSize: 24, nullable: false)
    tradeDate(nullable: false)
    liquidateNo(nullable: true)
    preFee(nullable: true)
    postFee(nullable: true)
    liqDate(nullable: true)
    redo(nullable: true)
    netAmount(nullable: true)
    redoTime(nullable: true)
    feeType(nullable: true)
    freezeAmount(nullable: true)
    channelCode(maxSize: 20, nullable: true)
    channelCodeCal(maxSize: 20, nullable: true)
  }
}
