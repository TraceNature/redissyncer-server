package syncer;


import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/10  syncer.config.path.logfile
 */

public class ApplicationStartedEventListener implements ApplicationListener, Ordered {
    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER - 1;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ConfigurableEnvironment environment = ((ApplicationEnvironmentPreparedEvent) event).getEnvironment();
//            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
//            if (!activeProfiles.contains("dev")) {
//                return;
//            }
            String somePropLevel = environment.getProperty("logging.level.root");

            String someProp = environment.getProperty("syncer.config.path.logfile");
            System.setProperty("logPath", someProp);
            System.setProperty("logLevel", somePropLevel);
            System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        }
    }
}