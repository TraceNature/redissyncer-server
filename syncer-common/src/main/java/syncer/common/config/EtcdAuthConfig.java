package syncer.common.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * etcd参数
 * @author: Eq Zhan
 * @create: 2021-03-01
 **/
@Component
@ConfigurationProperties(prefix = "etcd")
@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EtcdAuthConfig {
    private String[] endpoints;
    private String url;
    private String username;
    private String password;


    public String getUrl() {
        StringBuilder stringBuilder=new StringBuilder();
        if(Objects.isNull(url)&& Objects.nonNull(endpoints)){
            for (int i=0;i<endpoints.length;i++){
                stringBuilder.append("http://").append(endpoints[i]);
                if(i!=endpoints.length-1){
                    stringBuilder.append(",");
                }
            }
            this.url=stringBuilder.toString();
        }
        return url;
    }


}
