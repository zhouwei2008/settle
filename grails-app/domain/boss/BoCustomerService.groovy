package boss

class BoCustomerService {
  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_bo_customerservice']
    tablePerHierarchy false
  }
  //客户id
  Long customerId
  //合同号码
  String contractNo
  //服务代码
  String serviceCode
  //生效时间
  Date startTime
  //终止时间
  Date endTime
  //客户经理
  Long customerManagerOperatorId
  //审核状态
  String checkStatus
  //审核时间
  Date checkDate
  //审核操作员
  Long checkOperatorId
  //计费参数
  String feeParams
  //服务参数
  String serviceParams
  //服务账户号
  String srvAccNo
  //手续费账户号
  String feeAccNo
  //是否当前使用
  Boolean isCurrent = true
  //是否可用
  Boolean enable = true

  //创建时间
  Date dateCreated
  //更新时间
  Date lastUpdated
  //所属销售
  String belongToSale

  //服务银行列表
  //static hasMany = [ paySrvBanks : BoPaySrvBank ]

  static constraints = {
    customerId()
    contractNo(size: 1..20, blank: false)
    serviceCode(size: 1..20, blank: false)
    startTime()
    endTime()
    feeParams(size: 1..64, nullable: true)
    serviceParams(size: 1..512, nullable: true)
    isCurrent()
    enable()
    customerManagerOperatorId(nullable: true)
    checkStatus(nullable: true)
    checkDate(nullable: true)
    checkOperatorId(nullable: true)
    srvAccNo(nullable: true)
    feeAccNo(nullable: true)
    paySrvBanks(nullable: true)
    belongToSale(size: 1..20, nullable: true)
  }

  def static serviceMap = ['online': '在线支付', 'royalty': '实时分润','agentcoll':'代收','agentpay':'代付','selfSign':'自助签约']
}
