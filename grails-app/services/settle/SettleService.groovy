package settle

import groovy.sql.Sql
import java.sql.Timestamp
import boss.BoInnerAccount
import ismp.CmCustomerBankAccount
import ismp.CmCustomer
import ismp.TradeWithdrawn

class SettleService {

  static transactional = true

  def dataSource_settle
  def accountClientService

  def run() {
    log.info 'start footSetting'
    def sql = new Sql(dataSource_settle)
    //更新上次结算日期为空的初始化结算设置，更新下次结算日期
    def initFootSettings = FtSrvFootSetting.findAllByFootTypeNotEqualAndNextFootDateIsNull(0)
    for (footSetting in initFootSettings) {
      def nextFootDate = getNextFootDate(footSetting)
      if (nextFootDate) {
        footSetting.nextFootDate = nextFootDate
        footSetting.save(flush: true, failOnError: true)
      }
    }

    //结算日期
    def settleDate = new Date()
    settleDate.clearTime()
    //获得需要结算的结算设置
    def footSettings = FtSrvFootSetting.findAllByFootTypeNotEqualAndNextFootDateLessThanEquals(0, settleDate)
    log.info "foot records : ${footSettings?.size()}"
    for (footSetting in footSettings) {
      //获取风险预存期之前的清算单
      def mortDay = footSetting.nextFootDate
      mortDay.clearTime()
      //结算发生时，结算前一天的交易再减去风险预存期的天数
      mortDay = mortDay - (footSetting.mortDay + 1)
      println "mortDay:${footSetting.mortDay},mortDate:${mortDay}"
      def liqQuery = "select count(*) count, sum(amount) amount,sum(trans_num) transNum, sum(pre_fee) preFee, sum(post_fee) postFee, sum(freeze_amount) freezeAmount from ft_liquidate l where srv_code=${footSetting.srv.srvCode} and trade_code=${footSetting.tradeType.tradeCode} and customer_no=${footSetting.customerNo} and status=0 and settle_type=0 and liq_date<=${new Timestamp(mortDay.time)}"
      def liqResult = sql.firstRow(liqQuery)
      log.info "sum liq for customNo=${footSetting.customerNo},srv_code=${footSetting.srv.srvCode},trade_code=${footSetting.tradeType.tradeCode},result:amount=${liqResult.amount},transNum=${liqResult.transNum},preFee=${liqResult.preFee},postFee=${liqResult.postFee}, freezeAmount=${liqResult.freezeAmount},mortDay=${mortDay}, footSetting.mortDay=${footSetting.mortDay}"
      if (liqResult.count == 0) {
        log.info "no liquidate record, skip. "
        updateNextFootDate(footSetting)
        continue
      }
      //检查总净额减去即收手续费总额是否达到最低结算金额,另：最低结算金额设置为0表示不限制，对于净额为负的结算交易都应设为0
      long footAmount = footSetting.footAmount * 100
      if (footAmount != 0 && liqResult.amount - liqResult.preFee < footAmount) {
        log.info "total Amount less than footAmount, skip. total Amount:${liqResult.amount},total preFee:${liqResult.preFee},footAmount:${footAmount}"
        updateNextFootDate(footSetting)
        continue
      }
      //生成结算单
      def footNo = getFootNo()
      def foot = new FtFoot()
      foot.srvCode = footSetting.srv.srvCode
      foot.tradeCode = footSetting.tradeType.tradeCode
      foot.customerNo = footSetting.customerNo
      foot.type = 0
      foot.feeType = 0
      foot.preFee = liqResult.preFee
      foot.postFee = liqResult.postFee
      foot.transNum = liqResult.transNum
      foot.amount = liqResult.amount
      foot.footNo = footNo
      foot.footDate = new Date()
      foot.checkStatus = 0
      foot.freezeAmount = liqResult.freezeAmount
      foot.autoCheck = (footSetting.type == 0) ? 1 : 0
      foot.autoWithdraw = footSetting.withdraw
      foot.withdraw = 0
      foot.save()

      log.info "create foot: ${foot.dump()}"
      //更新清算单的结算状态和结算批次号
      FtLiquidate.executeUpdate("update FtLiquidate set footNo='${footNo}',status=1 where srvCode='${footSetting.srv.srvCode}' and tradeCode='${footSetting.tradeType.tradeCode}' and customerNo='${footSetting.customerNo}' and status=0 and settleType=0 and liqDate<=?", [mortDay])

      //更新结算设置的上次结算时间和下次结算时间
      footSetting.lastFootDate = settleDate
      def nextFootDate = getNextFootDate(footSetting)
      if (nextFootDate) {
        footSetting.nextFootDate = nextFootDate
      }
      footSetting.save()
    }
    log.info 'foot success'

  }

