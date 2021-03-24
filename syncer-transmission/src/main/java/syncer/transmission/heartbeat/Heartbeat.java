package syncer.transmission.heartbeat;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 自定义心跳任务
 * @author: Eq Zhan
 * @create: 2021-02-20
 **/
@Slf4j
public class Heartbeat {
    private ScheduledFuture<?> heartbeat;
    private ScheduledExecutorService executor= Executors.newSingleThreadScheduledExecutor();
    /**
     * 心跳频率
     */
    @Builder.Default
    private int heartbeatPeriod = 1000;
    /**
     * 心跳行为
     */
    private HeartbeatCommandRunner commandRunner;

    public Heartbeat(int heartbeatPeriod, HeartbeatCommandRunner commandRunner) {
        this.heartbeatPeriod = heartbeatPeriod;
        this.commandRunner = commandRunner;
    }

    public Heartbeat(HeartbeatCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    /**
     * 执行心跳行为
     */
    public void heartbeat() {
        assert heartbeat == null || heartbeat.isCancelled();
        commandRunner.run();
        heartbeat = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                /**
                 * TODO
                 */
                commandRunner.run();
            }
        }, heartbeatPeriod, heartbeatPeriod, MILLISECONDS);

    }


    void close(){
        executor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        Heartbeat heartbeat=new Heartbeat(1000,new DefaultHeartbeatCommandRunner());
        heartbeat.heartbeat();

        Thread.sleep(10000);
        heartbeat.close();
    }

}
