package syncer.syncerpluswebapp.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/13
 */
//@Component
public class CustomizeDataSourceInitializer implements InitializingBean {


    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        System.out.println("==================sql脚本正在执行==================");
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("/init.sql"));
        System.out.println("==================sql脚本初始化完成==================");
        return populator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
