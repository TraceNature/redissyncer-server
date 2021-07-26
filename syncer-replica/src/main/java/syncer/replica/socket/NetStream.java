package syncer.replica.socket;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.config.ReplicConfig;

import javax.net.ssl.SSLHandshakeException;
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
@Slf4j
public class NetStream {
    public InputStream getInputStreamByOnlineFile(String FileUrl,ReplicConfig config) throws IOException {

        try {
            URL url = new URL(FileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为6秒
            conn.setConnectTimeout(6 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            try {
                config.setFileSize(conn.getContentLength());
            }catch (Exception e){
                log.error("获取在线数据文件大小失败...");
            }

            //得到输入流
            InputStream in = conn.getInputStream();
            return in;
        }catch (SSLHandshakeException e){
            log.error("data file download fail ,reason [{}]",e.getMessage());
            throw e;
        }
    }




}