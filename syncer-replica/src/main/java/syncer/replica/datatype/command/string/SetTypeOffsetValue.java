package syncer.replica.datatype.command.string;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class SetTypeOffsetValue implements Statement {

    private static final long serialVersionUID = 1L;

    private byte[] type;
    private byte[] offset;
    private long value;

    public SetTypeOffsetValue() {
    }

    public SetTypeOffsetValue(byte[] type, byte[] offset, long value) {
        this.type = type;
        this.offset = offset;
        this.value = value;
    }

    public byte[] getType() {
        return type;
    }

    public void setType(byte[] type) {
        this.type = type;
    }

    public byte[] getOffset() {
        return offset;
    }

    public void setOffset(byte[] offset) {
        this.offset = offset;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

}