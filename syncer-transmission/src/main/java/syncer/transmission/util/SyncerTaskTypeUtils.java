package syncer.transmission.util;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 判断是单向同步任务还是双向同步任务
 * @Date 2021/1/5
 */
public class SyncerTaskTypeUtils {
    /**
     * 双向任务任务ID  MULTI_TASK_ID_PARENT_ID_NODE_ID
     */

    final static String multiTypeString="MULTI_";
    public static boolean isMultiTask(String multiType){
        if(multiType.toUpperCase().startsWith(multiTypeString)){
            return true;
        }
        return false;
    }


    /**
     * 全局taskid转双向List
     * @param globalTaskId
     * @return
     */
    public static List<String> globalTaskId2TaskId(String globalTaskId){

        if(globalTaskId.startsWith(multiTypeString)){
            List<String>list= Lists.newArrayList();
            Arrays.asList(globalTaskId.split("_"))
                    .stream()
                    .filter(id->{
                        return !id.equalsIgnoreCase("MULTI");
                    }).forEach(data->{
                list.add(data);
            });
            return list;
        }
        return Lists.newArrayList();
    }





    /**
     * 生成双向全局taskId
     * @param taskId
     * @param parentId
     * @param nodeId
     * @return
     */
    public static String globalTaskId(String taskId,String parentId,String nodeId){
        StringBuilder stringBuilder=new StringBuilder(multiTypeString)
                .append(taskId)
                .append("_")
                .append(parentId)
                .append("_")
                .append(nodeId);
        return stringBuilder.toString();
    }

    /**
     * 生成双向single全局taskId
     * @param taskId
     * @param parentId
     * @return
     */
    public static String globalTaskId(String taskId,String parentId){
        StringBuilder stringBuilder=new StringBuilder(multiTypeString)
                .append(taskId)
                .append("_")
                .append(parentId);
        return stringBuilder.toString();
    }



    /**
     * 生成双向single全局taskId
     * @param taskId
     * @return
     */
    public static String globalSingleTaskId(String taskId){
        StringBuilder stringBuilder=new StringBuilder(multiTypeString)
                .append(taskId);
        return stringBuilder.toString();
    }
}
