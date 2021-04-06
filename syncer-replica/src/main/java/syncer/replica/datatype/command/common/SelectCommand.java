package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * Command select {currentNumber}
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
public class SelectCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;
    private int currentNumber;

    public SelectCommand(int currentNumber) {
        this.currentNumber = currentNumber;
    }

    public SelectCommand() {
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(int currentNumber) {
        this.currentNumber = currentNumber;
    }
}
