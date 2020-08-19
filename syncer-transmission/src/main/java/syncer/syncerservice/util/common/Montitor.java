package syncer.syncerservice.util.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.itit.itf.okhttp.FastHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import syncer.syncerpluscommon.util.common.TokenNameUtils;
import syncer.syncerservice.config.ServerConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/17
 */
@Component
public class Montitor {
    @Autowired
    ServerConfig serverConfig;

    public  long jvmMemoryMax(){
        try {

            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getMax();
//            String jsonResult=FastHttpClient
//                    .get()
//                    .addHeader(TokenNameUtils.TOKEN_NAME,"tokenuuidpreqwertgnkoipudw")
//                    .url(serverConfig.getUrl()+"/actuator/metrics/jvm.memory.max?tag=area:heap").build().execute().body().string();
//            JSONObject jsonObject= JSON.parseObject(jsonResult);
//            long value=jsonObject.getJSONArray("measurements").getJSONObject(0).getLongValue("value");
//            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


    public  long jvmMemoryUsed(){
        try {
//            String jsonResult=FastHttpClient.get().url(serverConfig.getUrl()+"/actuator/metrics/jvm.memory.used?tag=area:heap").build().execute().body().string();
//            JSONObject jsonObject= JSON.parseObject(jsonResult);
//            long value=jsonObject.getJSONArray("measurements").getJSONObject(0).getLongValue("value");
//            return value;
            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            return heapMemoryUsage.getUsed();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }


}
