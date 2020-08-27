package syncer.syncerpluswebapp.thread;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerpluscommon.log.LoggerMessage;
import syncer.syncerpluscommon.log.LoggerQueue;

import javax.websocket.Session;
import java.io.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/27
 */
@Slf4j
public class TailLogThread  extends Thread {

    private Process process;
    private String filePath;
    private BufferedReader reader;
    private InputStream in;
    private LoggerQueue logQueue = LoggerQueue.getInstance();
    public TailLogThread(String filePath) {
        this.filePath=filePath;
    }

    @Override
    public void run() {

        try {
            File file = new File(filePath);
            if(file.exists()){
                log.info("文件日志找到：" + filePath);
                process = Runtime.getRuntime().exec("tail -f " + filePath);
                logQueue.push(LoggerMessage.builder().body("等待载入文件......" + "<br>").build());

                in = process.getInputStream();
            }else{
                logQueue.push(LoggerMessage.builder().body("<a style='color: #ff0000'>文件没有找到</a>" + "<br>").build());
                log.error("日志文件没有找到");
                return;
            }
            this.reader = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = reader.readLine()) != null) {
                // 将实时日志通过WebSocket发送给客户端，给每一行添加一个HTML换行
                logQueue.push(LoggerMessage.builder().body(line + "<br>").build());
            }
        } catch (EOFException e1) {
            try {
                logQueue.push(LoggerMessage.builder().body("客户端已经关闭!" + "<br>").build());
            } catch (Exception e) {
                log.error("服务流关闭提示消息发送报错：{}" ,e.getMessage(), e);
            }
            log.info("出现了EOFExcption:：服务流已经关闭!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}