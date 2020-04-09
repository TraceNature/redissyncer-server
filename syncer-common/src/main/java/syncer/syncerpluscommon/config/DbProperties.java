package syncer.syncerpluscommon.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    private String logfile;
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
