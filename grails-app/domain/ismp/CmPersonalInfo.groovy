package ismp

class CmPersonalInfo extends CmCustomer {

  static mapping = {
  }

  String identityType
  String identityNo
  Date dateCertification
  Boolean isCertification

  static constraints = {
    identityType(maxSize: 8, blank: false)
    identityNo(maxSize: 32, blank: false)
    dateCertification(nullable: true)
    isCertification(nullable: true)
  }

  def static identityTypeMap = ['id': '身份证', 'arm': '军官证', 'passp': '护照']

}
