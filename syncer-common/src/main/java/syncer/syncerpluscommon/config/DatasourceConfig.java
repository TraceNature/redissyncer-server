package syncer.syncerpluscommon.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
//@Configuration
public class DatasourceConfig {
    static DataSource dataSource=DataSourceBuilder.create()
            .driverClassName("org.sqlite.JDBC")
                .url("jdbc:sqlite:data/syncer.db")
                .type(HikariDataSource.class)
                .build();
    @Bean(name = "datasourceALI")
    @Primary
//    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSourceALI() {
        return dataSource;
    }
}
