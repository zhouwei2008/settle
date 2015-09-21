package settle

import boss.BoCustomerService
import groovy.sql.Sql
import ismp.CmCustomer
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

class LiquidateService {

  static transactional = true

  def dataSource_settle

  def accountClientService

  def grailsTemplateEngineService

  def run() {
    log.info 'start liquidate'

    def dbSettle = new Sql(dataSource_settle)

    def settleDay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date())
    log.info "settleDay : ${settleDay}"

//    def queryTest = """select count(*) co,
//                       sum(amount) amount,
//                       sum(post_fee) post_fee,
//                       sum(freeze_amount) freeze_amount,
//                       customer_no,
//                       srv_code,
//                       trade_code,
//                       to_char(bill_date, 'yyyy-mm-dd') bill_date,
//                       realtime_settle,
//                       channel_code
//                  from ft_trade
//                 where (realtime_settle = 0 or (realtime_settle = 1 and fee_type = 1))
//                   and liquidate_no is null
//                   and bill_date < sysdate + 1
//                   and customer_no = 100000000001524
//                 group by customer_no,
//                          srv_code,
//                          trade_code,
//                          to_char(bill_date, 'yyyy-mm-dd'),
//                          realtime_settle,
//                          channel_code
//                 order by
//                       bill_date asc"""
//    def tradeSums = dbSettle.rows(queryTest)

   //按customer_no，srv_code，trade_code，bill_date，realtime_settle，channel_code分组查询清算日期前的所有交易
    def query = """select count(*) co,
                       sum(amount) amount,
                       sum(post_fee) post_fee,
                       sum(freeze_amount) freeze_amount,
                       customer_no,
                       srv_code,
                       trade_code,
                       to_char(bill_date, 'yyyy-mm-dd') bill_date,
                       realtime_settle,
                       channel_code
                  from ft_trade
                 where (realtime_settle = 0 or (realtime_settle = 1 and fee_type = 1))
                   and liquidate_no is null
                   and bill_date < to_date('${settleDay}','yyyy-mm-dd')
                 group by customer_no,
                          srv_code,
                          trade_code,
                          to_char(bill_date, 'yyyy-mm-dd'),
                          realtime_settle,
                          channel_code
                 order by
                       bill_date asc
                         """

    def tradeSums = dbSettle.rows(query)

    //查询系统应收手续费账户
    def sysFeeAdvAcc = boss.BoInnerAccount.findByKey('feeInAdvance').accountNo

