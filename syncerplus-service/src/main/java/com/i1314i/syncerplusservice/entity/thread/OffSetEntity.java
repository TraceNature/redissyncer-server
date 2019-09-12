package com.i1314i.syncerplusservice.entity.thread;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Getter@Setter@EqualsAndHashCode
public class OffSetEntity implements Serializable {
    private String  replId;
    private final AtomicLong replOffset = new AtomicLong(-1);
}
