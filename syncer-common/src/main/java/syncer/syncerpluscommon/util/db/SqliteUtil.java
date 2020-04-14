package syncer.syncerpluscommon.util.db;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.jdbc.datasource.DataSourceUtils;
import syncer.syncerpluscommon.config.SqliteProperties;
import syncer.syncerpluscommon.util.spring.SpringUtil;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/13
 */
@Slf4j
public class SqliteUtil {



    public static void runSqlScript() {

        DataSource data = getDataSource();
        Connection conn = null;

        try {


            conn = data.getConnection();
            execute(conn);
//            ScriptRunner runner = new ScriptRunner(conn);
//            Resources.setCharset(Charset.forName("UTF-8")); //设置字符集,不然中文乱码插入错误
//            runner.setLogWriter(null);//设置是否输出日志
//            // 绝对路径读取
////          Reader read = new FileReader(new File("f:\\test.sql"));
//            // 从class目录下直接读取
//
//            Reader read = Resources.getResourceAsReader(SqliteUtil.class.getClass().getClassLoader(),"init.sql");
//
//            runner.runScript(read);
//            runner.closeConnection();
//            conn.close();
           log.info("sql脚本执行完毕");
        } catch (Exception e) {
            log.info("sql脚本执行发生异常");
            e.printStackTrace();
        } finally {
            try {
                DataSourceUtils.doCloseConnection(conn, dataSource);
            } catch (Exception e) {
            }
        }
    }

    protected static BasicDataSource dataSource = null;

    public static DataSource getDataSource() {
        synchronized (Thread.class) {
            if (null == dataSource) {
                dataSource = new BasicDataSource();
                SqliteProperties sqliteProperties= SpringUtil.getBean(SqliteProperties.class);

                dataSource.setUrl(sqliteProperties.getUrl());
                dataSource.setDriverClassName(sqliteProperties.getDriver_class_name());



            }
        }
        return dataSource;
    }


    public static String getPath(){
        SqliteProperties sqliteProperties= SpringUtil.getBean(SqliteProperties.class);
        return sqliteProperties.getDbUrl();
    }
    public static String getFilePath(){
        SqliteProperties sqliteProperties= SpringUtil.getBean(SqliteProperties.class);
        return sqliteProperties.getFilePath();
    }

    private static void execute(final Connection conn) throws Exception {
        ScriptRunner runner = new ScriptRunner(conn);
        // doesn't print logger
        runner.setLogWriter(null);
        Resources.setCharset(StandardCharsets.UTF_8);
        Reader read = Resources.getResourceAsReader("init.sql");
//        log.info("execute soul schema sql: {}", SCHEMA_SQL_FILE);
        runner.runScript(read);
        runner.closeConnection();
        conn.close();
    }
}
