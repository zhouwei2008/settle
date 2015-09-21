package settle

class FtLiquidateController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.sort = params.sort ? params.sort : "liqDate"
        params.order = params.order ? params.order : "desc"
         def query = {
            if (params.customerNo) {
                like('customerNo','%'+params.customerNo+'%')
            }
            if (params.liquidateNo) {
                like('liquidateNo','%'+params.liquidateNo+'%')
            }
            if (params.startDateCreated) {
            ge('liqDate', Date.parse('yyyy-MM-dd', params.startDateCreated))
             }
             if (params.endDateCreated) {
            lt('liqDate', Date.parse('yyyy-MM-dd', params.endDateCreated) + 1)
             }
        }
        def total = FtLiquidate.createCriteria().count(query)
        def gwOrderList = FtLiquidate.createCriteria().list(params, query)
        [ftLiquidateInstanceList: gwOrderList, ftLiquidateInstanceTotal: total]
    }

    def create = {
        def ftLiquidateInstance = new FtLiquidate()
        ftLiquidateInstance.properties = params
        return [ftLiquidateInstance: ftLiquidateInstance]
    }

    def save = {
        def ftLiquidateInstance = new FtLiquidate(params)
        if (ftLiquidateInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), ftLiquidateInstance.id])}"
            redirect(action: "show", id: ftLiquidateInstance.id)
        }
        else {
            render(view: "create", model: [ftLiquidateInstance: ftLiquidateInstance])
        }
    }

    def show = {
        def ftLiquidateInstance = FtLiquidate.get(params.id)
        if (!ftLiquidateInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
            redirect(action: "list")
        }
        else {
            [ftLiquidateInstance: ftLiquidateInstance]
        }
    }

    def edit = {
        def ftLiquidateInstance = FtLiquidate.get(params.id)
        if (!ftLiquidateInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [ftLiquidateInstance: ftLiquidateInstance]
        }
    }

    def update = {
        def ftLiquidateInstance = FtLiquidate.get(params.id)
        if (ftLiquidateInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (ftLiquidateInstance.version > version) {

                    ftLiquidateInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'ftLiquidate.label', default: 'FtLiquidate')] as Object[], "Another user has updated this FtLiquidate while you were editing")
                    render(view: "edit", model: [ftLiquidateInstance: ftLiquidateInstance])
                    return
                }
            }
            ftLiquidateInstance.properties = params
            if (!ftLiquidateInstance.hasErrors() && ftLiquidateInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), ftLiquidateInstance.id])}"
                redirect(action: "show", id: ftLiquidateInstance.id)
            }
            else {
                render(view: "edit", model: [ftLiquidateInstance: ftLiquidateInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def ftLiquidateInstance = FtLiquidate.get(params.id)
        if (ftLiquidateInstance) {
            try {
                ftLiquidateInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftLiquidate.label', default: 'FtLiquidate'), params.id])}"
            redirect(action: "list")
        }
    }
}
