package com.i1314i.syncerplusservice.service.command;

import com.moilioncircle.redis.replicator.cmd.impl.SetCommand;

public class SuperCommand extends SetCommand {
    private static final long serialVersionUID = 1L;

    private byte[] command;
    private byte[][] args = new byte[0][];
    private int index;
    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public byte[][] getArgs() {
        return args;
    }

    public void setArgs(byte[][] args) {
        this.args = args;
    }



    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
