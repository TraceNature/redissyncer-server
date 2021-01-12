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
package syncer.common.properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/9
 */

@Component
@ConfigurationProperties(prefix = "syncer.config.path")
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DbProperties {
    /**
     * 日志文件
     */
    private String logfile;
    /**
     * 数据文件
     */
    private String datafile;

    public String getLogfile() {
        if(StringUtils.isEmpty(logfile)){
            logfile="./log";
        }
        return logfile;
    }

    public String getDatafile() {
        if(StringUtils.isEmpty(logfile)){
            logfile="./";
        }
        return datafile;
    }
}
