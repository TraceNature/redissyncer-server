package syncer.syncerservice.service;

import syncer.syncerpluscommon.entity.ResultMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/24
 */
public interface IUserService {
     ResultMap login();
     String logout();

}
