package com.i1314i.syncerplusservice.util;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TaskMonitorUtils {
    @Getter
    private static Map<String,Thread> aliveThreadHashMap=new ConcurrentHashMap<String,Thread>();

    /**
     * 停止但还活着的线程
     */
    @Getter
    private static Map<String,Thread> deadThreadHashMap=new ConcurrentHashMap<String,Thread>();

    /**
     * 线程状态
     */
    @Getter
    private static Map<String,Boolean> threadStateHashMap=new ConcurrentHashMap<String,Boolean>();


    /**
     * 添加线程到aliveMap
     * @param threadName
     * @param thread
     */
    public synchronized static void addAliveThread(String threadName,Thread thread){
        threadStateHashMap.put(threadName,true);
        aliveThreadHashMap.put(threadName,thread);
        if(deadThreadHashMap.containsKey(threadName)){
            deadThreadHashMap.remove(threadName);
        }

    }

    /**
     * 从aliveMap中将线程删除加入deadMap
     * @param threadName
     */
    public synchronized static void removeAliveThread(String threadName,Thread thread){
        if(aliveThreadHashMap.containsKey(threadName)){
            deadThreadHashMap.put(threadName,thread);
            aliveThreadHashMap.remove(threadName);
        }

    }


    public synchronized static void removeAliveThread(String threadName){
        if(aliveThreadHashMap.containsKey(threadName)){
            deadThreadHashMap.put(threadName,aliveThreadHashMap.get(threadName));
            aliveThreadHashMap.remove(threadName);
        }

    }


    /**
     * 加入DeadMap 并将同名thread从中删除
     * @param threadName
     * @param thread
     */
    public synchronized static void addDeadThread(String threadName,Thread thread){
        aliveThreadHashMap.remove(threadName);
        deadThreadHashMap.put(threadName,thread);
    }

    /**
     * 删除dead中的线程
     * @param threadName
     */
    public synchronized static void removeDeadThread(String threadName){
        deadThreadHashMap.remove(threadName);
    }


    public synchronized static void addStateThread(String threadName,Boolean status){
        threadStateHashMap.put(threadName,status);
    }

    public synchronized static void removeStateThread(String threadName){
        threadStateHashMap.remove(threadName);
    }



    public synchronized static void setStateThread(String threadName){
        threadStateHashMap.put(threadName,false);
    }

    public synchronized static void removeAliveAndDeadThread(String threadName){
        deadThreadHashMap.remove(threadName);
        aliveThreadHashMap.remove(threadName);
    }



    public static boolean containsKeyAliveMap(String key){
        return aliveThreadHashMap.containsKey(key);
    }


    public static boolean containsKeyDeadMap(String key){
        return deadThreadHashMap.containsKey(key);
    }
}
