package syncer.common.config;

import syncer.common.constant.BreakpointContinuationType;

public class BreakPointConfig {
    static EtcdServerConfig config=new EtcdServerConfig();


    public static BreakpointContinuationType getBreakpointContinuationType(){
        return config.getBreakpointContinuationType();
    }
}
