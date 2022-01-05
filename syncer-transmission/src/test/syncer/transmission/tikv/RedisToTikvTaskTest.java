package syncer.transmission.tikv;


import org.junit.Test;
import syncer.jedis.Jedis;

public class RedisToTikvTaskTest {
    @Test
    public void run(){
        Jedis jedis=new Jedis("127.0.0.1",6379);
        int i=30;
        while (i-- > 0) {
            if(i%2==0){
                jedis.set("key"+i,"value"+i);

            }else {
                jedis.sadd("set"+i,"seti"+i);
            }

        }
        jedis.close();
        RedisToTikvTask redisToTikvTask=new RedisToTikvTask("redis01","127.0.0.1",6379,"","114.67.120.120:2379");
        redisToTikvTask.run();
    }
}
