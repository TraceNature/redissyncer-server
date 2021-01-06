package syncer.webapp.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@Slf4j
public class SqlFileExecutor {

    /**
     * 读取 SQL 文件，获取 SQL 语句
     * @param sqlFile SQL 脚本文件
     * @return List<sql> 返回所有 SQL 语句的 List
     * @throws Exception
     */
    private static List<String> loadSql(String sqlFile) throws Exception {
        List<String> sqlList = new ArrayList<String>();

        try {

//            InputStream sqlFileIn = new FileInputStream(sqlFile);
            InputStream sqlFileIn = SqlFileExecutor.class.getClassLoader().getResourceAsStream(
                    sqlFile);
            StringBuffer sqlSb = new StringBuffer();
            byte[] buff = new byte[1024];
            int byteRead = 0;
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlSb.append(new String(buff, 0, byteRead));
            }

            // Windows 下换行是 /r/n, Linux 下是 /n
            String[] sqlArr = sqlSb.toString().split("(;//s*//r//n)|(;//s*//n)");
            for (int i = 0; i < sqlArr.length; i++) {
                String sql = sqlArr[i].replaceAll("--.*", "").trim();
                if (!sql.equals("")) {
                    sqlList.add(sql);
                }
            }
            return sqlList;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * 传入连接来执行 SQL 脚本文件，这样可与其外的数据库操作同处一个事物中
     * @param conn 传入数据库连接
     * @param sqlFile SQL 脚本文件
     * @throws Exception
     */
    public  static void execute(Connection conn, String sqlFile) throws Exception {
        Statement stmt = null;
        List<String> sqlList = loadSql(sqlFile);
        stmt = conn.createStatement();
        for (String sql : sqlList) {
            stmt.addBatch(sql);
        }
        int[] rows = stmt.executeBatch();
        System.out.println("Row count:" + Arrays.toString(rows));
    }

    /**
     * 自建连接，独立事物中执行 SQL 文件
     * @param sqlFile SQL 脚本文件
     * @throws Exception
     */
    public static void execute(String sqlFile) throws Exception {

        Connection conn = null;
        Statement stmt = null;
        List<String> sqlList = loadSql(sqlFile);
        DataSource data = SqliteUtil.getDataSource();

        try {

            conn=data.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (String sql : sqlList) {
                if(StringUtils.isEmpty(sql)){
                    continue;
                }
                stmt.addBatch(sql);
            }
            int[] rows = stmt.executeBatch();
            System.out.println("Row count:" + Arrays.toString(rows));
            conn.commit();
            log.info("sql脚本执行完毕");
        } catch (Exception ex) {
            conn.rollback();
            log.info("sql脚本执行发生异常");
            throw ex;
        } finally {
            try {
                DataSourceUtils.doCloseConnection(conn, data);
            } catch (Exception e) {
            }
        }
    }


    public static void execute() throws Exception {

        try {
            InputStream input = SqlFileExecutor.class.getClassLoader().getResourceAsStream(
                    "syncer.db");
            FileOutputStream output = new FileOutputStream(SqliteUtil.getPath());
            FileCopyUtils.copy(input, output);
            log.info("sql脚本执行完毕");
        }catch (Exception e){
            log.info("sql脚本执行发生异常");
            throw e;
        }

//        execute("init.sql");
    }

//    public static void main(String[] args) throws Exception {
//        List<String> sqlList = new SqlFileExecutor().loadSql(args[0]);
//        System.out.println("size:" + sqlList.size());
//        for (String sql : sqlList) {
//            System.out.println(sql);
//        }
//    }

}
