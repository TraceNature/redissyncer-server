package com.i1314i.syncerplusservice.task;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientTask implements Runnable {
    byte[] key;
    long expired;
    byte[] dumped;
    boolean replace;

    @Override
    public void run() {

    }
}
