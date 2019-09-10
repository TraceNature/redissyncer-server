package com.i1314i.syncerplusservice.entity;

import com.i1314i.syncerplusservice.entity.thread.EventTypeEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 不同版本管道传输时  记录指令数量
 */

public class SyncTaskEntity {

    private  int syncNums=0;
    private Lock lock=new ReentrantLock();
    private boolean userStatus=true;
    private volatile List<EventEntity>keys =new ArrayList<>();

    public synchronized void addKey(EventEntity key){
        try {
            lock.lock();
            keys.add(key);
        }finally {
            lock.unlock();
        }

    }

    public List<EventEntity> getKeys() {
        return keys;
    }

    public synchronized int getSyncNums() {
        return syncNums;
    }

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
        try {
            lock.lock();
            this.syncNums++;
        }finally {
            lock.unlock();
        }

    }



    public synchronized void add(int num){
        try {
            lock.lock();
            this.syncNums+=num;
        }finally {
            lock.unlock();
        }
    }
    public synchronized void clear(){
        try {
            lock.lock();
            this.syncNums=0;
        }finally {
            lock.unlock();
        }

    }
}
