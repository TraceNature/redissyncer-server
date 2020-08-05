package syncer.syncerpluscommon.util.db;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerpluscommon.util.Type2TypeUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 平行时空
 * @created 2018-05-20 9:37
 **/
@Slf4j
public class SqlUtils {
        static Lock lock=new ReentrantLock();
        // INSERT, UPDATE, DELETE 操作都可以包含在其中
        public static  int update(String sql, Object... args) {

            Connection connection = null;
            PreparedStatement preparedStatement = null;
            lock.lock();
            try {
                connection = ConnUtil.getConnection();
                preparedStatement = connection.prepareStatement(sql);

                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);

                }
                return preparedStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                ConnUtil.releaseDB(null, preparedStatement, connection);

            }
            return -1;
        }


    public static  int update(String sql, List args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        lock.lock();
        try {
            connection = ConnUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < args.size(); i++) {
                preparedStatement.setObject(i + 1, args.get(i));
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            ConnUtil.releaseDB(null, preparedStatement, connection);
        }
        return -1;
    }


    // 查询一条记录, 返回对应的对象
        public static  <T> T get(Class<T> clazz, String sql, Object... args) {
            lock.lock();
            try {
                List<T> result = getForList(clazz, sql, args);
                if(result.size() > 0){
                    return result.get(0);
                }

                return null;
            }finally {
                lock.unlock();
            }
        }

        /**
         * 传入 SQL 语句和 Class 对象, 返回 SQL 语句查询到的记录对应的 Class 类的对象的集合
         * @param clazz: 对象的类型
         * @param sql: SQL 语句
         * @param args: 填充 SQL 语句的占位符的可变参数.
         * @return
         */
        public static  <T> List<T> getForList(Class<T> clazz,
                                      String sql, Object... args) {

            List<T> list = new ArrayList<>();

            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            lock.lock();
            try {
                //1. 得到结果集
                connection = ConnUtil.getConnection();
                preparedStatement = connection.prepareStatement(sql);

                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);
                }

                resultSet = preparedStatement.executeQuery();

                //2. 处理结果集, 得到 Map 的 List, 其中一个 Map 对象
                //就是一条记录. Map 的 key 为 reusltSet 中列的别名, Map 的 value
                //为列的值.
                List<Map<String, Object>> values =
                        handleResultSetToMapList(resultSet);

                //3. 把 Map 的 List 转为 clazz 对应的 List
                //其中 Map 的 key 即为 clazz 对应的对象的 propertyName,
                //而 Map 的 value 即为 clazz 对应的对象的 propertyValue
                list = transfterMapListToBeanList(clazz, values);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                ConnUtil.releaseDB(resultSet, preparedStatement, connection);
            }

            return list;
        }

        /**
         * 转换List<Map> 为  List<T>
         * @param clazz
         * @param values
         * @return
         * @throws InstantiationException
         * @throws IllegalAccessException
         */
        public static  <T> List<T> transfterMapListToBeanList(Class<T> clazz,
                                                      List<Map<String, Object>> values) throws Exception {
            List<T> result = new ArrayList<>();
            T bean = null;
            if (values.size() > 0) {
                for (Map<String, Object> m : values) {
                    //通过反射创建一个其他类的对象
                    bean = clazz.newInstance();
                    for (Map.Entry<String, Object> entry : m.entrySet()) {
                        String propertyName = entry.getKey();
                        Object value = entry.getValue();

                        Field f=bean.getClass().getDeclaredField(propertyName);
                        f.setAccessible(true);

                        Class<?> filedCls = f.getType();
                        Object columnValueObj=null;
                        if(filedCls == boolean.class || filedCls == Boolean.class) {
                            if(value instanceof Integer){
                                value = Type2TypeUtils.int2boolean((Integer) value);
                            }else{
                                value= (Boolean) value;
                            }

                        }else if(filedCls == String.class){
                            value= (String) value;
                        }else if(filedCls == int.class || filedCls == Integer.class){
                            value= (Integer) value;
                        }else if(filedCls == byte.class || filedCls == Byte.class){
                            value= (Byte) value;
                        }else if(filedCls == short.class || filedCls == Short.class) {
                            value= (Short) value;
                        } else if(filedCls == long.class || filedCls == Long.class) {
                            value= Long.valueOf(String.valueOf(value) );
                        }else if(filedCls == float.class || filedCls == Float.class) {
                            value= (Float) value;
                        }else if(filedCls == double.class || filedCls == Double.class) {
                            value= (Double) value;
                        } else if(filedCls == BigDecimal.class) {
                            value= (BigDecimal) value;
                        }



//                        try {
                            f.set(bean, value);
//                        }catch (Exception e){
//                            if(e.getMessage().indexOf("Can not set boolean field")>=0){
//                                f.set(bean, Type2TypeUtils.int2boolean((Integer) value));
//                            }else {
//                                e.printStackTrace();
//                            }
//                        }
                        //BeanUtils.setProperty(bean, propertyName, value);
                    }
                    // 13. 把 Object 对象放入到 list 中.
                    result.add(bean);
                }
            }

            return result;
        }



        /**
         * 处理结果集, 得到 Map 的一个 List, 其中一个 Map 对象对应一条记录
         *
         * @param resultSet
         * @return
         * @throws SQLException
         */
        public static  List<Map<String, Object>> handleResultSetToMapList(
                ResultSet resultSet) throws SQLException {
            // 5. 准备一个 List<Map<String, Object>>:
            // 键: 存放列的别名, 值: 存放列的值. 其中一个 Map 对象对应着一条记录
            List<Map<String, Object>> values = new ArrayList<>();

            List<String> columnLabels = getColumnLabels(resultSet);
            Map<String, Object> map = null;

            // 7. 处理 ResultSet, 使用 while 循环
            while (resultSet.next()) {
                map = new HashMap<>();

                for (String columnLabel : columnLabels) {
                    Object value = resultSet.getObject(columnLabel);
                    map.put(columnLabel, value);
                }
                // 11. 把一条记录的一个 Map 对象放入 5 准备的 List 中
                values.add(map);
            }
            return values;
        }

        /**
         * 获取结果集的 ColumnLabel 对应的 List
         *
         * @param rs
         * @return
         * @throws SQLException
         */
        private static  List<String> getColumnLabels(ResultSet rs) throws SQLException {
            List<String> labels = new ArrayList<>();

            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                labels.add(rsmd.getColumnLabel(i + 1));
            }

            return labels;
        }

        // 返回某条记录的某一个字段的值 或 一个统计的值(一共有多少条记录等.)
        public <E> E getForValue(String sql, Object... args) {

            //1. 得到结果集: 该结果集应该只有一行, 且只有一列
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            try {
                //1. 得到结果集
                connection = ConnUtil.getConnection();
                preparedStatement = connection.prepareStatement(sql);

                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);
                }

                resultSet = preparedStatement.executeQuery();

                if(resultSet.next()){
//                    System.out.println(resultSet.getString("username"));
                    return (E) resultSet.getObject(1);
                }
            } catch(Exception ex){
                ex.printStackTrace();
            } finally{
                ConnUtil.releaseDB(resultSet, preparedStatement, connection);
            }
            //2. 取得结果

            System.out.println("error");
            return null;
        }


    /**
     * 批处理   // INSERT, UPDATE, DELETE 操作都可以包含在其中
     * @param sql
     * @return Integer
     */
    public static  Integer  updateBatch(String sql,Object...args) throws Exception {
        PreparedStatement pre = null;

        Connection connection=null;
        try {
            connection=ConnUtil.getConnection();
            pre = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                pre.setObject(1,args[i]);
                pre.addBatch();
            }

             int[] num =null;
            num = pre.executeBatch();


//            int p=0;
//            for (int i:
//                    num) {
//                if(i>0){
//                    p++;
//                }
//            }

            return num.length;

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            ConnUtil.releaseDB(null, pre, connection);
        }
        return 0;
    }


    public static  Integer  updateBatch(String sql,List<Object[]>args) throws Exception {
        PreparedStatement pre = null;

        Connection connection=null;
        lock.lock();
        try {
            connection=ConnUtil.getConnection();
            pre = connection.prepareStatement(sql);
            for (int i = 0; i < args.size(); i++) {
                Object[] object=args.get(i);
                for (int j = 0; j < object.length; j++) {
                    pre.setObject(j+1,object[j]);
                }
                pre.addBatch();
            }

            int[] num =null;
            num = pre.executeBatch();

            return num.length;

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            ConnUtil.releaseDB(null, pre, connection);
        }
        return 0;
    }


    /**
     * 插入更新对象集合 批处理
     * @param sql
     * @param clazz
     * @param values
     * @param <T>
     * @return
     * @throws Exception
     */
    public static synchronized <T> Integer InsertAndUpdate(String sql,Class<T> clazz,List<Map<String, Object>> values) {

        Connection connection=null;
        PreparedStatement pre= null;
        try {
            connection=ConnUtil.getConnection();
            pre = connection.prepareStatement(sql);
            T bean = null;
            if(values.size()>0){
                for (Map<String,Object>m:values
                        ) {
                    for(int i=0;i<m.size();i++){
                        pre.setObject(i+1,m.get(i));
                    }
                    pre.addBatch();
                }
               int[] num= pre.executeBatch();
                return num.length;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            ConnUtil.releaseDB(null,pre,connection);
        }


        return 0;
    }
}
