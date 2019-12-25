package syncer.syncerservice.util.jedis;


import org.springframework.beans.factory.FactoryBean;
import syncer.syncerjedis.HostAndPort;
import syncer.syncerjedis.JedisCluster;
import syncer.syncerjedis.JedisPoolConfig;
import syncer.syncerservice.util.jedis.cluster.ClusterNodesUtil;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 平行时空
 * @created 2018-06-14 22:15
 **/
public class JedisClusterFactory implements FactoryBean<JedisCluster> {

    //连接池参数 spring 注入

    private JedisPoolConfig jedisPoolConfig = null;
    //
    private JedisCluster jedisCluster=null;
    private int connectionTimeout = 2000;
    private int soTimeout = 3000;
    private String passWord="";

    private int maxRedirections = 10;
    //redis结点列表 spring注入
    private Set<String> jedisClusterNodes;

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    @Override
    public JedisCluster getObject() throws Exception {
        return jedisCluster;
    }

    @Override
    public Class<?> getObjectType() {
        return (this.jedisCluster != null ? this.jedisCluster.getClass()
                : JedisCluster.class);
    }

    @Override
    public boolean isSingleton() {
        // TODO Auto-generated method stub
        return true;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public JedisCluster getJedisCluster() throws ParseException {
        //判断地址是否为空
        if(jedisClusterNodes == null || jedisClusterNodes.size() == 0){
            throw new NullPointerException("jedisClusterNodes is null.");
        }
        //构造结点
        Set<HostAndPort> haps = new HashSet<HostAndPort>();
        for(String node:jedisClusterNodes){
            String[] arr = node.split(":");
            if(arr.length != 2){
                throw new ParseException("node address error!", node.length()-1);
            }
            haps.add(new HostAndPort(arr[0],Integer.valueOf(arr[1])));
        }

        Map<String,String>nodesMap=new HashMap<>();
        ClusterNodesUtil.builderMap(nodesMap,haps,passWord);

        if(passWord==null||"".equals(passWord.trim())){
            jedisCluster = new JedisCluster(haps, connectionTimeout, soTimeout, maxRedirections,jedisPoolConfig,nodesMap);
        }else {
            jedisCluster = new JedisCluster(haps, connectionTimeout, soTimeout, maxRedirections, passWord,jedisPoolConfig,nodesMap);
        }


        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getMaxRedirections() {
        return maxRedirections;
    }

    public void setMaxRedirections(int maxRedirections) {
        this.maxRedirections = maxRedirections;
    }

    public Set<String> getJedisClusterNodes() {
        return jedisClusterNodes;
    }

    public void setJedisClusterNodes(Set<String> jedisClusterNodes) {
        this.jedisClusterNodes = jedisClusterNodes;
    }
}
