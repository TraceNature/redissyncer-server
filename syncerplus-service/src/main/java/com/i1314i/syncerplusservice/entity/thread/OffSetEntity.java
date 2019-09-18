package com.i1314i.syncerplusservice.entity.thread;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Getter@Setter@EqualsAndHashCode
public class OffSetEntity implements Serializable {
    private static final long serialVersionUID = -5809782578272943997L;
    private String  replId;
    private final AtomicLong replOffset = new AtomicLong(-1);
}
