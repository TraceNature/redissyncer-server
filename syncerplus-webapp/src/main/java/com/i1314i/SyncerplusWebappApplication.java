package com.i1314i;

import com.i1314i.syncerplusservice.monitor.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages="com.i1314i")
@EnableScheduling
@EnableCaching  //开启缓存
public class SyncerplusWebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncerplusWebappApplication.class, args);
        /**
         * 开启线程监控
         */
        new Thread(new MinerMonitorThread()).start();
    }



    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}
