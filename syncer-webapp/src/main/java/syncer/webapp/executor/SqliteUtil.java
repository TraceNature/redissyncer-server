// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.webapp.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.jdbc.datasource.DataSourceUtils;
import syncer.common.properties.SqliteProperties;
import syncer.common.util.spring.SpringUtil;

import javax.sql.DataSource;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Objects;

import org.apache.commons.dbcp.BasicDataSource;
/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@Slf4j
public class SqliteUtil {


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

    public static String getSysPath(){
        return System.getProperty("user.dir");
    }

    private static void execute(final Connection conn) throws Exception {
        try {
            ScriptRunner runner = new ScriptRunner(conn);
            // doesn't print logger
            runner.setLogWriter(null);
            Resources.setCharset(StandardCharsets.UTF_8);
            Reader read = Resources.getResourceAsReader("init.sql");
//        log.info("execute soul schema sql: {}", SCHEMA_SQL_FILE);
            runner.runScript(read);
            runner.closeConnection();
        }finally {
            if(Objects.nonNull(conn)){
                conn.close();
            }
        }


    }
}