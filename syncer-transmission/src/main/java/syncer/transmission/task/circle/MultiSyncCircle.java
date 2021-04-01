// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.task.circle;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import syncer.common.util.MD5Utils;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.parser.syncer.datatype.DumpKeyValuePairEvent;
import syncer.replica.util.strings.Strings;
import syncer.transmission.util.strings.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 破环类
 * @Date 2021/1/5
 */
@Data
@Builder
public class MultiSyncCircle {
    private Map<String, Map<String, AtomicLong>>nodeGroupData;

    private Map<String, AtomicInteger>dbData;

    private Map<String, FlushCommandStatus>flushCommandStatus;

    @Builder.Default
    private AtomicInteger nodeStatus=new AtomicInteger(0);
    @Builder.Default
    private AtomicBoolean nodeSuccessStatus=new AtomicBoolean(true);
    private int nodeCount;
    @Builder.Default
    private AtomicInteger nodeSuccessStatusType=new AtomicInteger(0);

    public MultiSyncCircle(Map<String, Map<String, AtomicLong>> nodeGroupData, Map<String, AtomicInteger> dbData, Map<String, FlushCommandStatus> flushCommandStatus, AtomicInteger nodeStatus, AtomicBoolean nodeSuccessStatus, int nodeCount,AtomicInteger nodeSuccessStatusType) {
        this.nodeGroupData = nodeGroupData;
        this.dbData = dbData;
        this.flushCommandStatus = flushCommandStatus;
        this.nodeStatus = nodeStatus;
        this.nodeSuccessStatus = nodeSuccessStatus;
        this.nodeCount = nodeCount;
        this.nodeSuccessStatusType= nodeSuccessStatusType;
    }


    /**
     * 判断是否是辅助key
     *
     * @param defaultCommand
     * @param serverId
     * @return
     */
    public boolean isCircleKey(DefaultCommand defaultCommand, String serverId) {
        String key = new StringBuilder("circle-").append(serverId).append("-").toString();
        //判断辅助key
        if (defaultCommand.getCommand() != null && Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("PSETEX")) {
            if (defaultCommand.getArgs() != null) {
                String[] data = Strings.byteToString(defaultCommand.getArgs());
                List<String> dataList = Arrays.asList(data).stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());
                if (dataList.get(0).startsWith(key)) {
                    return true;
                }
            }
        } else if (defaultCommand.getCommand() != null && Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("DEL")) {
            if (defaultCommand.getArgs() != null && defaultCommand.getArgs().length > 0) {
                String[] data = Strings.byteToString(defaultCommand.getArgs());
                List<String> dataList = Arrays.asList(data).stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());
                if (dataList.get(0).startsWith(key)) {
                    return true;
                }

            }
        }
        return false;
    }

    public String getMd5(DefaultCommand defaultCommand, String serverId) {
        String command=Strings.byteToString(defaultCommand.getCommand()).toUpperCase();

        if("RESTORE".equalsIgnoreCase(command)){
            List<String>list=restoreCommandValues(defaultCommand.getArgs());
            return getRdbDumpMd5(list.get(0).getBytes(),list.get(2).getBytes(),Long.valueOf(list.get(1)),serverId,3.0);
        }else{
            return getCommandMd5(defaultCommand,serverId);
        }
    }

    /**
     * 获取restore 命令各个参数
     * RESTORE key ttl serialized-value [REPLACE]
     * @param value
     * @return
     */
    List<String> restoreCommandValues(byte[][]value){
        List<String>list= Lists.newArrayList();
        String[] commandParam=Strings.byteToString(value);
        for (int i=0;i<commandParam.length;i++){
            if(!StringUtils.isEmpty(commandParam[i])){
                list.add(commandParam[i]);
            }
        }
        return list;
    }
    /**
     * 根据增量Event获取MD5值
     * @param defaultCommand
     * @param serverId
     * @return
     */
    public String getCommandMd5(DefaultCommand defaultCommand, String serverId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("circle-");
        stringBuilder.append(serverId);
        stringBuilder.append("-");
        /**
         if (defaultCommand.getArgs() != null && defaultCommand.getArgs().length > 0) {
         //            if(Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("SELECT")){
         //                stringBuilder.append("SELECT") ;
         //            }else if(Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("DEL")){
         //                stringBuilder.append("DEL") ;
         //            }
         //           stringBuilder.append(Strings.byteToString(defaultCommand.getArgs())[0]) ;
         stringBuilder.append(Strings.byteToString(defaultCommand.getCommand()));
         stringBuilder.append("-");
         }

         **/
        stringBuilder.append(Strings.byteToString(defaultCommand.getCommand()));
        stringBuilder.append("-");

        stringBuilder.append(MD5Utils.getMD5(getStringCommand(defaultCommand)));
        return stringBuilder.toString().trim();
    }


    /**
     *
     * 根据增量Event获取String
     * @param defaultCommand
     * @return
     */
    String getStringCommand(DefaultCommand defaultCommand) {
        StringBuilder stringBuilder = new StringBuilder();
        if (defaultCommand.getCommand() != null) {
            stringBuilder.append(Strings.byteToString(defaultCommand.getCommand()).toUpperCase());
        }
        if (defaultCommand.getArgs() != null && defaultCommand.getArgs().length > 0) {
            String[] data = Strings.byteToString(defaultCommand.getArgs());
            for (String str : data
            ) {
                if (StringUtils.isEmpty(str)) {
                    continue;
                }
                stringBuilder.append(str);
            }
        }
        return replace(stringBuilder.toString());
    }


    /**
     *
     * @param dumpKeyValuePair
     * @return
     */
    String getStringRdbDump(DumpKeyValuePairEvent dumpKeyValuePair) {
        return getBaseStringRdbDump(dumpKeyValuePair.getKey(),dumpKeyValuePair.getValue(),dumpKeyValuePair.getExpiredMs());
    }


    String getBaseStringRdbDump(byte[]key,byte[]value,Long expiredMs) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Objects.nonNull(key)) {
            stringBuilder.append(Strings.byteToString(key).toUpperCase());
        }
        if(Objects.nonNull(value)){
            stringBuilder.append(Strings.byteToString(value));
        }
