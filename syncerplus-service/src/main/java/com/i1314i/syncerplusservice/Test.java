package com.i1314i.syncerplusservice;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusservice.util.file.FileUtils;


import java.util.Set;

public class Test {
//    public static void main(String[] args) throws IOException, URISyntaxException {
////        ?replId=111&replOffset=222
////        ?replId=fb0faf395bc214dfe3f1f6b9f30374bf2eb5f103&replOffset=2
//        RedisURI uri=new RedisURI("redis://114.67.100.239:6379?authPassword=redistest0102");
//        System.out.println(uri);
//        Replicator replicator = new JDReplicator("redis://114.67.100.239:6379?authPassword=redistest0102");
//        replicator.addEventListener(new EventListener() {
//
//            @Override
//            public void onEvent(Replicator replicator, Event event) {
//                System.out.println(JSON.toJSONString(event));
//            }
//        });
//
//        replicator.open();
//    }


    public static void main(String[] args) throws Exception {


    }

//    public static void main(String[] args) throws Exception {
//        Replicator r = new RedisReplicator("redis://114.67.100.239:6379?authPassword=redistest0102");
//        r.setRdbVisitor(new ValueIterableRdbVisitor(r));
//        r.addEventListener(new ValueIterableEventListener(new EventListener() {
//            @Override
//            public void onEvent(Replicator replicator, Event event) {
//                if (event instanceof BatchedKeyValuePair<?, ?>) {
//                    BatchedKeyValuePair event1= (BatchedKeyValuePair) event;
//                    event1.setBatch(1);
//
//                    System.out.println(JSON.toJSONString(new String((byte[]) event1.getValue())));
//                    System.out.println(JSON.toJSONString(event1.isLast()));
//                    // do something
//                }
//                if (event instanceof Command) {
//                    System.out.println(event);
//                }
//            }
//        }));
//        r.open();
//    }
}
