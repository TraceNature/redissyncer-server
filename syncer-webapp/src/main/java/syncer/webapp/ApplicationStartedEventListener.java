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

package syncer.webapp;

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
            String somePropLevel = environment.getProperty("logging.level.root");
            String someProp = environment.getProperty("syncer.config.path.logfile");
            String logfileNameProp = environment.getProperty("syncer.config.path.logfileName");
            System.setProperty("logPath", someProp);
            System.setProperty("logLevel", somePropLevel);
            System.setProperty("logFileName", logfileNameProp);
            System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        }
    }
}