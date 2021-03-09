package syncer.common.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
    private String url;
    private String username;
    private String password;
}
