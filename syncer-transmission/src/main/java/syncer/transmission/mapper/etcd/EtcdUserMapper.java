package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ibm.etcd.api.KeyValue;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.UserMapper;
import syncer.transmission.model.UserModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * /tasks/user/{username}  {"id":"","username":"","name":"","password":"","salt":""}
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/
@Builder
@Data
@Slf4j
public class EtcdUserMapper implements UserMapper {
    private JEtcdClient client ;
    private String nodeId;

    @Override
    public List<UserModel> selectAll() throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getUserPrefix());
        List<UserModel>userModelList= Lists.newArrayList();
        if(Objects.nonNull(keyValueList)){
            userModelList.addAll(keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),UserModel.class);
            }).collect(Collectors.toList()));
        }
        return userModelList;
    }

    @Override
    public UserModel findUserById(int id) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getUserPrefix());
        if(Objects.nonNull(keyValueList)){
            List<UserModel> userModelList=keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),UserModel.class);
            }).collect(Collectors.toList()).stream().filter(k->{
                return k.getId()==id;
            }).collect(Collectors.toList());
            if(userModelList.size()>0){
                return userModelList.get(0);
            }
        }
        return null;
    }

    @Override
    public List<UserModel> findUserByUsername(String username) throws Exception {
        List<KeyValue>keyValueList=client.getPrefix(EtcdKeyCmd.getUserByUserName(username));
        if(Objects.nonNull(keyValueList)){
            return keyValueList.stream().map(keyValue -> {
                return JSON.parseObject(keyValue.getValue().toStringUtf8(),UserModel.class);
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean updateUserPasswordById(int id, String password) throws Exception {
        UserModel userModel=findUserById(id);
        userModel.setPassword(password);
        client.put(EtcdKeyCmd.getUserByUserName(userModel.getUsername()),JSON.toJSONString(userModel));
        return true;
    }
}
