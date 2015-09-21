package settle
/**
 * 业务类型
 */
class FtFeeChannel {

    String code //通道编号
    String name //通道名称
    String type //业务类型
    Integer ftSrvTypeId //所属业务类型ID

    static mapping = {
        id generator: 'sequence', params: [sequence: 'seq_fee_channel']
    }

    static constraints = {
        code(maxSize: 30, nullable: false, blank: false)
        name(maxSize: 50, nullable: false, blank: false)
        type(maxSize: 30, nullable: false, blank: false)
        ftSrvTypeId(maxSize: 19, nullable: false, blank: false)
    }
    def static dsfMap=['batch': '批量渠道','single': '单笔渠道','interface':'接口渠道']
}