package com.i1314i.syncerplusservice.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 不同版本管道传输时  记录指令数量
 */
@Setter
@Getter
public class SyncTaskEntity {
    private int syncNums=0;

    public void add(){
        syncNums++;
    }

    public void clear(){
        syncNums=0;
    }
}
