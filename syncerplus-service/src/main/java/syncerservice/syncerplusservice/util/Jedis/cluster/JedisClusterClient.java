package syncerservice.syncerplusservice.util.Jedis.cluster;


import syncerservice.syncerplusservice.task.clusterTask.command.ClusterNodesUtil;
import syncerservice.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 集群方式redis客户端操作
 * @author 平行时空
 * @created 2018-06-14 22:10
 **/

@Slf4j
public class JedisClusterClient {
    private JedisClusterPlus jedis;
    private Set<String> jedisClusterNodes=null;
    public JedisClusterClient(String jedisaddress, String password, Integer maxTotal, Integer minIdle, long timeOut, int connectTimeout) throws ParseException {
        // 添加集群的服务节点Set集合
        Set<HostAndPort> hostAndPortsSet = new HashSet<HostAndPort>();
        // 添加节点

        String[]jedisClusterAddress=jedisaddress.split(";");
        jedisClusterNodes=new HashSet<>();
        for (String address:
                jedisClusterAddress) {
            log.info("NodeAddress:[{}]",address);
            jedisClusterNodes.add(address);

        }

        if(jedisClusterNodes == null || jedisClusterNodes.size() == 0){
            throw new NullPointerException("jedisClusterNodes is null.");
        }
        //构造结点
        for(String node:jedisClusterNodes){

            String[] arr = node.split(":");
            if(arr.length != 2){
                throw new ParseException("node address  error!", node.length()-1);
            }
            hostAndPortsSet.add(new HostAndPort(arr[0],Integer.valueOf(arr[1])));

        }



        // Jedis连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(minIdle);
        // 最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(maxTotal);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(minIdle);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(connectTimeout); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(true);
        Map<String,String>nodesMap=new HashMap<>();
        ClusterNodesUtil.builderMap(nodesMap,hostAndPortsSet,password);
        jedis = new JedisClusterPlus(hostAndPortsSet, jedisPoolConfig,nodesMap);
    }

    public JedisClusterPlus getJedis() {
        return jedis;
    }

}