  //生成结算单号

  def getFootNo() {
    def sql = new Sql(dataSource_settle)
    def seq = sql.firstRow('select seq_foot.nextval from dual').nextval.toString()
    if (seq.length() > 5) {
      seq = seq.substring(seq.length() - 5, seq.length() - 1)
    } else {
      seq = seq.padLeft(5, '0')
    }
    return 'A' + new Date().format('yyyyMMddHHmmss') + seq
  }

  //更新下次结算时间

  def updateNextFootDate(FtSrvFootSetting footSetting) {
    def settleDate = new Date()
    settleDate.clearTime()
    footSetting.lastFootDate = settleDate
    def nextFootDate = getNextFootDate(footSetting)
    if (nextFootDate) {
      footSetting.nextFootDate = nextFootDate
      footSetting.save()
    }
  }

  /**
   * 根据本次结算时间和结算设置获得下次结算时间，每次结算完以后会更新lastFootDate，本方法根据lastFootDate和结算周期设置计算nextFootDate，
   * 并返回
   * @param footSetting 结算设置
   */
  def getNextFootDate(FtSrvFootSetting footSetting) {
    def today = new Date()
    today.clearTime()

    if (footSetting.footType == 1) { //日结，每日结
      if (footSetting.nextFootDate == null) {
        return today
      } else {
        return today + 1
      }
    } else if (footSetting.footType == 2) { //周结，每周的固定日期结，根据结算表达式确定，如：1,3为每周的周一和周三结
      def lastDate = footSetting.lastFootDate == null ? today - 1 : footSetting.lastFootDate
      def days = footSetting.footExpr.split(',')

      //把周日转到第一个，日历是以周日开始的
      days = days.collect {
        ++it
      }
      def lastDay = days[days.size() - 1]
      if (lastDay == '8') {
        //把8挪到前面变成1，去掉8
        days.add(0, '1')
        days.remove(days.size() - 1)
      }

      //表达式中的第一个日期
      def firstExpDay = null

      for (int i = 0; i < days.size(); i++) {
        if (!days[i].isInteger()) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }
        def eachExpDay = days[i].toInteger()
        if (eachExpDay <= 0 || eachExpDay > 7) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }

        if (firstExpDay == null) {
          firstExpDay = eachExpDay
        }

        def nextDateByExp = new GregorianCalendar()
        nextDateByExp.setTime(lastDate)
        nextDateByExp.set(Calendar.DAY_OF_WEEK, eachExpDay)

        def nextDate = nextDateByExp.time
        nextDate.clearTime()
        if (nextDate.time > lastDate.time) { //返回下一个大于当前结算日期的日期
          return nextDate
        } else if (i == days.size() - 1) {  //如果遍历完表达式，没有大于结算日期的，循环到第一个表达式日期，周加一
          nextDateByExp.add(Calendar.WEEK_OF_YEAR, 1)
          nextDateByExp.set(Calendar.DAY_OF_WEEK, firstExpDay)

          return nextDateByExp.time
        }
      }
      return null
    } else if (footSetting.footType == 3) { //月结，每月的固定日期结，根据结算表达式确定，如: 1,15为每月的1日和15日结。如果填写的日期超过本月最大日期，按照本月最大日期算
      def lastDate = footSetting.lastFootDate == null ? today - 1 : footSetting.lastFootDate
      def days = footSetting.footExpr.split(',')

      //表达式中的第一个日期
      def firstExpDay = null

      for (int i = 0; i < days.length; i++) {
        if (!days[i].isInteger()) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }
        def eachExpDay = days[i].toInteger()
        if (eachExpDay <= 0 || eachExpDay > 31) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }
        if (firstExpDay == null) {
          firstExpDay = eachExpDay
        }

        def nextDateByExp = new GregorianCalendar()
        nextDateByExp.setTime(lastDate)
        //判断本月最大日期是多少
        def maxDay = nextDateByExp.getActualMaximum(Calendar.DAY_OF_MONTH)
        //实际日期为较小的那个值
        def actualDay = Math.min(maxDay, eachExpDay)
        nextDateByExp.set(Calendar.DAY_OF_MONTH, actualDay)

        def nextDate = nextDateByExp.time
        nextDate.clearTime()
        if (nextDate.time > lastDate.time) { //返回下一个大于当前结算日期的日期
          return nextDate
        } else if (i == days.length - 1) {  //如果遍历完表达式，没有大于结算日期的，循环到第一个表达式日期，月份加一
          nextDateByExp.add(Calendar.MONTH, 1)

          maxDay = nextDateByExp.getActualMaximum(Calendar.DAY_OF_MONTH)
          //实际日期为较小的那个值
          actualDay = Math.min(maxDay, firstExpDay)
          nextDateByExp.set(Calendar.DAY_OF_MONTH, actualDay)

          return nextDateByExp.time
        }
      }
      return null
    } else if (footSetting.footType == 4) { //年结，每年的几月份结，表达式为: 3.15,6.15,9.15,12.15  。表示每年的3月15，6月15，9月15，12月15结
      def lastDate = footSetting.lastFootDate == null ? today - 1 : footSetting.lastFootDate
      def months = footSetting.footExpr.split(',')

      //表达式中的第一个日期
      def firstExpMon = null
      def firstExpDay = null

      for (int i = 0; i < months.length; i++) {
        //计算表达式中的月和日并判断正确性
        def monDayArray = months[i].split('\\.')

        if (monDayArray.length != 2) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }
        def monExp = monDayArray[0]
        def dayExp = monDayArray[1]
        if (!monExp.isInteger() || !dayExp.isInteger()) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }
        monExp = monExp.toInteger() - 1
        dayExp = dayExp.toInteger()
        if (monExp < 0 || monExp > 11 || dayExp < 1 || dayExp > 31) {
          log.warn("footExpr is wrong!${footSetting.dump()}")
          continue
        }

        if (firstExpDay == null) {
          firstExpMon = monExp
          firstExpDay = dayExp
        }

        def nextDateByExp = new GregorianCalendar()
        nextDateByExp.setTime(lastDate)
        nextDateByExp.set(Calendar.MONTH, monExp)
        //判断本月最大日期是多少
        def maxDay = nextDateByExp.getActualMaximum(Calendar.DAY_OF_MONTH)
        //实际日期为较小的那个值
        def actualDay = Math.min(maxDay, dayExp)
        nextDateByExp.set(Calendar.DAY_OF_MONTH, actualDay)

        def nextDate = nextDateByExp.time
        nextDate.clearTime()
        if (nextDate.time > lastDate.time) { //返回下一个大于当前结算日期的日期
          return nextDate
        } else if (i == months.length - 1) {  //如果遍历完表达式，没有大于结算日期的，循环到第一个表达式日期，年份加一
          nextDateByExp.add(Calendar.YEAR, 1)

          nextDateByExp.set(Calendar.MONTH, firstExpMon)

          maxDay = nextDateByExp.getActualMaximum(Calendar.DAY_OF_MONTH)
          //实际日期为较小的那个值
          actualDay = Math.min(maxDay, firstExpDay)
          nextDateByExp.set(Calendar.DAY_OF_MONTH, actualDay)

          return nextDateByExp.time
        }
      }
      return null
    }
  }


}