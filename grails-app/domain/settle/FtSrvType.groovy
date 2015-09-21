package settle
/**
 * 业务类型
 */
class FtSrvType {

  String srvCode //业务类型编码
  String srvName //业务类型名称

  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_srv_type']
  }

  static constraints = {
    srvCode(maxSize: 20, nullable: false, blank: false, unique: true)
    srvName(maxSize: 30, nullable: false, blank: false)
  }
}
