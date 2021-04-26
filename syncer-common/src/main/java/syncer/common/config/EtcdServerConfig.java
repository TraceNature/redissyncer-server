package syncer.common.config;

import com.alibaba.fastjson.JSON;
import sun.net.util.IPAddressUtil;
import syncer.common.constant.BreakpointContinuationType;
import syncer.common.constant.StoreType;
import syncer.common.util.spring.SpringUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: Eq Zhan
 * @create: 2021-03-04
 **/
public class EtcdServerConfig {
    private Config config= SpringUtil.getBean(Config.class);
    private EtcdAuthConfig etcdAuthConfig=SpringUtil.getBean(EtcdAuthConfig.class);

    public String getUrl() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "http://"+address.getHostAddress() +":"+config.getPort();
    }

    public String getHost(){
        if(ipCheck(config.getNodeAddr())){
            return config.getNodeAddr();
        }

        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address.getHostAddress();
    }


    /**
     * 校验ip地址是否正确
     * @param ipStr
     * @return
     */
    public static  boolean ipCheck(String ipStr){
        boolean iPv4LiteralAddress = IPAddressUtil.isIPv4LiteralAddress(ipStr);
        boolean iPv6LiteralAddress = IPAddressUtil.isIPv6LiteralAddress(ipStr);
        //ip有可能是v4,也有可能是v6,滿足任何一种都是合法的ip
        if (!(iPv4LiteralAddress||iPv6LiteralAddress)){
            return false;
        }
        return true;
    }


    /**
     * 获取存储类型
     * @return
     */
    public StoreType getStoreType(){
        if("sqlite".equalsIgnoreCase(config.getStorageType())){
            return StoreType.SQLITE;
        }
        return StoreType.ETCD;
    }


    public String getNodeType(){
        return config.getNodetype();
    }

    public String getNodeId(){
        return config.getNodeId();
    }

    public int getLocalPort() throws Exception {
        return config.getPort();
    }

    /**
     * 是否已单节点部署
     * @return
     */
    public boolean isSingleNode(){
        return config.isSingleNode();
    }


    /**
     * 获取etcd配置
     * @return
     */
    public EtcdAuthConfig getEtcdConfig(){
        return this.etcdAuthConfig;
    }


    public BreakpointContinuationType getBreakpointContinuationType(){
        return config.getBreakpointContinuationType();
    }

}
