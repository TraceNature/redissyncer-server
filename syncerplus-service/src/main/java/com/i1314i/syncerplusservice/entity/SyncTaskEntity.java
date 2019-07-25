package com.i1314i.syncerplusservice.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 不同版本管道传输时  记录指令数量
 */

public class SyncTaskEntity {
    @Getter
    private  int syncNums=0;

    private boolean userStatus=true;





    public boolean isUserStatus() {
        return userStatus;
    }

    public synchronized void inUserStatus() {
        this.userStatus = userStatus;
    }

    public synchronized void offUserStatus() {
        this.userStatus = userStatus;
    }

    public synchronized void add(){
        syncNums++;
    }

    public synchronized void clear(){
        syncNums=0;
    }
}
