package syncer.transmission.tikv;


import org.junit.Test;

public class RedisToTikvTaskTest {
    @Test
    public void run(){
        RedisToTikvTask redisToTikvTask=new RedisToTikvTask("testInstanceId","127.0.0.1",6379,"","114.67.120.120:2379");
        redisToTikvTask.run();
    }
}
