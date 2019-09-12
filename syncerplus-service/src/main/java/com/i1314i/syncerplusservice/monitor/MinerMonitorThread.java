package com.i1314i.syncerplusservice.monitor;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Alive活动线程监控类（不监控手动关闭的同步任务相关线程）
 */
@Slf4j
public class MinerMonitorThread extends Thread {


    public static volatile boolean done = false;

    @Override
    public void run() {
        log.info("Alive线程监控任务启动....");
        while(!done){
//            synchronized (this) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Map<String, Thread>  aliveThreadMap=TaskMonitorUtils.getAliveThreadHashMap();
//                if(aliveThreadMap.size()>0){
//                    for (Map.Entry<String, Thread> entry:aliveThreadMap.entrySet()) {
//                        if(!entry.getValue().isAlive()||entry.getValue().isInterrupted()){
//                            TaskMonitorUtils.removeAliveThread(entry.getKey(),entry.getValue());
//                            TaskMonitorUtils.setStateThread(entry.getKey());
//                        }
//                    }
//
//                }


//                if (MinerQueue.unVisitedIsEmpty()
//                        && MinerQueue.waitingMiseringIsEmpty()
//                        && MinerQueue.storeIsEmpty()) {
//                    done = true;
//                    MinerThreadPool.shutdown();
//                    LOG.info("MinerMonitorThread程序结束。。。。。。当前线程[" + Thread.currentThread().getName() + "]");
//                    long endTime = System.currentTimeMillis();
//                    LOG.info("MinerMonitorThread已经访问队列URL大小[" + MinerQueue.getUrlSetSize() + "]当前线程[" + Thread.currentThread().getName() + "]");
//                    LOG.info("用时[" + MinerUtil.msToss(endTime - MinerUtil.starTime) + "]当前线程[" + Thread.currentThread().getName() + "]");
//                }
//            }
        }
    }

}
