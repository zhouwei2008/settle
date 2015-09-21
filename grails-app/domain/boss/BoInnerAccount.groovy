package boss
//系统账户字典表
class BoInnerAccount {
  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_bo_inneraccount']
  }
  //键值
  String key
  //内置账户号
  String accountNo
  //说明
  String note

  static constraints = {
  }
}