    for (row in tradeSums) {

      try {
        log.info "trade sums : ${row.dump()}"
        def srvCode = row.srv_code
        def customerNo = row.customer_no
        def tradeCode = row.trade_code
        def channel = row.channel_code
        def billDate = row.bill_date
        def count = row.co
        def amount = row.amount
        def realtimeSettle = row.realtime_settle
        def postFee = row.post_fee
        def freezeAmount = row.freeze_amount

        def settleDate = Date.parse('yyyy-MM-dd', billDate)


        //查询业务类型信息，交易类型和结算设置
        def srvType = FtSrvType.findBySrvCode(srvCode)
        //业务类型不匹配不进行处理
        if (!srvType) {
          log.warn('srvType not found! ')
          continue
        }
        //查询交易类型，不匹配不进行处理
        def tradeType = FtSrvTradeType.findBySrvAndTradeCode(srvType, tradeCode)
        if (!tradeType) {
          log.warn('tradeType not found! ')
          continue
        }
        //查询客户信息
        def customer = CmCustomer.findByCustomerNo(customerNo)
        if (!customer) {
          log.warn('customer not found! ')
          continue
        }

        //查询服务信息
//        def service = BoCustomerService.findByCustomerIdAndServiceCode(customer.id, srvCode)
        def service = BoCustomerService.findWhere([customerId: customer.id, serviceCode: srvCode, isCurrent: true,enable: true])
        if (!service) {
          log.warn('service not found! ')
          continue
        }


          def feeSetting

          //guonan update  查询费率
          feeSetting = getFeeSetting(srvType, tradeType, customer.customerNo, channel,settleDate)
          if (!feeSetting) {
             feeSetting = getFeeSetting(srvType, tradeType, customer.customerNo,null,settleDate)
          }
          if (!feeSetting) {
            log.warn("feeSetting not found ,srvType:${srvType},tradeType:${tradeType},customerNo:${customer.customerNo}")
            continue
          }


          //计算手续费
          BigDecimal feeAmount = new BigDecimal("0")

         log.info "realtimeSettle is ${realtimeSettle}"

            //只计算非实时
          if(realtimeSettle==0){
              log.info "feeModel is ${feeSetting.feeModel}"
              log.info "channelCode is ${feeSetting.channelCode}"
           if(feeSetting.feeValue==null){
               feeSetting.feeValue=0
           }
          // 判断计费模式 0为固定，1为包年包月，2为交易量（即阶梯）
          if (feeSetting.feeModel == 0) {
               log.info "feeModel is 固定"
               log.info "feeType is ${feeSetting.feeType}"
              // 按笔收
              if (feeSetting.feeType == 0) {
                  log.info "feeType is 按笔"
                  log.info "feeValue is ${feeSetting.feeValue}"

                  feeAmount = new BigDecimal(feeSetting.feeValue.toString()).multiply(new BigDecimal('100'))
                  //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount < 1 && feeSetting.feeValue != 0) {
                    feeAmount = 1
                  }
                 log.info "one feeAmount is ${feeAmount}"
                  //新增分组修改按笔需乘以总数
                  def ftTread = getTradeDetails(dbSettle,srvType.srvCode, tradeType.tradeCode, customer.customerNo,channel,settleDate,realtimeSettle)
                  if (ftTread) {
                      for (detail in ftTread) {
                          updateTrade(dbSettle,feeSetting,feeAmount,detail.id)
                      }
                  }
                  if(count==null){
                       count=0
                  }
                  feeAmount = feeAmount.multiply(new BigDecimal(count.toString()))
                  log.info "count feeAmount is ${count},${feeAmount}"
              // 按流量收
              } else if (feeSetting.feeType == 1) {
                  log.info "feeType is 按流量"
                  log.info "feeValue is ${feeSetting.feeValue}"
                  log.info "feeMin is ${feeSetting.feeMin}"
                  log.info "feeMax is ${feeSetting.feeMax}"

                  def ftTread = getTradeDetails(dbSettle,srvType.srvCode, tradeType.tradeCode, customer.customerNo, channel,settleDate,realtimeSettle)
                  if (ftTread) {
                      for (detail in ftTread) {
                          def deAmount = detail.amount
                           log.info "one amount is ${deAmount}"
                          BigDecimal deFeeAmount = new BigDecimal("0")
                          BigDecimal feeAmountFact = new BigDecimal(feeSetting.feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(deAmount.toString()))
                          // 最高最低手续费
                          if ((feeSetting.feeMin != null) &&(feeSetting.feeMin != 0)&& (feeSetting.feeMax != null) && (feeSetting.feeMax != 0)) {
                              deFeeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                          } else if (feeSetting.feeMin != null &&feeSetting.feeMin != 0) {
                              deFeeAmount = new BigDecimal(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                          } else if (feeSetting.feeMax != null && feeSetting.feeMax != 0) {
                              deFeeAmount = new BigDecimal(Math.min(feeAmountFact, feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                               //如果手续费率不为0，手续费最小为1分钱
                              if (deFeeAmount <1 && feeSetting.feeValue != 0) {
                                deFeeAmount = 1
                              }
                          } else {
                              deFeeAmount = new BigDecimal(feeAmountFact.toString())
                               //如果手续费率不为0，手续费最小为1分钱
                              if (deFeeAmount <1 && feeSetting.feeValue != 0) {
                                deFeeAmount = 1
                              }
                          }
                          log.info "one feeAmount is ${deFeeAmount}"
                          feeAmount = feeAmount.add(deFeeAmount)

                          updateTrade(dbSettle,feeSetting,deFeeAmount,detail.id)
                      }
                  }
                   log.info "count feeAmount is ${count},${feeAmount}"
              // 包流量
              } else if (feeSetting.feeType == 2) {
                  log.info "feeType is 包流量"
                  log.info "流量限额 is ${feeSetting.feeFlow}(单位：元)"
                  log.info "已使用流量 is ${feeSetting.feeUsedFlow}(单位：分)"
                  log.info "预付手续费 is ${feeSetting.feePre}(单位：元)"
                  log.info "超量费率 is ${feeSetting.feeValue}%"
                  log.info "最低手续费 is ${feeSetting.feeMin}(单位：元)"
                  log.info "最高手续费 is ${feeSetting.feeMax}(单位：元)"


                  def str = ConfigurationHolder.config.grails.opeatorEmail
                  def opeatorEmail = str
                  def sellerEmail
                  def flag = 0
                  if (str.indexOf(",") != -1) {
                      def flags = str.split(",")
                      opeatorEmail = flags[0]
                      sellerEmail = flags[1]
                  }
                  def feeFlow = (new BigDecimal(String.valueOf(feeSetting.feeFlow))).multiply(new BigDecimal("100")).longValue()
                  def newFeeUsedFlow = (new BigDecimal(String.valueOf(feeSetting.feeUsedFlow))).longValue()

                  def ftTread = getTradeDetails(dbSettle,srvType.srvCode, tradeType.tradeCode, customer.customerNo, channel,settleDate,realtimeSettle)
                  if (ftTread) {
                      for (detail in ftTread) {
                          def deAmount = detail.amount
                          newFeeUsedFlow = (new BigDecimal(String.valueOf(newFeeUsedFlow))).add(new BigDecimal(String.valueOf(deAmount))).longValue()

                          if (newFeeUsedFlow <= feeFlow) {
                              feeAmount = 0

                              def perDel = (new BigDecimal(String.valueOf(feeSetting.feeFlow))).multiply(new BigDecimal("80")).compareTo(new BigDecimal(String.valueOf(newFeeUsedFlow)))
                              if ((!feeSetting.isViewPerMail) && feeSetting.isViewPer && (perDel <= 0)) {
                                  // 发信
                                  def srvName = FtSrvType.findBySrvCode(srvCode)?.srvName
                                  sendEmail("/isViewMail/isViewPerMail","包流量费率80%提示",opeatorEmail,[item: feeSetting, srvName: srvName])
                                  sendEmail("/isViewMail/isViewPerMail","包流量费率80%提示",sellerEmail,[item: feeSetting, srvName: srvName])
                                  feeSetting.isViewPerMail = true
                              }
                          } else {
                              log.info "one amount is ${deAmount}"
                              BigDecimal deFeeAmount = new BigDecimal("0")
                              BigDecimal feeAmountFact = new BigDecimal(feeSetting.feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(deAmount.toString()))
                              // 最高最低手续费
                              if ((feeSetting.feeMin != null) && (feeSetting.feeMin != 0) && (feeSetting.feeMax != null) && (feeSetting.feeMax != 0)) {
                                  deFeeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                              } else if (feeSetting.feeMin != null && feeSetting.feeMin != 0) {
                                  deFeeAmount = new BigDecimal(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                              } else if (feeSetting.feeMax != null && feeSetting.feeMax != 0) {
                                  deFeeAmount = new BigDecimal(Math.min(feeAmountFact, feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                                   //如果手续费率不为0，手续费最小为1分钱
                                  if (deFeeAmount < 1 && feeSetting.feeValue != 0) {
                                      deFeeAmount = 1
                                  }
                              } else {
                                  deFeeAmount = new BigDecimal(feeAmountFact.toString())
                                   //如果手续费率不为0，手续费最小为1分钱
                                  if (deFeeAmount < 1 && feeSetting.feeValue != 0) {
                                      deFeeAmount = 1
                                  }
                              }
                              log.info "one feeAmount is ${deFeeAmount}"
                              feeAmount = feeAmount.add(deFeeAmount)

                              updateTrade(dbSettle,feeSetting,deFeeAmount,detail.id)

                              if ((!feeSetting.isViewOverMail) && feeSetting.isViewOver) {
                                  // 发信
                                  def srvName = FtSrvType.findBySrvCode(srvCode)?.srvName
                                  sendEmail("/isViewMail/isViewOverMail","包流量费率超量提示",opeatorEmail,[item: feeSetting, srvName: srvName])
                                  sendEmail("/isViewMail/isViewOverMail","包流量费率超量提示",sellerEmail,[item: feeSetting, srvName: srvName])
                                  feeSetting.isViewOverMail = true
                              }
                          }
                      }
                  }

                  feeSetting.feeUsedFlow = newFeeUsedFlow
                  feeSetting.save()
                  log.info "手续费 is ${feeAmount}"
              }
          } else if (feeSetting.feeModel == 1) {
               if(feeSetting.firstLiqDate==null){
                   continue
               }

              log.info "feeModel is 包年包月"
              log.info "firstLiqDate is ${feeSetting.firstLiqDate}"
              Calendar calendar = Calendar.getInstance()

              calendar.setTime(feeSetting.firstLiqDate)
              def calBegin = Calendar.getInstance()
              calBegin.setTime(calendar.getTime())
              if(settleDate<feeSetting.firstLiqDate){
                  continue
              }
               if(feeSetting.packLen==null){
                   feeSetting.packLen=1
               }
                  //有改动
                  while (calendar.getTime() <= settleDate) {
                      calBegin.setTime(calendar.getTime())
                      if (feeSetting.packType == 0) {
                          // 包月
                          calendar.add(Calendar.MONTH, feeSetting.packLen)
                      } else {
                          // 包年
                          calendar.add(Calendar.YEAR, feeSetting.packLen)
                      }
                  }

              log.info "calBegin is ${calBegin.getTime()}"
              log.info "calendar is ${calendar.getTime()}"
              long dateCount = calendar.getTime() - calBegin.getTime()
              log.info "dateCount is ${dateCount}"
              log.info "feeValue is ${feeSetting.feeValue}"

                 feeAmount = new BigDecimal(String.valueOf(feeSetting.feeValue.toBigDecimal().multiply(new BigDecimal("100")).divide(new BigDecimal(String.valueOf(dateCount)), 1, BigDecimal.ROUND_HALF_UP)))
                 //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount <1 && feeSetting.feeValue != 0) {
                    feeAmount = 1
                  }
               log.info "feeAmount is ${feeAmount}"
          } else if (feeSetting.feeModel == 2) {

               log.info "feeModel is 阶梯"
              // 阶梯费率信息
              def feeStep = FtTradeFeeStep.get(feeSetting.id)
              if(feeStep==null){
                   continue
               }
              // 阶梯费率该包年（月）周期内金额总流量      ;

               log.info "firstLiqDate is ${feeSetting.firstLiqDate}"

               if(feeSetting.firstLiqDate==null){
                   continue
               }
              Calendar calendar = Calendar.getInstance()
              calendar.setTime(feeSetting.firstLiqDate)
              def calBegin = Calendar.getInstance()
              calBegin.setTime(calendar.getTime())
              if(settleDate<feeSetting.firstLiqDate){
                  continue
              }
              log.info "packType is ${feeSetting.packType}"
              log.info "packLen is ${feeSetting.packLen}"
               if(feeSetting.packLen==null){
                   feeSetting.packLen=1
               }
                  while (calendar.getTime() <= settleDate) {
                      calBegin.setTime(calendar.getTime())
                      if (feeSetting.packType == 0) {
                          // 包月
                          calendar.add(Calendar.MONTH, feeSetting.packLen)
                      } else {
                          // 包年
                          calendar.add(Calendar.YEAR, feeSetting.packLen)
                      }
                  }


          updateTradeChannel(srvCode, tradeCode,customer.customerNo,channel,feeSetting.channelCode,settleDate)

          def lastTotalAmount =  feeStep.lastStepAmount
          def lastUpdateStepDate =  feeStep.updateStepDate
           log.info "lastTotalAmount is ${feeStep.lastStepAmount}"
           log.info "lastUpdateStepDate is ${feeStep.lastStepAmount}"

          def  isLiqDate=0
          //如果当前清算天是周期第一天，则金额归0
          if(settleDate==calBegin.getTime()){
              isLiqDate=1
              lastTotalAmount=0
          }
          def ftTrade
          //如果没有记录之前的累计金额
          if(!lastUpdateStepDate){
              //查询当前清算天前到周期起始日期的累计金额
              if(isLiqDate==0){
                  ftTrade = getStepFeeTotal(srvCode, tradeCode, customerNo, feeSetting.channelCode,settleDate,calBegin.getTime(),realtimeSettle)
                  lastTotalAmount = ftTrade.get(0).get("total_amount")
              }
              feeStep.updateStepDate= settleDate
              feeStep.lastStepAmount= lastTotalAmount
              feeStep.save(flush:true)

          }//如果累计金额的更新时间小天当前清算天，说明金额只累计到当前清算天前，今天需要查出当前清算天到上一次累计金额更新时间之间的金额
          else if(lastUpdateStepDate<settleDate){
              //查询当前清算天到上一次累计金额更新时间之间的金额
              if(isLiqDate==0){
                  def lastSettleDay = lastUpdateStepDate.format("yyyy-MM-dd HH:mm:ss.SSS")
                  def lastSettleDate = Date.parse('yyyy-MM-dd', lastSettleDay)
                  ftTrade = getStepFeeTotal(srvCode, tradeCode, customerNo, feeSetting.channelCode,settleDate,lastSettleDate,realtimeSettle)
                  lastTotalAmount = lastTotalAmount+ftTrade.get(0).get("total_amount")
              }
              feeStep.updateStepDate = settleDate
              feeStep.lastStepAmount = lastTotalAmount
              feeStep.save(flush:true)
          }
           log.info "new lastTotalAmount is ${lastTotalAmount}"

              def ftTread = getTradeDetails(dbSettle,srvType.srvCode, tradeType.tradeCode, customer.customerNo, channel,settleDate,realtimeSettle)
              if (ftTread) {
                  for (detail in ftTread) {
                      def deAmount = detail.amount
                      BigDecimal deFeeAmount = new BigDecimal("0")
                      log.info "one amount is ${deAmount}"
                      def feeType = 0
                      def feeValue = new BigDecimal("0")
                      def feeMin = new BigDecimal("0")
                      def feeMax = new BigDecimal("0")

                      //查询当前交易之前到今天的累计金额
                      ftTrade = getStepFeeTotal(srvCode, tradeCode, customerNo, feeSetting.channelCode,detail.bill_date.timestampValue(),settleDate,realtimeSettle)
                      def totalAmount=lastTotalAmount+ftTrade.get(0).get("total_amount")
                      log.info "totalAmount is ${totalAmount}"

                      // 阶梯
                      if ((feeStep.step5FeeValue) && (totalAmount >= feeStep.step5From*100)) {
                          feeType = feeStep.step5FeeType
                          feeValue = feeStep.step5FeeValue
                          feeMin = feeStep.step5FeeMin
                          feeMax = feeStep.step5FeeMax
                      } else if ((feeStep.step4FeeValue) && (totalAmount >= feeStep.step4From*100)) {
                          feeType = feeStep.step4FeeType
                          feeValue = feeStep.step4FeeValue
                          feeMin = feeStep.step4FeeMin
                          feeMax = feeStep.step4FeeMax
                      } else if ((feeStep.step3FeeValue) && (totalAmount >= feeStep.step3From*100)) {
                          feeType = feeStep.step3FeeType
                          feeValue = feeStep.step3FeeValue
                          feeMin = feeStep.step3FeeMin
                          feeMax = feeStep.step3FeeMax
                      } else if ((feeStep.step2FeeValue) && (totalAmount >= feeStep.step2From*100)) {
                          feeType = feeStep.step2FeeType
                          feeValue = feeStep.step2FeeValue
                          feeMin = feeStep.step2FeeMin
                          feeMax = feeStep.step2FeeMax
                      } else if ((feeStep.step1FeeValue) && (totalAmount >= feeStep.step1From*100)) {
                          feeType = feeStep.step1FeeType
                          feeValue = feeStep.step1FeeValue
                          feeMin = feeStep.step1FeeMin
                          feeMax = feeStep.step1FeeMax
                      }

                 if(feeValue==null){
                      feeValue=0
                  }

                  log.info "feeType is ${feeType}"
                  log.info "feeValue is ${feeValue}"
                  log.info "feeMin is ${feeMin}"
                  log.info "feeMax is ${feeMax}"

                      // 按笔收
                      if (feeType == 0) {
                          log.info "feeType is 按笔"
                          deFeeAmount = new BigDecimal(feeValue.toString()).multiply(new BigDecimal('100'))
                            //如果手续费率不为0，手续费最小为1分钱
                              if (deFeeAmount <1 && feeValue != 0) {
                                deFeeAmount = 1
                              }

                      // 按流量收
                      } else if (feeType == 1) {
                          log.info "feeType is 按流量"
                          BigDecimal feeAmountFact = new BigDecimal(feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(deAmount.toString()))
                          // 最高最低手续费
                          if ((feeMin != null)&& (feeMin != 0) && (feeMax != null)&&(feeMax != 0)) {
                              deFeeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                          } else if ((feeMin != null)&&feeMin != 0) {
                              deFeeAmount = new BigDecimal(Math.max(feeAmountFact, feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                          } else if ((feeMax != null)&&feeMax != 0) {
                              deFeeAmount = new BigDecimal(Math.min(feeAmountFact, feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString())
                               //如果手续费率不为0，手续费最小为1分钱
                              if (deFeeAmount <1 && feeValue != 0) {
                                deFeeAmount = 1
                              }
                          } else {
                              deFeeAmount = new BigDecimal(feeAmountFact.toString())
                               //如果手续费率不为0，手续费最小为1分钱
                              if (deFeeAmount <1 && feeValue != 0) {
                                deFeeAmount = 1
                              }
                          }
                      }
                      log.info "one feeAmount is ${deFeeAmount}"

                      feeAmount = feeAmount.add(deFeeAmount)
                      updateTrade(dbSettle,feeSetting,deFeeAmount,detail.id)
                  }
              }
              }
              log.info "count feeAmount is ${count},${feeAmount}"
          }

        //计算交易净额
        def netAmount = amount * tradeType.netWeight
        log.info "netAmount is ${netAmount}"
        def liquidate = new FtLiquidate()
        if (realtimeSettle == 1) { //实时结算中的后返手续费从明细中汇总
          feeAmount = postFee
          liquidate.feeType = 1
          liquidate.preFee = 0
          liquidate.postFee = feeAmount
        } else { //非实时结算
          //判断手续费正负值
          if (feeSetting.tradeWeight == 0) {
            feeAmount = -feeAmount
          }
          feeAmount = feeAmount.setScale(0, BigDecimal.ROUND_HALF_UP)
          liquidate.feeType = feeSetting.fetchType
          if (feeSetting.fetchType == 0) {
            liquidate.preFee = feeAmount
            liquidate.postFee = 0
          } else {
            liquidate.preFee = 0
            liquidate.postFee = feeAmount
          }
        }
          log.info "tradeWeight is ${feeSetting.tradeWeight}"
          log.info "fetchType is ${feeSetting.fetchType}"
         log.info "postFee is ${liquidate.postFee}"
          log.info "preFee is ${liquidate.preFee}"
          log.info "liquidate.feeType is ${liquidate.feeType}"
        if (feeAmount == null) {
          log.warn('fee is null, ignore')
          continue
        }

        def liqNo = getLiqNo()
        log.info "liquidate.liqNo is ${liqNo}"
        //生成清算单
        liquidate.srvCode = srvCode
        liquidate.tradeCode = tradeCode
        liquidate.amount = netAmount
        liquidate.transNum = count
        liquidate.customerNo = customerNo
        liquidate.liqDate =settleDate
        liquidate.liquidateNo = liqNo
        liquidate.status = 0
        liquidate.postFootStatus = 0
        liquidate.liqAccNo = ' '
        liquidate.settleType = realtimeSettle
        liquidate.freezeAmount = freezeAmount
        liquidate.channelCode =  feeSetting.channelCode
        def cmdList = null

        if (realtimeSettle == 0) {
          //判断即收还是后返手续费
          if (feeSetting.fetchType == 0) { //即收手续费
            //println "${service.srvAccNo}, ${sysFeeAdvAcc}"
            //把手续费从客户服务帐户扣除到系统应收手续费帐户，如果为负则反向转账
            //实时即收手续费不记帐
            cmdList = accountClientService.buildTransfer(null, service.srvAccNo, sysFeeAdvAcc, feeAmount, 'fee', liqNo, '0', "定时清算即扣手续费")
          } else if (feeSetting.fetchType == 1) { //后返手续费,实时或非实时结算
            //把手续费从服务后返手续费帐户转到系统应收手续费帐户，如果手续费为负则反向
            //实时手续费不记帐
            cmdList = accountClientService.buildTransfer(null, service.feeAccNo, sysFeeAdvAcc, feeAmount, 'fee', liqNo, '0', "定时清算后收手续费")
          }
        }

        liquidate.redo = false
        liquidate.save(failOnError: true)
        log.info "liquidate.save "
        updateTradeLiqNo(liqNo,srvCode,tradeCode,customerNo,channel,feeSetting.channelCode,settleDate,realtimeSettle)

          log.info "FtTrade.executeUpdate "

        //转账
        boolean redo = false //转账失败，是否重做
        try {
          if (cmdList) {
            log.info "account command : ${cmdList.dump()}"
            def transResult = accountClientService.batchCommand(UUID.randomUUID().toString().replaceAll('-', ''), cmdList)
            //throw new RuntimeException('test rollback')
            if (transResult.result != 'true') {
              log.warn("实时转账失败，错误码：${transResult.errorCode},错误信息：${transResult.errorMsg}")
              //帐户余额不足或者账务系统故障需要重新转账
              if (transResult.errorCode == '03' || transResult.errorCode == 'ff') {
                redo = true
              } else {
                log.warn 'account call faile, rollback'
                throw new RuntimeException('account call faile, rollback')
              }
            }
          }
        } catch (IOException e) {
          log.warn('balance trans faile', e)
          redo = true
        }
          log.info "account.executeUpdate "
        //设置重转账标记
        if (redo) {
          liquidate.redo = true
          liquidate.save()
        }
      } catch (Exception e) {
        log.warn('liq error', e)
      }
    }
    log.info 'liquidate success'
  }

    //  实时结算交易 计算手续费
  def calcuFeeUpgrade(feeSetting,channel, srvCode, tradeCode, amount, tradeNum,billDate) {

      def dbSettle =  new groovy.sql.Sql(dataSource_settle)

      BigDecimal feeAmount = new BigDecimal("0")

      log.info "feeModel is ${feeSetting.feeModel}"

      if(feeSetting.feeValue==null){
          feeSetting.feeValue=0
      }
      // 固定费率
      if (feeSetting.feeModel == 0) {

          log.info "feeType is ${feeSetting.feeType}"
          log.info "feeValue is ${feeSetting.feeValue}"
          // 按笔收
          if (feeSetting.feeType == 0) {
              feeAmount = new BigDecimal(feeSetting.feeValue.toString()).multiply(new BigDecimal('100')).multiply(new BigDecimal(tradeNum.toString()))
               //如果手续费率不为0，手续费最小为1分钱
              if (feeAmount <1 && feeSetting.feeValue != 0) {
                feeAmount = 1
              }
              log.info "feeAmount is ${feeAmount}"
          // 按流量收
          } else if (feeSetting.feeType == 1) {
              log.info "feeMin is ${feeSetting.feeMin}"
              log.info "feeMax is ${feeSetting.feeMax}"

              BigDecimal feeAmountFact = new BigDecimal(feeSetting.feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(amount.toString()))
              // 最高最低手续费  四舍五入 保留一位小数
              if ((feeSetting.feeMin != null) && (feeSetting.feeMin != 0) && (feeSetting.feeMax != null) && (feeSetting.feeMax != 0)) {
                  feeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
              } else if ((feeSetting.feeMin != null) && feeSetting.feeMin != 0) {
                  feeAmount = new BigDecimal(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
              } else if ((feeSetting.feeMax != null) && feeSetting.feeMax != 0) {
                  feeAmount = new BigDecimal(Math.min(feeAmountFact, feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                  //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount < 1 && feeSetting.feeValue != 0) {
                    feeAmount = 1
                  }
              } else {
                  feeAmount = new BigDecimal(feeAmountFact.toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                  //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount < 1 && feeSetting.feeValue != 0) {
                    feeAmount = 1
                  }
              }
              log.info "feeAmount is ${feeAmount}"
          // 包流量
          } else if (feeSetting.feeType == 2) {
              log.info "feeType is 包流量"
              log.info "流量限额 is ${feeSetting.feeFlow}(单位：元)"
              log.info "已使用流量 is ${feeSetting.feeUsedFlow}(单位：分)"
              log.info "预付手续费 is ${feeSetting.feePre}(单位：元)"
              log.info "超量费率 is ${feeSetting.feeValue}%"
              log.info "最低手续费 is ${feeSetting.feeMin}(单位：元)"
              log.info "最高手续费 is ${feeSetting.feeMax}(单位：元)"


              def str = ConfigurationHolder.config.grails.opeatorEmail
              def opeatorEmail = str
              def sellerEmail
              def flag = 0
              if (str.indexOf(",") != -1) {
                  def flags = str.split(",")
                  opeatorEmail = flags[0]
                  sellerEmail = flags[1]
              }

              def newFeeUsedFlow = (new BigDecimal(String.valueOf(feeSetting.feeUsedFlow))).add(new BigDecimal(String.valueOf(amount))).longValue()
              def feeFlow = (new BigDecimal(String.valueOf(feeSetting.feeFlow))).multiply(new BigDecimal("100")).longValue()

              if (newFeeUsedFlow <= feeFlow) {
                  feeAmount = 0

                  def perDel = (new BigDecimal(String.valueOf(feeSetting.feeFlow))).multiply(new BigDecimal("80")).compareTo(new BigDecimal(String.valueOf(newFeeUsedFlow)))
                  if ((!feeSetting.isViewPerMail) && feeSetting.isViewPer && (perDel <= 0)) {
                      // 发信
                      def srvName = FtSrvType.findBySrvCode(srvCode)?.srvName
                      sendEmail("/isViewMail/isViewPerMail","包流量费率80%提示",opeatorEmail,[item: feeSetting, srvName: srvName])
                      sendEmail("/isViewMail/isViewPerMail","包流量费率80%提示",sellerEmail,[item: feeSetting, srvName: srvName])
                      feeSetting.isViewPerMail = true
                  }
              } else {
                  BigDecimal feeAmountFact = new BigDecimal(feeSetting.feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(amount.toString()))
                  // 最高最低手续费  四舍五入 保留一位小数
                  if ((feeSetting.feeMin != null)&& (feeSetting.feeMin != 0) &&(feeSetting.feeMax != null)&&  (feeSetting.feeMax != 0)) {
                      feeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                  } else if ((feeSetting.feeMin != null)&&feeSetting.feeMin != 0) {
                      feeAmount = new BigDecimal(Math.max(feeAmountFact, feeSetting.feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                  } else if ((feeSetting.feeMax != null)&& feeSetting.feeMax != 0) {
                      feeAmount = new BigDecimal(Math.min(feeAmountFact, feeSetting.feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                      //如果手续费率不为0，手续费最小为1分钱
                      if (feeAmount < 1 && feeSetting.feeValue != 0) {
                        feeAmount = 1
                      }
                  } else {
                      feeAmount = new BigDecimal(feeAmountFact.toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                      //如果手续费率不为0，手续费最小为1分钱
                      if (feeAmount <1 && feeSetting.feeValue != 0) {
                        feeAmount = 1
                      }
                  }

                  if ((!feeSetting.isViewOverMail) && feeSetting.isViewOver) {
                      // 发信
                      def srvName = FtSrvType.findBySrvCode(srvCode)?.srvName
                      sendEmail("/isViewMail/isViewOverMail","包流量费率超量提示",opeatorEmail,[item: feeSetting, srvName: srvName])
                      sendEmail("/isViewMail/isViewOverMail","包流量费率超量提示",sellerEmail,[item: feeSetting, srvName: srvName])
                      feeSetting.isViewOverMail = true
                  }
              }

              feeSetting.feeUsedFlow = newFeeUsedFlow
              feeSetting.save()
              log.info "手续费 is ${feeAmount}"
          }

      // 阶梯费率
      } else if (feeSetting.feeModel == 2) {
          // 阶梯费率信息
          def feeStep = FtTradeFeeStep.get(feeSetting.id)
          if(feeStep==null){
            return null
          }
          // 阶梯费率该包年（月）周期内金额总流量      ;

          if(feeSetting.firstLiqDate==null){
            return null
          }

//          def settleDate = Calendar.getInstance()
            def settleDay = billDate.format("yyyy-MM-dd HH:mm:ss.SSS")
            def settleDate = Date.parse('yyyy-MM-dd', settleDay)
              Calendar calendar = Calendar.getInstance()
              calendar.setTime(feeSetting.firstLiqDate)
              def calBegin = Calendar.getInstance()
              calBegin.setTime(calendar.getTime())

          log.info "billDate is ${billDate}"
          log.info "settleDay is ${settleDay}"
          log.info "settleDate is ${settleDate}"
          log.info "firstLiqDate is ${feeSetting.firstLiqDate}"

          if(settleDate<feeSetting.firstLiqDate){
            return null
          }

          log.info "calBeginS is ${calBegin.getTime()}"
          if(feeSetting.packLen==null){
            feeSetting.packLen=1
          }
          while (calendar.getTime() <= settleDate) {
              calBegin.setTime(calendar.getTime())
              if (feeSetting.packType == 0) {
                  // 包月
                  calendar.add(Calendar.MONTH, feeSetting.packLen)
              } else {
                  // 包年
                  calendar.add(Calendar.YEAR, feeSetting.packLen)
              }
          }
          log.info "calBeginE is ${calBegin.getTime()}"


           updateTradeChannel(srvCode, tradeCode,feeSetting.customerNo,channel,feeSetting.channelCode,settleDate)

           log.info "srvCode is ${srvCode}"
           log.info "tradeCode is ${tradeCode}"
           log.info "customerNo is ${feeSetting.customerNo}"
          log.info "channelCode is ${feeSetting.channelCode}"
          def totalAmount =  feeStep.lastStepAmount
          def lastUpdateStepDate =  feeStep.updateStepDate
          def isLiqDate=0
          if(settleDate==calBegin.getTime()){
              totalAmount=0
              isLiqDate=1
          }
          def ftTrade
          //如果没有记录之前的累计金额
          if(!lastUpdateStepDate){
              //查询今天前一天到周期起始日期的累计金额
              if(isLiqDate==0){
                  ftTrade = getStepFeeTotal(srvCode, tradeCode, feeSetting.customerNo, feeSetting.channelCode,settleDate,calBegin.getTime(),1)
                  totalAmount = ftTrade.get(0).get("total_amount")
              }
              feeStep.updateStepDate= settleDate
              feeStep.lastStepAmount= totalAmount
              feeStep.save(flush:true)

          }//如果累计金额的更新时间小于今天，说明金额只累计到今天前，需要查出今天前一天到上次累计金额更新时间之间的金额
          else if(lastUpdateStepDate<settleDate){
              //查询今天前一天到上次累计金额更新时间之间的金额
              if(isLiqDate==0){
                  def lastSettleDay = lastUpdateStepDate.format("yyyy-MM-dd HH:mm:ss.SSS")
                  def lastSettleDate = Date.parse('yyyy-MM-dd', lastSettleDay)
                  ftTrade = getStepFeeTotal(srvCode, tradeCode, feeSetting.customerNo, feeSetting.channelCode,settleDate,lastSettleDate,1)
                  totalAmount = totalAmount+ftTrade.get(0).get("total_amount")
              }
              feeStep.updateStepDate = settleDate
              feeStep.lastStepAmount = totalAmount
              feeStep.save(flush:true)
          }
          //查询当前交易之前到今天的累计金额
          ftTrade = getStepFeeTotal(srvCode, tradeCode, feeSetting.customerNo, feeSetting.channelCode,billDate,settleDate,1)
          totalAmount=totalAmount+ftTrade.get(0).get("total_amount")
          log.info "totalAmount is ${totalAmount}"
          def feeType = 0
          def feeValue = new BigDecimal("0")
          def feeMin = new BigDecimal("0")
          def feeMax = new BigDecimal("0")

          // 阶梯
          if ((feeStep.step5FeeValue) && (totalAmount >= feeStep.step5From*100)) {
              feeType = feeStep.step5FeeType
              feeValue = feeStep.step5FeeValue
              feeMin = feeStep.step5FeeMin
              feeMax = feeStep.step5FeeMax
          } else if ((feeStep.step4FeeValue) && (totalAmount >= feeStep.step4From*100)) {
              feeType = feeStep.step4FeeType
              feeValue = feeStep.step4FeeValue
              feeMin = feeStep.step4FeeMin
              feeMax = feeStep.step4FeeMax
          } else if ((feeStep.step3FeeValue) && (totalAmount >= feeStep.step3From*100)) {
              feeType = feeStep.step3FeeType
              feeValue = feeStep.step3FeeValue
              feeMin = feeStep.step3FeeMin
              feeMax = feeStep.step3FeeMax
          } else if ((feeStep.step2FeeValue) && (totalAmount >= feeStep.step2From*100)) {
              feeType = feeStep.step2FeeType
              feeValue = feeStep.step2FeeValue
              feeMin = feeStep.step2FeeMin
              feeMax = feeStep.step2FeeMax
          } else if ((feeStep.step1FeeValue) && (totalAmount >= feeStep.step1From*100)) {
              feeType = feeStep.step1FeeType
              feeValue = feeStep.step1FeeValue
              feeMin = feeStep.step1FeeMin
              feeMax = feeStep.step1FeeMax
          }

                 if(feeValue==null){
                      feeValue=0
                  }
                 if(feeMin==null){
                      feeMin=0
                  }
                  if(feeMax==null){
                      feeMax=0
                  }
           log.info "feeType is ${feeType}"
           log.info "feeValue is ${feeValue}"
           log.info "feeMin is ${feeMin}"
          log.info "feeMax is ${feeMax}"
          // 按笔收
          if (feeType == 0) {
              feeAmount = new BigDecimal(feeValue.toString()).multiply(new BigDecimal('100')).multiply(new BigDecimal(tradeNum.toString()))
                //如果手续费率不为0，手续费最小为1分钱
                 if (feeAmount <1 && feeValue != 0) {
                    feeAmount = 1
                  }

          // 按流量收
          } else if (feeType == 1) {
              BigDecimal feeAmountFact = new BigDecimal(feeValue.toString()).divide(new BigDecimal('100')).multiply(new BigDecimal(amount.toString()))
              // 最高最低手续费  四舍五入 保留一位小数
              if ((feeMin != null)&&(feeMin != 0) &&(feeMax != null)&& (feeMax != 0)) {
                  feeAmount = new BigDecimal(Math.min(Math.max(feeAmountFact, feeMin.toBigDecimal().multiply(new BigDecimal("100"))), feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
              } else if ((feeMin != null)&&feeMin != 0) {
                  feeAmount = new BigDecimal(Math.max(feeAmountFact, feeMin.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
              } else if ((feeMax != null)&&feeMax != 0) {
                  feeAmount = new BigDecimal(Math.min(feeAmountFact, feeMax.toBigDecimal().multiply(new BigDecimal("100"))).toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                   //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount <1 && feeValue != 0) {
                    feeAmount = 1
                  }
              } else {
                  feeAmount = new BigDecimal(feeAmountFact.toString()).setScale(0, BigDecimal.ROUND_HALF_UP)
                   //如果手续费率不为0，手续费最小为1分钱
                  if (feeAmount <1 && feeValue != 0) {
                    feeAmount = 1
                  }
              }
          }

          log.info "feeAmount is ${feeAmount}"
      }

      //判断手续费正负值
      if (feeSetting.tradeWeight == 0) {
        feeAmount = -feeAmount
      }
      return feeAmount
  }

   def getTradeGroups(settleDay) {
        def sql = new Sql(dataSource_settle)
        def query = """select count(*) co,
                       sum(amount) amount,
                       sum(post_fee) post_fee,
                       sum(freeze_amount) freeze_amount,
                       customer_no,
                       srv_code,
                       trade_code,
                       to_char(bill_date, 'yyyy-mm-dd') bill_date,
                       realtime_settle,
                       channel_code
                  from ft_trade
                 where (realtime_settle = 0 or (realtime_settle = 1 and fee_type = 1))
                   and liquidate_no is null
                   and bill_date <= to_date('${settleDay}','yyyy-mm-dd')
                 group by customer_no,
                          srv_code,
                          trade_code,
                          to_char(bill_date, 'yyyy-mm-dd'),
                          realtime_settle,
                          channel_code
                 order by
                       bill_date asc
                         """

    def tradeSums = sql.rows(query)
        return tradeSums
    }


    def updateTrade(dbSettle,feeSetting,deFeeAmount,id) {
        //判断手续费正负值   ,回写手续费
        def preFee=0
        def postFee=0
        def fee=deFeeAmount
        if (feeSetting.tradeWeight == 0) {
            fee = -deFeeAmount
          }
         fee = fee.setScale(0, BigDecimal.ROUND_HALF_UP)
          if (feeSetting.fetchType == 0) {
            preFee = fee
          } else {
            postFee = fee
          }

        def channelSql
        if(feeSetting.channelCode){
           channelSql=" ,channel_code_cal='"+feeSetting.channelCode+"'"
        } else{
           channelSql=" ,channel_code_cal=null"
        }
        def sql = "update ft_trade set post_fee='"+postFee +"',pre_fee='"+preFee+"'" +channelSql+" where id='"+id+"'"
        dbSettle.executeUpdate(sql)
    }


    def updateTradeLiqNo(liqNo,srvCode,tradeCode,customerNo,oldChannel,newChannel,settleDate,realtimeSettle) {
       def dbSettle = new Sql(dataSource_settle)
       def settleDay = settleDate.format("yyyy-MM-dd")
       def settleDayNext = (settleDate+1).format("yyyy-MM-dd")
       def thisDay = new Date().format("yyyy-MM-dd HH:mm:ss.SSS")
        def channelSetSql
        if(newChannel){
           channelSetSql=" ,t.channel_code='"+newChannel+"'"
        } else{
           channelSetSql=" ,t.channel_code=null"
        }
        def channelWhereSql
        if(oldChannel){
           channelWhereSql=" and t.channel_code='"+oldChannel+"'"
        } else{
           channelWhereSql=" and t.channel_code is null"
        }
        def realTimeSql
        if(realtimeSettle==0){
            realTimeSql=" and t.realtime_settle=0"
        } else{
           realTimeSql=" and t.realtime_settle=1 and fee_type=1 "
        }
        def updatsSql="""update ft_trade t
              set t.liquidate_no='"""+liqNo+"""'
              ,t.liq_date=to_timestamp('"""+thisDay+"""','yyyy-mm-dd hh24:mi:ss.ff')""" +
             """  where
               t.bill_date < to_date('"""+settleDayNext+"""','yyyy-mm-dd')
           and t.bill_date >= to_date('"""+settleDay+"""','yyyy-mm-dd')
           and t.trade_code = '"""+tradeCode+"""'
           and t.srv_code = '"""+srvCode+"""'
           and t.customer_no = '"""+customerNo+"""'
           and t.liquidate_no is null """ +realTimeSql  +channelWhereSql
        log.info "TradeLiqNo updatsSql is ${updatsSql}"
        dbSettle.executeUpdate(updatsSql)
    }


    def updateTradeChannel(srvCode,tradeCode,customerNo,oldChannel,newChannel,settleDate) {
       def dbSettle = new Sql(dataSource_settle)
       def settleDay = settleDate.format("yyyy-MM-dd")
       def settleDayNext = (settleDate+1).format("yyyy-MM-dd")
        def channelSetSql
        if(newChannel){
           channelSetSql=" t.channel_code_cal='"+newChannel+"'"
        } else{
           channelSetSql=" t.channel_code_cal=null"
        }
        def channelWhereSql
        if(oldChannel){
           channelWhereSql=" and t.channel_code='"+oldChannel+"'"
        } else{
           channelWhereSql=" and t.channel_code is null"
        }

        def updatsSql="""update ft_trade t
              set """ + channelSetSql +
             """  where
               t.bill_date < to_date('"""+settleDayNext+"""','yyyy-mm-dd')
           and t.bill_date >= to_date('"""+settleDay+"""','yyyy-mm-dd')
           and t.trade_code = '"""+tradeCode+"""'
           and t.srv_code = '"""+srvCode+"""'
           and t.customer_no = '"""+customerNo+"""'
           and t.liquidate_no is null """ +channelWhereSql
        log.info "updateTradeChannel updatsSql is ${updatsSql}"
        dbSettle.executeUpdate(updatsSql)
    }
   /**
     * 获取手续费设置
     * @param srvType 所属业务类型
     * @param tradeType 所属交易类型
     * @param customerNo 所属客户号
     * @param channelCode 费率通道
     * @param settleDate Date 交易入账日期
     * @return  FtTradeFee
     * @author guonan
     *
     */
  def getFeeSetting(srvType, tradeType, customerNo, channelCode,settleDate) {
      def params =[:]
      params.order = "desc"
      params.sort = "dateEnd"
      params.channelCode = channelCode
      params.tradeType = tradeType
      params.customerNo = customerNo
      params.srv = srvType
      params.dateEnd = settleDate
      params.dateBegin = settleDate
      def query = {
            if (params.channelCode) {
                eq ('channelCode',params.channelCode)
            }else{
                isNull("channelCode")
            }
            if (params.tradeType) {
                eq('tradeType', params.tradeType)
            }
            if (params.srv) {
                eq('srv', params.srv)
            }
            if (params.customerNo) {
                eq('customerNo', params.customerNo)
            }
            if (params.dateBegin) {
               ge('dateEnd', params.dateEnd)
               le('dateBegin', params.dateBegin)
            }
            order("dateEnd", "desc")
        }
        def tradeFeelist = FtTradeFee.createCriteria().list(params, query)
      if(tradeFeelist)
        return tradeFeelist?.first()
      else
        return null
  }

   /**
     * 获取交易详细信息
     * @param srvCode 服务类型编码
     * @param tradeCode 交易类型编码
     * @param customerNo 所属客户号
     * @param channelCode 费率通道
     * @param settleDate Date 交易入账日期
     * @param realTime  是否为实时1为实时，0为非实时
     * @return  List<FtTrade>
     * @author guonan
     *
     */

   def getTradeDetails(dbSettle,srvCode, tradeCode, customerNo, channelCode,settleDate,realTime) {
        def channelSql
        if(channelCode){
            channelSql=" and channel_code='"+channelCode+"'"
        } else{
           channelSql=" and channel_code is null"
        }
        def realTimeSql
        if(realTime==0){
            realTimeSql=" and realtime_settle=0"
        } else{
           realTimeSql=" and realtime_settle=1"
        }
       def settleDay = settleDate.format("yyyy-MM-dd")
       def settleDayNext = (settleDate+1).format("yyyy-MM-dd")
        def sql = """
        select
               id
               ,amount
               ,bill_date
          from ft_trade
         where 2>1
           and liquidate_no is null
           and bill_date < to_date('"""+settleDayNext+"""','yyyy-mm-dd')
           and bill_date >= to_date('"""+settleDay+"""','yyyy-mm-dd')
           and trade_code = '"""+tradeCode+"""'
           and customer_no = '"""+customerNo+"""'
           and srv_code = '"""+srvCode+"""'""" + channelSql+realTimeSql+
         """ order by
            bill_date asc
         """
       log.info "getTradeDetails  is ${sql}"
        def db = new Sql(dataSource_settle)
        return db.rows(sql)
    }


    /**
     * 阶梯费率计算时获取本记算周期已经清算过的总金额
     * @param srvCode 服务类型编码
     * @param tradeCode 交易类型编码
     * @param customerNo 所属客户号
     * @param channelCode 费率通道
     * @param settleDate  费率阶梯计算周期中的当前日期
     * @param cycleStart  费率阶梯计算周期起始日期
     * @param realTime
     * @return  FtTrade
     * @author guonan
     *
     */

   def getStepFeeTotal(srvCode, tradeCode, customerNo, channelCode,settleDate,cycleStart,realTime) {
        def sql = new Sql(dataSource_settle)
        def settleDay = settleDate.format("yyyy-MM-dd HH:mm:ss.SSS")
        def cycleStartDay = cycleStart.format("yyyy-MM-dd HH:mm:ss.SSS")
        def channelSql
        if(channelCode){
            channelSql=" and channel_code_cal='"+channelCode+"'"
        } else{
           channelSql=" and channel_code_cal is null"
        }
        def query = """
        select
               nvl(sum(amount),0) as total_amount,
               count(*) co
          from ft_trade
         where 2>1
           and bill_date >= to_timestamp('${cycleStartDay}','yyyy-mm-dd hh24:mi:ss.ff')
           and bill_date < to_timestamp('${settleDay}','yyyy-mm-dd hh24:mi:ss.ff')
           and trade_code = '${tradeCode}'
           and customer_No = '${customerNo}'
           and srv_Code = '${srvCode}'
         """ +channelSql
        log.info "getStepFeeTotal  is ${query}"
        return sql.rows(query)
    }

  def getLiqNo() {
    def sql = new Sql(dataSource_settle)
    def seq = sql.firstRow('select seq_liquidate.nextval from dual').nextval.toString()
    if (seq.length() > 5) {
      seq = seq.substring(seq.length() - 5, seq.length() - 1)
    } else {
      seq = seq.padLeft(5, '0')
    }
    return 'L' + new Date().format('yyyyMMddHHmmss') + seq
  }

    // 包流量发信提醒
    def sendEmail(String mailtemplate, String mailTitle, String target, model) throws Exception {
        def http = new HTTPBuilder(ConfigurationHolder.config.emis.serverUrl)

        String txt = grailsTemplateEngineService.renderView(mailtemplate, model)

        mailTitle = URLEncoder.encode(mailTitle, 'GBK')
        def args = [to: target, subject: mailTitle, body: URLEncoder.encode(txt, 'GBK'), charset: 'GBK']
        log.info args
        http.request(POST, JSON) {req ->
            requestContentType = ContentType.URLENC
            uri.path = "inAccess/email"
            body = args
            req.getParams().setParameter("http.connection.timeout", new Integer(60000));
            req.getParams().setParameter("http.socket.timeout", new Integer(60000));
            response.success = { resp, reader ->

                return reader
            }
            response.failure = { resp ->
                log.error resp.statusLine

                throw new Exception('request error')
            }
        }
    }
}
