package settle

class FtFoot {

  //服务类型编码
  String srvCode
  //交易类型编码
  String tradeCode
  //交易金额 ,单位分
  Long amount
  //交易笔数
  Integer transNum
  //客户号
  String customerNo
  //结算单类型：0为自动，1为手工
  Integer type
  //手续费类型,0为交易净额结算单，1为后返手续费结算单, 2为包流量预付手续费结算单
  Integer feeType = 0
  //即收手续费金额
  Long preFee
  //后收手续费金额
  Long postFee
  //结算发生时间
  Date footDate
  //结算批次号
  String footNo
  //审核状态，0为待审核，1为审核通过，结算完成，2为审核拒绝
  Integer checkStatus
  //审核操作员ID
  Long checkOpId
  //审核时间
  Date checkDate
  //手工生成结算单操作员
  Long createOpId
  //拒绝原因
  String rejectReason

  //资金帐户冻结金额
  Long freezeAmount
  //是否自动审核，0 否，1 是
  Integer autoCheck
  //是否自动提现，0 否，1 是
  Integer autoWithdraw
  //是否已提现，0 否，1 是
  Integer withdraw


  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_foot']
  }

  static constraints = {

    srvCode(maxSize: 20, nullable: false)
    tradeCode(maxSize: 20, nullable: false)
    customerNo(maxSize: 24, nullable: false)
    type(nullable: false)
    footNo(maxSize: 30, nullable: false)
    checkStatus(nullable: false)
    checkOpId(nullable: true)
    checkDate(nullable: true)
    createOpId(nullable: true)
    rejectReason(nullable: true)
    freezeAmount(nullable: true)
    autoCheck(nullable: true)
    autoWithdraw(nullable: true)
    withdraw(nullable: true)
  }

  def static checkStatusMap = ['':'-请选择-','0': '待审', '1': '通过', '2': '拒绝']
  def static feetypeMap = ['0': '结算单', '1': '后返手续费', '2': '预收手续费']
}
