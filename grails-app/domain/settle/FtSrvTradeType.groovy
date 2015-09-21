package settle
/**
 * 业务交易类型
 */
class FtSrvTradeType {
  //所属业务类型
  FtSrvType srv
  //交易类型编码
  String tradeCode
  //交易名称
  String tradeName
  //交易类型权重， -1为负值，1为正值，0为不产生净额
  Integer netWeight

  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_srv_trade_type']
  }

  static constraints = {
    tradeCode(maxSize: 20, nullable: false)
    tradeName(maxSize: 50, nullable: false)
  }

  def static netWeightMap = ['0': '无净额', '1': '正净额', '-1': '负净额']
}
