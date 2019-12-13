package syncer.syncerplusservice.task.clusterTask.command;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.*;


/**
 * cluster Nodes 工具类，主要基于cluter nodes命令生成Cluster集群的内网外网映射表
 * 解决内网搭建时
 */
public class ClusterNodesUtil {




    public static boolean isExSet(Set<String>set,String tar){
        if(set.contains(tar)){
            return true;
        }
        return false;
    }


    /**
     * 构建映射表
     * @param nodesMap
     * @param hostAndPorts
     * @param password
     */
    public static void builderMap1(Map<String,String>nodesMap,Set<HostAndPort>hostAndPorts,String password){
        Set<String>hostBSet=new HashSet<>();
        for (HostAndPort hap:hostAndPorts
        ) {
            hostBSet.add(hap.getHost());
        }

        for (HostAndPort hap:hostAndPorts
             ) {
            String oldHost=hap.getHost();
            Jedis jedis=new Jedis(hap);
            if(!StringUtils.isEmpty(password)){
                jedis.auth(password);
            }

            //连接获取每个redis节点
            jedis.connect();
            List<Object> slots = jedis.clusterSlots();
            jedis.close();


            //遍历每个地址
            for(Object slotInfoObj:slots){
                List<Object> slotInfo = (List<Object>) slotInfoObj;
                for (Object slot:slotInfo){
                    if(slot instanceof ArrayList){

                        List<Object>list= (List<Object>)slot;

                        for (Object ii:list){

                            if(ii instanceof  byte[]){

                                String hoty=new String((byte[]) ii);

                                if(!isExSet(hostBSet,hoty)){

                                    nodesMap.put(hoty,oldHost);

                                }else {
                                    nodesMap.put(hoty,hoty);
                                }
                                break;
                            }

                        }

                    }
                }

            }


        }




    }

    public static void builderMap(Map<String,String>nodesMap,Set<HostAndPort>hostAndPorts,String password){
        Set<String>hostBSet=new HashSet<>();
        for (HostAndPort hap:hostAndPorts
        ) {
            hostBSet.add(hap.getHost());
        }

        for (HostAndPort hap:hostAndPorts
        ) {
            String oldHost=hap.getHost();
            Jedis jedis=new Jedis(hap);
            if(!StringUtils.isEmpty(password)){
                jedis.auth(password);
            }

            //连接获取每个redis节点
            jedis.connect();
            List<Object> slots = jedis.clusterSlots();
            jedis.close();

            String data=jedis.clusterNodes();
//            System.out.println(data);
            List<String>addressList= Arrays.asList(data.split("\n"));


            //遍历每个地址

            for (String address:addressList
                 ) {
                try {
                    String[]add=address.split(" ");
                    String host=add[1].split(":")[0];
                    if(add[2].startsWith("myself")) {
                        nodesMap.put(host, hap.getHost());
                    }else {
                        nodesMap.put(host, host);
                    }
                }catch (Exception e){
                    nodesMap.put(hap.getHost(), hap.getHost());
                }


            }



        }




    }
}
