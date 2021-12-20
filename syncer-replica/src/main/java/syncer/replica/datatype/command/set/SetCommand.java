package syncer.replica.datatype.command.set;

import syncer.replica.datatype.command.ExistType;
import syncer.replica.datatype.command.GenericKeyValueCommand;
import syncer.replica.util.type.ExpiredType;

public class SetCommand extends GenericKeyValueCommand {

    private static final long serialVersionUID = 1L;

    private boolean keepTtl;
    private ExpiredType expiredType;
    private Long expiredValue;
    private XATType xatType;
    private Long xatValue;
    private ExistType existType;
    private boolean get = false;

    public SetCommand(byte[] key, byte[] value, boolean keepTtl, ExpiredType expiredType, Long expiredValue, XATType xatType, Long xatValue, ExistType existType, boolean get) {
        super(key, value);
        this.keepTtl = keepTtl;
        this.expiredType = expiredType;
        this.expiredValue = expiredValue;
        this.xatType = xatType;
        this.xatValue = xatValue;
        this.existType = existType;
        this.get = get;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isKeepTtl() {
        return keepTtl;
    }

    public void setKeepTtl(boolean keepTtl) {
        this.keepTtl = keepTtl;
    }

    public ExpiredType getExpiredType() {
        return expiredType;
    }

    public void setExpiredType(ExpiredType expiredType) {
        this.expiredType = expiredType;
    }

    public Long getExpiredValue() {
        return expiredValue;
    }

    public void setExpiredValue(Long expiredValue) {
        this.expiredValue = expiredValue;
    }

    public XATType getXatType() {
        return xatType;
    }

    public void setXatType(XATType xatType) {
        this.xatType = xatType;
    }

    public Long getXatValue() {
        return xatValue;
    }

    public void setXatValue(Long xatValue) {
        this.xatValue = xatValue;
    }

    public ExistType getExistType() {
        return existType;
    }

    public void setExistType(ExistType existType) {
        this.existType = existType;
    }

    public boolean isGet() {
        return get;
    }

    public void setGet(boolean get) {
        this.get = get;
    }
}
