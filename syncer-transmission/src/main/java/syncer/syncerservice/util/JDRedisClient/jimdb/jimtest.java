package syncer.syncerservice.util.JDRedisClient.jimdb;

import com.jd.jim.cli.*;
import com.jd.jim.cli.config.ConfigLongPollingClientFactory;
import com.jd.jim.cli.protocol.CommandType;
import com.jd.jim.cli.util.JimFutureUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/28
 */
public class jimtest {

    private static final String jimUrl = "jim://2581598003362223711/80000008";

    protected static Cluster client = null;


    public static void setUp() throws Exception {


    }


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        ConfigLongPollingClientFactory configClientFactory = new ConfigLongPollingClientFactory(
                "http://cfs.jim.jd.local/");
        ReloadableJimClientFactory factory = new ReloadableJimClientFactory();
        //不设置默认是0
        factory.setConfigId("0");
        factory.setJimUrl(jimUrl);

        factory.setConfigClient(configClientFactory.create());

        client = factory.getClient();
        client.set("STRING_KEY_1", "testSetAndGetAndDelAndExists_String_value");
        byte[][]data=new byte[][]{"jimdbtest".getBytes(),"testSetAndGetAndDelAndExists_String_value".getBytes()};
        Object res=client.sendCommand(CommandType.SET,data);
        System.out.println("result:"+res);
//        client.mSet();
        System.out.println(client.exists("STRING_KEY_1"));
        //String
//        client.del(TestConstanst.STRING_KEY_1);

//        Boolean rsBool = client.exists(TestConstanst.STRING_KEY_1);
//
//
//        //pipeline
//        PipelineClient pipelineClient = client.pipelineClient();
//        JimFuture<String> set = pipelineClient.set("STRING_KEY_1", "xx");
//        JimFuture<String> get = pipelineClient.get("STRING_KEY_1");
//        pipelineClient.flush();
//        List<JimFuture> futures = new ArrayList<JimFuture>();
//        futures.add(set);
//        futures.add(get);
//        for (JimFuture future : futures) {
//            System.out.println(future.get(2, TimeUnit.SECONDS));
//        }
//
//        if (JimFutureUtils.awaitAll(2, TimeUnit.SECONDS, set, get)) {
//            System.out.println(String.format("key[%s] value is %s", "STRING_KEY_1", get.get()));
//        }
//
//        //async
//        AsyncClient asyncClient = client.asyncClient();
//        asyncClient.set("STRING_KEY_1", "xx");
//        JimFuture<String> asyncGet = asyncClient.get("STRING_KEY_1");
//
//
//        asyncClient = client.asyncClient();
//        JimFuture<Long> del = asyncClient.del("STRING_KEY_1");
//        System.out.println(del.get(2, TimeUnit.SECONDS));
    }
}
