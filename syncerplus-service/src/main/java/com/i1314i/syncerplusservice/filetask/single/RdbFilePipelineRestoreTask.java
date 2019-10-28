package com.i1314i.syncerplusservice.filetask.single;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.FileType;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.event.EventListener;
import com.i1314i.syncerplusredis.event.PostRdbSyncEvent;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import com.i1314i.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import com.i1314i.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import com.i1314i.syncerplusredis.replicator.RedisReplicator;
import com.i1314i.syncerplusredis.replicator.Replicator;
import com.i1314i.syncerplusservice.util.SyncTaskUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class RdbFilePipelineRestoreTask implements Runnable {
    @Override
    public void run() {
        Replicator r = null;
        try {
            r = new JDRedisReplicator(null, FileType.ONLINERDB,"http://xiaoqi.i1314i.com/dump.rdb",Configuration.defaultSetting());

            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r));
            r.addEventListener(new ValueDumpIterableEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
                    if (event instanceof BatchedKeyValuePair<?, ?>) {
                        // do something
                    }
                }
            }));

            r.open();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IncrementException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        RedisURI redis=new RedisURI("redis:///path/to/dump.rdb?filetype=onlinerdb");
        System.out.println(redis.getFileType());


//        InputStream in = new BufferedInputStream(new FileInputStream("http://xiaoqi.i1314i.com/dump.rdb"));
        // 以流的形式下载文件。
//        InputStream fis = new BufferedInputStream(new FileInputStream("http://xiaoqi.i1314i.com/dump.rdb"));
//        byte[] buffer = new byte[fis.available()];
//        fis.read(buffer);
//        fis.close();


//        downLoadFromUrl("http://xiaoqi.i1314i.com/dump.rdb","","");
        // 清空response

//        System.out.println(buffer);
        try {
            Replicator r  = new JDRedisReplicator(null, FileType.ONLINERDB,"http://xiaoqi.i1314i.com/dump.rdb",Configuration.defaultSetting());

            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r));
            r.addEventListener(new ValueDumpIterableEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {
                    System.out.println(JSON.toJSONString(event));
                    if (event instanceof BatchedKeyValuePair<?, ?>) {
                        // do something
                    }

                    if (event instanceof PostRdbSyncEvent) {
                        System.out.println("全量同步结束");
                    }
                }
            }));

            r.open();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IncrementException e) {
            e.printStackTrace();
        }

    }


    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
//        File saveDir = new File(savePath);
//        if(!saveDir.exists()){
//            saveDir.mkdir();
//        }
//        File file = new File(saveDir+File.separator+fileName);
//        FileOutputStream fos = new FileOutputStream(file);

        System.out.println(getData);
//        fos.write(getData);
//        if(fos!=null){
//            fos.close();
//        }
//        if(inputStream!=null){
//            inputStream.close();
//        }


        System.out.println("info:"+url+" download success");

    }


    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {


            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
