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
