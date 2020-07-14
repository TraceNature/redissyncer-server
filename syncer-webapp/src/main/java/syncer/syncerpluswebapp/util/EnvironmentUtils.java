package syncer.syncerpluswebapp.util;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/10
 */
@Component
public class EnvironmentUtils implements EnvironmentAware {

    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtils.environment = environment;
    }
    // 获取环境变量中的配置属性
    public static String searchByKey(String key){
        return EnvironmentUtils.environment.getProperty(key);
    }
}