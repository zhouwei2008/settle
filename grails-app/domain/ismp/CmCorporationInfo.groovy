package ismp

class CmCorporationInfo extends CmCustomer {

    static mapping = {
    }

    String  registrationName
    String businessLicenseCode
    String organizationCode
    String taxRegistrationNo
    Date registrationDate
    Date licenseExpires
    String businessScope
    Double registeredFunds
    String registeredPlace
    String numberOfStaff
    String expectedTurnoverOfYear
    String checkStatus
    Long checkOperatorId
    Date checkDate
    String corporate
    String companyPhone
    String officeLocation
    String contact
    String contactPhone
    String zipCode
    String note

    static constraints = {
        registrationName(maxSize: 64,blank: false)
        businessLicenseCode(maxSize:20,blank:false)
        organizationCode(maxSize:20,blank:false)
        taxRegistrationNo(maxSize:20,blank:false)
        businessScope(maxSize:500,blank:false)
        registeredFunds(nullable:true)
        registeredPlace(maxSize:200,nullable:true)
        numberOfStaff(maxSize: 20,nullable: true)
        expectedTurnoverOfYear(maxSize: 20,nullable:true)
        checkStatus(maxSize:16,nullable:true)
        checkOperatorId(nullable:true)
        checkDate(nullable:true)
        corporate(maxSize:32,nullable:true)
        companyPhone(maxSize:20,nullable:true)
        officeLocation(maxSize:200,nullable:true)
        contact(maxSize:32,nullable:true)
        contactPhone(maxSize:20,nullable:true)
        zipCode(maxSize:10,nullable:true)
        note(maxSize:128,nullable:true)
    }
}
