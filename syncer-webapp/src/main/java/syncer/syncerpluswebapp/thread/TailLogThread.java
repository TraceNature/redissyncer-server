package syncer.syncerpluswebapp.thread;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerpluscommon.log.LoggerMessage;
import syncer.syncerpluscommon.log.LoggerQueue;

import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/27
 */
@Slf4j
public class TailLogThread extends Thread {


    private BufferedReader reader;
    private Session session;

    public TailLogThread(InputStream in, Session session) {
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.session = session;

    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // 将实时日志通过WebSocket发送给客户端，给每一行添加一个HTML换行
                LoggerMessage log = LoggerQueue.getInstance().poll();
                session.getBasicRemote().sendText(line + "<br>");
            }
        } catch (EOFException e1) {
            try {
                session.getBasicRemote().sendText("客户端已经关闭!" + "<br>");
            } catch (Exception e) {
                log.error("服务流关闭提示消息发送报错：" + e.getMessage(), e);
            }
            log.info("出现了EOFExcption:：服务流已经关闭!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}