package syncer.syncerpluscommon.config;

import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/22
 */
public class PageHelperConfig {
    //配置mybatis的分页插件pageHelper
    @Bean
    public PageHelper pageHelper(){
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum","true");
        properties.setProperty("rowBoundsWithCount","true");
        properties.setProperty("reasonable","true");
        properties.setProperty("dialect","sqlite");    //配置mysql数据库的方言
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
