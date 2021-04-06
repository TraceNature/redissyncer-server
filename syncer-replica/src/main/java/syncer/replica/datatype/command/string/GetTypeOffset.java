package syncer.replica.datatype.command.string;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class GetTypeOffset implements Statement {

    private static final long serialVersionUID = 1L;

    private byte[] type;
    private byte[] offset;

    public GetTypeOffset() {
    }

    public GetTypeOffset(byte[] type, byte[] offset) {
        this.type = type;
        this.offset = offset;
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
}
