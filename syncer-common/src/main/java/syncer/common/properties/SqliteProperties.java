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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/13
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SqliteProperties {
    private String  driver_class_name;
    private String url;
    private String dbUrl;
    private String filePath;

    public String getDbUrl() {
        if(url.contains("sqlite")){
            return url.split("jdbc:sqlite:")[1];
        }
        return url;
    }

    public String getFilePath() {
        return getDbUrl().substring(0,getDbUrl().lastIndexOf("/"));
    }
}
