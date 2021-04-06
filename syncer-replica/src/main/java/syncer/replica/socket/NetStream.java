package syncer.replica.socket;

import lombok.Builder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
@Builder
public class NetStream {
    public InputStream getInputStreamByOnlineFile(String FileUrl) throws IOException {
        URL url = url = new URL(FileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为6秒
        conn.setConnectTimeout(6 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream in = conn.getInputStream();
        return in;
    }
}