package settle

import org.codehaus.groovy.grails.commons.ConfigurationHolder


class LiquidateJob {
  def static cron = ConfigurationHolder.config.liquidate.job.cron?ConfigurationHolder.config.liquidate.job.cron.replace("\"",""):"0 10 0 * * ?"
  static triggers = {
    //simple name:'testTrigger', startDelay:0, repeatInterval: 300000000, repeatCount: 10
    cron name: 'liqTrigger', cronExpression: cron     //run at 00:10 every day
//    cron name: 'liqTrigger', cronExpression: "0 43 9 * * ?"
  }
  def group = "LiqGroup"

  def concurrent = false

  def liquidateService

  def settleService

  def withdrawService

  def execute() {
    liquidateService.run()
    settleService.run()
    withdrawService.run()
  }
}
