package syncer.replica.datatype.command.string;

import java.io.Serializable;
import java.util.List;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class OverFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    private OverFlowType overFlowType;
    private List<Statement> statements;

    public OverFlow() {
    }

    public OverFlow(OverFlowType overFlowType, List<Statement> statements) {
        this.overFlowType = overFlowType;
        this.statements = statements;
    }

    public OverFlowType getOverFlowType() {
        return overFlowType;
    }

    public void setOverFlowType(OverFlowType overFlowType) {
        this.overFlowType = overFlowType;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }
}