package settle

class JobController {

  def liquidateService
  def settleService
  def withdrawService

  def index = { }

  def runLiq = {
    liquidateService.run()
    render 'liquidate success'
  }
  def runFoot = {
    settleService.run()
    render 'settle success'
  }

  def run = {
    liquidateService.run()
    settleService.run()
    withdrawService.run()
    render 'run success'
  }
}
