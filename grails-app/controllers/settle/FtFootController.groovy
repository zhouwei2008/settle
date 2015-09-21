package settle

class FtFootController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [ftFootInstanceList: FtFoot.list(params), ftFootInstanceTotal: FtFoot.count()]
    }

    def create = {
        def ftFootInstance = new FtFoot()
        ftFootInstance.properties = params
        return [ftFootInstance: ftFootInstance]
    }

    def save = {
        def ftFootInstance = new FtFoot(params)
        if (ftFootInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), ftFootInstance.id])}"
            redirect(action: "show", id: ftFootInstance.id)
        }
        else {
            render(view: "create", model: [ftFootInstance: ftFootInstance])
        }
    }

    def show = {
        def ftFootInstance = FtFoot.get(params.id)
        if (!ftFootInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
            redirect(action: "list")
        }
        else {
            [ftFootInstance: ftFootInstance]
        }
    }

    def edit = {
        def ftFootInstance = FtFoot.get(params.id)
        if (!ftFootInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [ftFootInstance: ftFootInstance]
        }
    }

    def update = {
        def ftFootInstance = FtFoot.get(params.id)
        if (ftFootInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (ftFootInstance.version > version) {

                    ftFootInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'ftFoot.label', default: 'FtFoot')] as Object[], "Another user has updated this FtFoot while you were editing")
                    render(view: "edit", model: [ftFootInstance: ftFootInstance])
                    return
                }
            }
            ftFootInstance.properties = params
            if (!ftFootInstance.hasErrors() && ftFootInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), ftFootInstance.id])}"
                redirect(action: "show", id: ftFootInstance.id)
            }
            else {
                render(view: "edit", model: [ftFootInstance: ftFootInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def ftFootInstance = FtFoot.get(params.id)
        if (ftFootInstance) {
            try {
                ftFootInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'ftFoot.label', default: 'FtFoot'), params.id])}"
            redirect(action: "list")
        }
    }
}
