package com.i1314i.syncerplusservice.util.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

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

        if(existsFile(settingPath)){
            if(TaskMonitorUtils.getAliveThreadHashMap()!=null&&TaskMonitorUtils.getAliveThreadHashMap().size()>0)
                   saveTextFile(JSON.toJSONString(TaskMonitorUtils.getAliveThreadHashMap(), SerializerFeature.DisableCircularReferenceDetect),settingPath);
        }
    }


    public static void main(String[] args) {
        createSyncerSetting("hello");
    }
}
