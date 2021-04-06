package syncer.replica.heartbeat;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    public Heartbeat() {
    }

    public Heartbeat(int heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
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


    /**
     * 执行心跳行为
     */
    public void heartbeat(HeartbeatCommandRunner runner, long period, long period2,TimeUnit timeUnit) {
        assert heartbeat == null || heartbeat.isCancelled();
        runner.run();
        heartbeat = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                /**
                 * TODO
                 */
                runner.run();
            }
        }, period, period2, timeUnit);
    }


    void close(){
        executor.shutdown();
    }

}