//        if(Objects.nonNull(expiredMs)){
//            stringBuilder.append(expiredMs);
//        }
        return replace(stringBuilder.toString());
    }


    /**
     *  根据RDB dump获取md5
     * @param key
     * @param value
     * @param expiredMs
     * @param serverId
     * @param redisVersion
     * @return
     */
    public String getRdbDumpMd5(byte[]key,byte[]value,Long expiredMs, String serverId,double redisVersion) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("circle-");
        stringBuilder.append(serverId);
        stringBuilder.append("-");
        stringBuilder.append("RESTORE");
        stringBuilder.append("-");
        stringBuilder.append(MD5Utils.getMD5(getBaseStringRdbDump(key,value,expiredMs)));
        return stringBuilder.toString().trim();
    }

    public String getRdbDumpMd5(DumpKeyValuePairEvent dumpKeyValuePair, String serverId, double redisVersion) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("circle-");
        stringBuilder.append(serverId);
        stringBuilder.append("-");
        stringBuilder.append("RESTORE");
        stringBuilder.append("-");
        stringBuilder.append(MD5Utils.getMD5(getStringRdbDump(dumpKeyValuePair)));
        return stringBuilder.toString().trim();
    }


    String replace(String str) {
        String destination = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            destination = m.replaceAll("");
        }
        return destination;
    }


    public void addDataMap(String nodeId, String circleKey) {
        Map<String, AtomicLong> map=getDataMap(nodeId);
        circleKey=circleKey.trim();
        if (map.containsKey(circleKey)) {
            map.get(circleKey).incrementAndGet();
        } else {
            map.put(circleKey, new AtomicLong(1));
        }
    }

    public void removeDataMap(Map<String, AtomicLong> map, String circleKey) {
        circleKey=circleKey.trim();
        if (map.containsKey(circleKey)) {
            AtomicLong data = map.get(circleKey);
            if (data.get() <= 1) {
                map.remove(circleKey);
            } else {
                data.decrementAndGet();
            }


        }
    }
    public void removeDataMap(String nodeId, String circleKey) {
        Map<String, AtomicLong> map=getDataMap(nodeId);
        circleKey=circleKey.trim();
        if (map.containsKey(circleKey)) {
            AtomicLong data = map.get(circleKey);
            if (data.get() <= 1) {
                map.remove(circleKey);
            } else {
                data.decrementAndGet();
            }
        }
    }

    public boolean isContinueTask(){
        return nodeStatus.get()>=nodeCount;
    }

    public Map<String, AtomicLong> getDataMap(String nodeId) {
        return nodeGroupData.get(nodeId);
    }

}
