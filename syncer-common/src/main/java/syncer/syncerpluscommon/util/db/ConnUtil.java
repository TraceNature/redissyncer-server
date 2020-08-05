package syncer.syncerpluscommon.util.db;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerpluscommon.util.yml.YmlPropUtils;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @author 平行时空
 * @created 2018-05-19 23:42
 **/
@Slf4j
public class ConnUtil {
    static Connection connection;


        public static Connection getConnection() throws Exception {
            String user = (String) YmlPropUtils.getInstance().getProperty("spring.datasource.username");
            String password = (String) YmlPropUtils.getInstance().getProperty("spring.datasource.password");
            String url= (String) YmlPropUtils.getInstance().getProperty("spring.datasource.url");
            String jdbcDriver=(String) YmlPropUtils.getInstance().getProperty("spring.datasource.driver-class-name");
            Class.forName(jdbcDriver);
            //首先判断是否为空
            if(connection==null) {
                //可能多个线程同时进入到这一步进行阻塞等待
                synchronized(ConnUtil.class) {
                    //第一个线程拿到锁，判断不为空进入下一步
                    if(connection==null) {
                        /**
                         * 由于编译器的优化、JVM的优化、操作系统处理器的优化，可能会导致指令重排（happen-before规则下的指令重排，执行结果不变，指令顺序优化排列）
                         * new Singleton3()这条语句大致会有这三个步骤：
                         * 1.在堆中开辟对象所需空间，分配内存地址
                         * 2.根据类加载的初始化顺序进行初始化
                         * 3.将内存地址返回给栈中的引用变量
                         *
                         * 但是由于指令重排的出现，这三条指令执行顺序会被打乱，可能导致3的顺序和2调换
                         * 👇
                         */

                        log.info("--------------初始化connect");
                        connection =  DriverManager.getConnection(url, user,
                                password);
                    }
                }
            }
            return connection;
        }

        public static void releaseDB(ResultSet resultSet, Statement statement,
                                     Connection connection) {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
        }

    }
