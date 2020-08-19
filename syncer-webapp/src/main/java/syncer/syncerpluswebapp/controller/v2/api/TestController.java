package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerservice.util.common.Montitor;

import java.math.BigDecimal;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/19
 */
@RestController
public class TestController {
    @Autowired
    Montitor montitor;
    @RequestMapping("/test/monitor")
    public String monitor(){
        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return montitor.jvmMemoryUsed()+":"+montitor.jvmMemoryMax()+ ": "+montitors;
    }
}
