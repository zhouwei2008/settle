package boss

class BoBankDic {
  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_bo_bankdic']    
  }
  String code
  String name
  //服务银行列表
//  static hasMany = [ paySrvBanks : BoPaySrvBank ]
  static constraints = {
    code(size: 1..24, blank: false, unique: true)
    name(size: 1..100, blank: false)
//    paySrvBanks(nullable: true)
  }
}
