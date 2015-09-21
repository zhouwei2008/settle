package ismp

class CmCustomerBankAccount {

    static mapping = {
        id(generator: 'org.hibernate.id.enhanced.SequenceStyleGenerator', params: [sequence_name: 'seq_cm_customer_bank_account', initial_value: 1])
    }

    CmCustomer customer
    String bankCode
    String branch
    String subbranch
    String bankNo
    String bankAccountNo
    String bankAccountName
    Boolean isCorporate
    Boolean isVerify
    Boolean isDefault
    String status
    String note
    Date dateCreated

    static constraints = {
        customer()
        bankCode(maxSize: 16, blank: false)
        branch(maxSize: 64, blank: false)
        subbranch(maxSize: 64)
        bankNo(maxSize: 16, blank: false)
        bankAccountNo(maxSize: 32, blank: false)
        bankAccountName(maxSize: 40, blank: false)
        status(maxSize: 16, inList: ['normal', 'disabled', 'deleted'])
        note(maxSize: 128, nullable: true)
    }

    def static statusMap = ['normal': '正常', 'disabled': '无效', 'deleted': '删除']
    def static isCorporateMap = ['0': '个人', '1': '企业']
}
