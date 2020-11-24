package syncer.syncerplusredis.entity.muli.multisync.dto;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 双向同步任务对象组装生成工具类
 * @Date 2020/10/12
 */
public class MuiltDtoToModelUtils {

    /**
     * taskId
     * 组装双向同步父类任务
     * @param data
     * @return
     */
    public static  ParentMultiTaskModel getParentMuiltiTaskModel(MuiltCreateTaskData data){
//        String text="127.0.0.1:6379?password=111&username=wddw\n" +
//                "127.0.0.1:6380?password=111&username=wddw";
        String taskId=TemplateUtils.uuid();
        List<MultiTaskModel> ApMultiTaskModelList=getMultiTaskModel(data.getSourceRedisAddress(),data,  taskId);
        List<MultiTaskModel> BpMultiTaskModelList=getMultiTaskModel(data.getTargetRedisAddress(),data,  taskId);
        List<MultiTaskModel>APNode=loadMultiTask(ApMultiTaskModelList,BpMultiTaskModelList);
        List<MultiTaskModel>BPNode=loadMultiTask(getMultiTaskModel(data.getTargetRedisAddress(),data,  taskId),getMultiTaskModel(data.getSourceRedisAddress(),data,  taskId));
        ParentMultiTaskModel parentMultiTaskModel=ParentMultiTaskModel.builder()
                .redisNodeA(APNode)
                .redisNodeB(BPNode)
                .taskName(data.getTaskName())
                .autostart(data.isAutostart()==true?1:0)
                .data(data)
                .taskId(taskId)
                .build();
        return parentMultiTaskModel;
    }

    /**
     * 组件节点子任务 parentId
     * @param uri
     * @param data
     * @return
     */
    static List<MultiTaskModel> getMultiTaskModel(String uri,MuiltCreateTaskData data, String taskId){
        String parentId= TemplateUtils.uuid();
        List<MultiTaskModel>multiTaskModelList=new ArrayList<>();
        String[]parentUriList=uri.split("\n");
        for (String parentUri:parentUriList){
            String nodeId= TemplateUtils.uuid();
            if(!StringUtils.isEmpty(parentUri)){
                String[]secUri=parentUri.split(":");
                System.out.println(secUri.length);
                String host=secUri[0];
                String port="6379";
                if(secUri.length==1){
                    host=secUri[0].split("\\?")[0];
                    secUri=secUri[0].split("\\?");
                }else {
                    secUri=secUri[1].split("\\?");
                    port=secUri[0];
                }
                if(secUri.length==1){
                    port="6379";
                    secUri=secUri[0].split("&");
                }else {
                    secUri=secUri[1].split("&");
                }

                //?authPassword=foobared&authUser=default
                StringBuilder stringUri=new StringBuilder("redis://").append(host).append(":").append(port).append("?");
                MultiTaskModel multiTaskModel=MultiTaskModel.builder().acl(false).host(host).port(Integer.valueOf(port)).build();
                for (String vdata:secUri
                     ) {
                    String key=vdata.split("=")[0];
                    String value=vdata.substring(vdata.indexOf("=")+1);
                    if("username".equalsIgnoreCase(key)&&!StringUtils.isEmpty(value)){
                        multiTaskModel.setAcl(true);
                        multiTaskModel.setUserName(value);
                        stringUri.append("authUser=").append(value).append("&");
                    }
                    if("password".equalsIgnoreCase(key)&&!StringUtils.isEmpty(value)){
                        multiTaskModel.setPassword(value);
                        stringUri.append("authPassword=").append(value).append("&");
                    }

                }
                stringUri.append("sys=redisyncer");
                multiTaskModel.setRedisAddress(stringUri.toString());
                multiTaskModel.setTaskName(data.getTaskName());
                multiTaskModelList.add(multiTaskModel);
                multiTaskModel.setOffset(-1L);

                multiTaskModel.setParentId(parentId);
                multiTaskModel.setTaskId(taskId);
                multiTaskModel.setNodeId(nodeId);
                multiTaskModel.setStatus(TaskStatusType.STOP.getCode());
                /**
                 * 暂时默认为单节点
                 */

                multiTaskModel.setTargetRedisType(1);

            }


        }
        return multiTaskModelList;
    }


    static  List<MultiTaskModel> loadMultiTask(List<MultiTaskModel> ApMultiTaskModelList,List<MultiTaskModel> BpMultiTaskModelList){
        return ApMultiTaskModelList.stream().map(multiTaskModel -> {
            if(BpMultiTaskModelList.size()>1){
                multiTaskModel.setHost(addressCon(BpMultiTaskModelList));
            }else {
                multiTaskModel.setHost(getAddresshost(BpMultiTaskModelList));
            }
            multiTaskModel.setPort(getPort(BpMultiTaskModelList));
            multiTaskModel.setPassword(getPassword(BpMultiTaskModelList));
            multiTaskModel.setStatus(TaskStatusType.STOP.getCode());
            return multiTaskModel;
        }).collect(Collectors.toList());
    }

    static String addressCon(List<MultiTaskModel> ApMultiTaskModelList){
        StringBuilder address=new StringBuilder();
        ApMultiTaskModelList.stream().forEach(multiTaskModel -> {
            address.append(multiTaskModel.getHost()).append(":").append(multiTaskModel.getPort()).append(";");
        });
        return address.toString();
    }


    static String getAddresshost(List<MultiTaskModel> ApMultiTaskModelList){
        return ApMultiTaskModelList.get(0).getHost();
    }


    static String getPassword(List<MultiTaskModel> ApMultiTaskModelList){
        return ApMultiTaskModelList.get(0).getPassword();
    }

    static Integer getPort(List<MultiTaskModel> ApMultiTaskModelList){
        return ApMultiTaskModelList.get(0).getPort();
    }
    public static void main(String[] args) {
        String text="127.0.0.1:6380?password=111&username=wddw\n" +
                "127.0.0.1:6380?password=111&username=wddw";
//        System.out.println(JSON.toJSONString(getMultiTaskModel(text,null)));
    }

}
