package syncer.syncerpluscommon.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import syncer.syncerpluscommon.util.db.SqliteUtil;
import syncer.syncerpluscommon.util.file.FileUtils;

import javax.sql.DataSource;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/13
 */
//@Configuration
public class InitDBConfig {
//    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {

        if(!FileUtils.existsFile(SqliteUtil.getFilePath())){
            FileUtils.mkdirs(SqliteUtil.getFilePath());
        }

        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("/init.sql"));
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }
}
