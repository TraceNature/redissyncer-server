package syncer.replica.datatype.command.string;

import syncer.replica.datatype.command.GenericKeyCommand;

import java.util.List;


/**
 * BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL]
 * > BITFIELD mykey INCRBY i5 100 1 GET u4 0
 */
public class BitFieldCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private List<Statement> statements;
    private List<OverFlow> overFlows;

    public BitFieldCommand() {
    }

    public BitFieldCommand(byte[] key, List<Statement> statements, List<OverFlow> overFlows) {
        super(key);
        this.statements = statements;
        this.overFlows = overFlows;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public List<OverFlow> getOverFlows() {
        return overFlows;
    }

    public void setOverFlows(List<OverFlow> overFlows) {
        this.overFlows = overFlows;
    }
}