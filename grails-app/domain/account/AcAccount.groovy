package account

class AcAccount {
  //账户号
  String accountNo
  //账户名称
  String accountName
  //借贷类型, debit - 借记账户， credit - 贷记账户
  String balanceOfDirection
  //账户类型 main - 主账户, freeze - 冻结账户
  String accountType
  //余额
  Long balance = 0
  //币种
  String currency = 'CNY'
  //账户状态: norm - 正常， freeze - 冻结 , closed - 关闭
  String status = 'norm'
  //主账户id
  Long parentId
  //创建时间
  Date dateCreated
  //更新时间
  Date lastUpdated
  //并发版本
  Long version
  //是否可透支，true 可透支，false 不可透支
  Boolean overdraft

  static constraints = {
    accountNo(size: 1..24, blank: false, unique: true)
    accountName(size: 1..50, blank: false)
    balanceOfDirection(size: 1..50, blank: false)
    accountType(size: 1..20, blank: false)
    balance(nullable: false)
    currency(size: 1..20, blank: false)
    status(size: 1..10, blank: false)
    parentId(nullable: true)
    overdraft(nullable: true)

  }
  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_ac_account']
  }

  def static statusMap = [norm: '正常', freeze: '冻结', closed: '关闭']
  def static dirMap = [debit: '借记账户', credit: '贷记账户']
  
}
