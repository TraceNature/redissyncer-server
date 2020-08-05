package syncer.syncerpluscommon.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
//@Configuration
//@MapperScan(basePackages = {"syncer"}, sqlSessionFactoryRef = "sqlSessionFactory1")
public class MybatisDBConfig {

    @Autowired
    @Qualifier("datasourceALI")
    private DataSource datasourceALI;
    static SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    @Bean
    @Primary
    @ConditionalOnMissingBean  //当容器里没有指定的bean的情况下创建bean
    public SqlSessionFactoryBean sqlSessionFactoryBean1() throws Exception {

        //指定数据源
        factoryBean.setDataSource(datasourceALI); // 使用ALI数据源, 连接ALI库
        //设置mybatis的主配置文件
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        Resource mybatisConfigXml = resolver.getResource("classpath:mapping/config/mybatis-config.xml");
//        Resource[] mapperConfigXmls = resolver.getResources("classpath:mapping/*Mapper.xml");
//        factoryBean.setTypeAliasesPackage("com.XXX.model");
//        factoryBean.setConfigLocation(mybatisConfigXml);
//        factoryBean.setMapperLocations(mapperConfigXmls);
        return factoryBean;
    }


    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory1() throws Exception {
        return sqlSessionFactoryBean1().getObject();
    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate1() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory1()); // 使用上面配置的Factory
        return template;
    }
}

