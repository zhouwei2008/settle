package settle

class FtSrvFootSetting {

  //所属客户号
  String customerNo
  //所属业务类型
  FtSrvType srv
  //所属交易类型
  FtSrvTradeType tradeType
  //结算周期类型，0为实时结算，1为日结，2为周结，3为月结, 4为年结
  Integer footType
  //押款天数（风险预存期）
  Integer mortDay
  //周期内结算次数
  Integer footTimes
  //结算表达式，在指定的时间间隔什么时候执行(形式是*,*.如：每月1日和15日执行就是1,15)
  String footExpr
  //最低结算金额
  Double footAmount
  //上次结算日期
  Date lastFootDate
  //下次结算日期
  Date nextFootDate
  //是否需要审核结算单 0不需要，1需要
  Integer type
  //提现方式,0手动提现，1自动提现
  Integer withdraw

  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_srv_foot_setting']
  }

  static constraints = {
    customerNo(maxSize: 24, nullable: false)
    footType(size: 2, nullable: false)
    mortDay(size: 5, nullable: false)
    footTimes(size: 2, nullable: false)
    footExpr(maxSize: 100, nullable: false)
    footAmount(nullable: false)
    lastFootDate(nullable: true)
    nextFootDate(nullable: true)
    type(nullable: true)
    withdraw(nullable: true)
  }

    
     def static tradeTypeMap = ['0': '实时结算', '1': '日结','2':'周结','3':'月结','4':"年结"]
     def static typeMap =["0":"不需要","1":"需要"]
     def static timesMap_01 = ['1': '1']
     def static timesMap_02 = ['1': '1', '2': '2','3':'3','4':'4','5':'5','6':'6','7':'7']
     def static timesMap_03 = ['1': '1', '2': '2','3':'3','4':'4','5':'5', '6': '6', '7': '7','8':'8','9':'9','10':'10',
     '11': '11', '12': '12','13':'13','14':'14','15':'15', '16': '16', '17': '17','18':'18','19':'19','20':'20',
    '21': '21', '22': '22','23':'23','24':'24','25':'25', '26': '26', '27': '27','28':'28']
     def static timesMap_04 =['1': '1', '2': '2','3':'3','4':'4','5':'5','6':'6','7':'7','8':'8','9':'9','10':'10',
     '11': '11', '12': '12']
     def static withdrawMap =["0":"手动提现","1":"自动提现"]

     
}
