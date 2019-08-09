package com.i1314i.syncerplusservice.task.clusterTask.command;


import com.i1314i.syncerplusservice.util.Jedis.cluster.SyncJedisClusterClient;
import com.i1314i.syncerplusservice.util.Jedis.cluster.extendCluster.JedisClusterPlus;
import redis.clients.jedis.Protocol;

import java.text.ParseException;

public class Test {


    public static void main(String[] args) throws ParseException {

//        System.out.println( client.getResource().sendCommand(Protocol.Command.SET,new String[]{"testee","test"}));
//
        SyncJedisClusterClient clusterClient=new SyncJedisClusterClient( "114.67.100.240:8002;114.67.100.239:8002;114.67.100.238:8002;114.67.83.131:8002;114.67.105.55:8002;114.67.83.163:8002",
                "",100,50,10000,100000);
//        System.out.println(clusterClient.jedisCluster().get("A"));
//        clusterClient.builder(clusterClient);
        JedisClusterPlus jedisCluster=clusterClient.getJedisCluster();
//        byte[][]bytes=new byte[0][2];



        System.out.println(new String((byte[]) clusterClient.jedisCluster().sendCommand("myself", Protocol.Command.SET,new String[]{"myself","myself"})));
        System.out.println(clusterClient.jedisCluster().get("myself"));
        System.out.println(clusterClient.allkeys("*"));
//        Thread thread=new Thread(new ARun());
//        thread.start();
    }
}
