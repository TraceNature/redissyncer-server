// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.util.lock;

import syncer.common.config.EtcdServerConfig;
import syncer.common.constant.StoreType;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/16
 */
public class TaskRunUtils {
    static Map<String, Lock> LOCK_MAP=new ConcurrentHashMap<>();
    static Map<String, Boolean> TASK_START_DATA=new ConcurrentHashMap<>();
    static EtcdServerConfig serverConfig = new EtcdServerConfig();
    private static JEtcdClient client;

    static {

        if(!StoreType.SQLITE.equals(serverConfig.getStoreType())){
            client= JEtcdClient.build();
        }
    }

    public static synchronized void  addTask(String taskId){
        LOCK_MAP.put(taskId,new ReentrantLock());
    }

    public static synchronized void  removeTask(String taskId){
        LOCK_MAP.remove(taskId);
    }

    public static synchronized boolean  containsTask(String taskId){
        return  LOCK_MAP.containsKey(taskId);
    }


    public static Lock getTaskLock(String taskId){
        if(!LOCK_MAP.containsKey(taskId)){
            LOCK_MAP.put(taskId,new ReentrantLock());
        }
        return LOCK_MAP.get(taskId);
    }


    public static synchronized   void getTaskLock(String taskId,EtcdLockCommandRunner runner){
        if(StoreType.SQLITE.equals(serverConfig.getStoreType())){
            if(!LOCK_MAP.containsKey(taskId)){
                LOCK_MAP.put(taskId,new ReentrantLock());
            }
            Lock lock=LOCK_MAP.get(taskId);
            lock.lock();
            try {
                runner.run();
            }finally {
                lock.unlock();
            }
        }else {
            client.lockCommandRunner(runner);
        }
    }

    public static void putTaskStartData(String taskId){
        TASK_START_DATA.put(taskId,true);
    }

}
