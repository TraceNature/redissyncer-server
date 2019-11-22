package syncer.syncerplusservice.util.file;

import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.util.TaskMsgUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件操作类
 */
public class FileUtils {

    /**
     * 保存文件
     *
     * @param file
     * @param pathname
     * @return
     */
    public static synchronized String saveFile(MultipartFile file, String pathname) {
        try {
            File targetFile = new File(pathname);
            if (targetFile.exists()) {
                return pathname;
            }

            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            file.transferTo(targetFile);

            return pathname;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static synchronized boolean existsFile(String path) {
        File targetFile = new File(path);

        return targetFile.exists();
    }


    /**
     * 删除文件
     *
     * @param pathname
     * @return
     */
    public static synchronized boolean deleteFile(String pathname) {
        File file = new File(pathname);
        if (file.exists()) {
            boolean flag = file.delete();

            if (flag) {
                File[] files = file.getParentFile().listFiles();
                if (files == null || files.length == 0) {
                    file.getParentFile().delete();
                }
            }

            return flag;
        }

        return false;
    }


    /**
     * 保存文件内容
     *
     * @param value
     * @param path
     */
    public static synchronized void saveTextFile(String value, String path) {
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            writer = new FileWriter(file);
            writer.write(value);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据路径获取内容
     *
     * @param path
     * @return
     */
    public static synchronized String getText(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            return getText(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static synchronized String getText(InputStream inputStream) {
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;
        try {
            isr = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(isr);
            StringBuilder builder = new StringBuilder();
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                string = string + "\n";
                builder.append(string);
            }

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String getLockFileName() {
        return "/syncer.lock";
    }

    public static String getSettingName() {
        return "/syncerSetting.json";
    }

    /**
     * 创建文件锁（判断是否首次启动）
     */
    public static void createSyncerLock() {
        String path = System.getProperty("user.dir") + FileUtils.getLockFileName();
        if (!existsFile(path)) {
            saveTextFile("syncer", path);
        }

    }


    /**
     * 删除文件锁
     */
    public static void delSyncerLock() {
        String path = System.getProperty("user.dir") + FileUtils.getLockFileName();
        if (existsFile(path)) {
            deleteFile(path);
        }

    }

    /**
     * 创建配置文件
     * @param value
     */
    public static synchronized void createSyncerSetting(String value){
        String lockPath = System.getProperty("user.dir") + FileUtils.getLockFileName();
        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();
        if(!existsFile(lockPath)){
            createSyncerLock();
        }

        saveTextFile(value,settingPath);
    }


    /**
     * 清理配置文件
     */
    public static void cleanSettings(){
        String lockPath = System.getProperty("user.dir") + FileUtils.getLockFileName();
        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();
        if(existsFile(lockPath)){
            deleteFile(lockPath);
        }
        if(existsFile(settingPath)){
            deleteFile(settingPath);
        }
    }


    public static void flushSettings(){
        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();

        String lockPath = System.getProperty("user.dir") + FileUtils.getLockFileName();

        if(!FileUtils.existsFile(lockPath)){
            FileUtils.createSyncerLock();
        }



            if(TaskMsgUtils.getAliveThreadHashMap()!=null&& TaskMsgUtils.getAliveThreadHashMap().size()>0){
                try {
                    Map<String,ThreadMsgEntity>data=new ConcurrentHashMap<>();
                    data.putAll(TaskMsgUtils.getAliveThreadHashMap());
                    for (Map.Entry<String,ThreadMsgEntity> d:data.entrySet()) {
                        ThreadMsgEntity msgEntity=d.getValue();
                        ThreadMsgEntity newData=new ThreadMsgEntity();

                        newData.setId(msgEntity.getId());
                        newData.setTaskName(msgEntity.getTaskName());
                        newData.setThread(msgEntity.getThread());
                        newData.setRedisClusterDto(msgEntity.getRedisClusterDto());
                        newData.setStatus(msgEntity.getStatus());
                        newData.setOffsetMap(msgEntity.getOffsetMap());
//                        BeanUtils.copyProperties(msgEntity,newData);
//                        newData.setRList(new ArrayList<>());
                        data.put(d.getKey(),newData);
                    }
                    ObjectOutToFile(settingPath,data);
                } catch (IOException e) {
                    e.printStackTrace();
                }


//                saveTextFile(JSON.toJSONString(TaskMsgUtils.getAliveThreadHashMap(), SerializerFeature.DisableCircularReferenceDetect),settingPath);
            }

    }


    public synchronized static void ObjectOutToFile(String path,Object object) throws IOException {
        //创建序列化流并关联文件
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
        //实现序列化
        //注意:序列化后得到的内容不能直接查看,要看必须经过逆序列化
        objectOutputStream.writeObject(object);
        //关闭流
        objectOutputStream.close();
    }


    public synchronized static Object FileInputToObject(String path) throws IOException, ClassNotFoundException {

        //创建逆序列化流并关联文件
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path));

        //实现逆序列化--读
        Object object = objectInputStream.readObject();

        return object;
    }


    public synchronized static void WriteToFile(String path,String object) throws IOException {
        //创建序列化流并关联文件
        OutputStream outputStream = new FileOutputStream(path);
        //实现序列化
        //注意:序列化后得到的内容不能直接查看,要看必须经过逆序列化
        outputStream.write(object.getBytes());
        //关闭流
        outputStream.close();
    }


    public synchronized static String ReaderFromFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }

    //    public  synchronized static Map<String,String>getFileTypeData(String fileurl) throws TaskMsgException {
//        if(StringUtils.isEmpty(fileurl))
//            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_URI_ERROR_CODE,TaskMsgConstant.TASK_MSG_URI_ERROR));
//
//        Map<String,String>data=new ConcurrentHashMap<>();
//        if(fileurl.trim().toLowerCase().startsWith("file://")){
//
//        }
//
//    }

    public static void main(String[] args) {
        createSyncerSetting("hello");
    }
}
