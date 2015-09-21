package settle

class FtTradeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def messageService

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.sort = params.sort ? params.sort : "billDate"
        params.order = params.order ? params.order : "desc"
         def backflag
        def query = {
            if (params.customerNo) {
                like('customerNo','%'+params.customerNo+'%')
            }
            if (params.liquidateNo) {
                like('liquidateNo','%'+params.liquidateNo+'%')
            }
            if (params.liqNo) {
                backflag=1
                eq('liquidateNo',params.liqNo)
            }
            if (params.startDateCreated) {
            ge('billDate', Date.parse('yyyy-MM-dd', params.startDateCreated))
             }
             if (params.endDateCreated) {
            lt('billDate', Date.parse('yyyy-MM-dd', params.endDateCreated) + 1)
             }
        }
        def total = FtTrade.createCriteria().count(query)
        def gwOrderList = FtTrade.createCriteria().list(params, query)
        [ftTradeInstanceList: gwOrderList, ftTradeInstanceTotal: total,backflag:backflag]
    }

    def create = {
        def ftTradeInstance = new FtTrade()
        ftTradeInstance.properties = params
        return [ftTradeInstance: ftTradeInstance]
    }

    def msg = {
        return
    }
    def add = {
        def amount = params.amount.toString().toLong()
        params.amount =  amount
        messageService.onMessage(params)
        flash.message = "添加成功"
        redirect(action: "list")
    }

    def save = {
        def ftTradeInstance = new FtTrade(params)
        if (ftTradeInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), ftTradeInstance.id])}"
            redirect(action: "show", id: ftTradeInstance.id)
        }
        else {
            render(view: "create", model: [ftTradeInstance: ftTradeInstance])
        }
    }

    def show = {
        def ftTradeInstance = FtTrade.get(params.id)
        if (!ftTradeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
            redirect(action: "list")
        }
        else {
            [ftTradeInstance: ftTradeInstance]
        }
    }

    def edit = {
        def ftTradeInstance = FtTrade.get(params.id)
        if (!ftTradeInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [ftTradeInstance: ftTradeInstance]
        }
    }

    def update = {
        def ftTradeInstance = FtTrade.get(params.id)
        if (ftTradeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (ftTradeInstance.version > version) {

                    ftTradeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'ftTrade.label', default: 'FtTrade')] as Object[], "Another user has updated this FtTrade while you were editing")
                    render(view: "edit", model: [ftTradeInstance: ftTradeInstance])
                    return
                }
            }
            ftTradeInstance.properties = params
            if (!ftTradeInstance.hasErrors() && ftTradeInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), ftTradeInstance.id])}"
                redirect(action: "show", id: ftTradeInstance.id)
            }
            else {
                render(view: "edit", model: [ftTradeInstance: ftTradeInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def ftTradeInstance = FtTrade.get(params.id)
        if (ftTradeInstance) {
            try {
                ftTradeInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftTrade.label', default: 'FtTrade'), params.id])}"
            redirect(action: "list")
        }
    }
}
