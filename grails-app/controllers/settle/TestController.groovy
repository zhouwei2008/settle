package settle

class TestController {

   def settleClientService
  def liquidateService

  def settleService

  def withdrawService

    def index = { }
    def doTest1 = {

        def amount = 5000
        def channel = '1106'
        settleClientService.test1(amount, channel)
        render (view: '/index')
    }

    def doTest2 = {
        Random random = new Random();
        def seqNo = 101202170030000 + Math.abs(random.nextInt()%10000)
        settleClientService.trade('online', 'refund', '100000000001524',18000, seqNo, '2012-02-18 08:12:23.125', '2012-02-18 08:12:23.125', '1106')
        render (view: '/index')
    }

    def doTest3 = {
        Random random = new Random();
        def seqNo = 101202170030000 + Math.abs(random.nextInt()%10000)
        settleClientService.trade('online', 'refund', '100000000001524',18000, seqNo, '2012-02-18 08:12:23.125', '2012-02-18 08:12:23.125', '1106')
        render (view: '/index')
    }



    def doTestx = {

        println "hihi,guonan is best"
            liquidateService.run()
            settleService.run()
            withdrawService.run()

        println "ok,you are right"
        render (view: '/index')
    }

    def doTestTrade = {

        def amount = 640000
        def channel = '1106'
        settleClientService.testTrade(amount, channel)
        render (view: '/index')
    }
}
