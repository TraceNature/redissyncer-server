package com.i1314i.syncerplusservice.entity;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class PipelineDataEntity {
    private byte[] key;
    private int dbNum;
    private int ttl;

    public PipelineDataEntity(byte[] key, int dbNum, int ttl) {
        this.key = key;
        this.dbNum = dbNum;
        this.ttl = ttl;
    }
}