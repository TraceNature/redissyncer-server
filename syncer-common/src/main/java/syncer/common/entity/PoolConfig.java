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
package syncer.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "syncerplus.poolconfig")
@Getter
@Setter
public class PoolConfig {

    /**
     * 核心线程池大小
     */
    private  int corePoolSize;

    /**
     * 最大可创建的线程数
     */
    private  int maxPoolSize;

    /**
     *
     * 队列最大长度
     */
    private  int queueCapacity;

    /**
     * 线程池维护线程所允许的空闲时间
     */
    private  int keepAliveSeconds;
}
