package com.i1314i.syncerplusservice.task.clusterTask.command;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;


/**
 * cluster Nodes 工具类，主要基于cluter nodes命令生成Cluster集群的内网外网映射表
 * 解决内网搭建时
 */
public class ClusterNodesUtil {


    public static void main(String[] args) {
        Set<HostAndPort>hostAndPorts=new HashSet<>();
        hostAndPorts.add(new HostAndPort("114.67.83.131",8002));
        hostAndPorts.add(new HostAndPort("114.67.83.163",8002));
        hostAndPorts.add(new HostAndPort("114.67.100.240",8002));
        hostAndPorts.add(new HostAndPort("114.67.100.238",8002));
        hostAndPorts.add(new HostAndPort("114.67.100.239",8002));
        hostAndPorts.add(new HostAndPort("114.67.105.55",8002));
        Map<String,String>nodesMap=new HashMap<>();
        builderMap(nodesMap,hostAndPorts,"");
        System.out.println(JSON.toJSONString(nodesMap));
    }



    public static boolean isExSet(Set<String>set,String tar){
        if(set.contains(tar)){
            return true;
        }
        return false;
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

            //连接
            jedis.connect();
            List<Object> slots = jedis.clusterSlots();
            jedis.close();
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
}
