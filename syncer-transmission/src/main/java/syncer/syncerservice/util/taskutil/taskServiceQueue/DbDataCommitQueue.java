package syncer.syncerservice.util.taskutil.taskServiceQueue;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
public class DbDataCommitQueue {

    private static ArrayBlockingQueue queue=new ArrayBlockingQueue(4000);

    public static void put(Object object){
        try {
            queue.put(object);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Object take(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int size(){
      return queue.size();
    }
}
