package ismp

class CmCustomer {

  static mapping = {
    id(generator: 'org.hibernate.id.enhanced.SequenceStyleGenerator', params: [sequence_name: 'seq_cm_customer', initial_value: 1])
    tablePerHierarchy false
  }

  String name
  String customerNo
  String type
  String status = 'init'
  String apiKey
  String accountNo
  Boolean needInvoice= false
  Date dateCreated
  Date lastUpdated


  static constraints = {
    name(maxSize: 32, blank: false)
    customerNo(maxSize: 24, blank: false)
    type(maxSize: 1, inList: ['P', 'C', 'A', 'S'], blank: false)
    status(maxSize: 16, inList: ['init', 'normal', 'disabled', 'deleted'], blank: false)
    apiKey(maxSize: 64, nullable: true)
    accountNo(maxSize: 24, nullable: true)
    needInvoice(nullable: true)
  }

  String toString() {
    return "${name}(${id})"
  }

  def static typeMap = ['P': '个人', 'C': '公司', 'A': '代理', 'S': '系统']
  def static statusMap = ['init': '初始化', 'normal': '正常', 'disabled': '停用', 'deleted': '删除']
}
