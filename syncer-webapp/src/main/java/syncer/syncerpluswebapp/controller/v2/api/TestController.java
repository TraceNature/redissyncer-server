package syncer.syncerpluswebapp.controller.v2.api;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import syncer.syncerservice.util.common.Montitor;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/19
 */
@RestController
public class TestController {
    @Resource
    private Montitor montitor;
    @RequestMapping(value = "/test/monitor",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiIgnore
    public String monitor(){
        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return montitor.jvmMemoryUsed()+":"+montitor.jvmMemoryMax()+ ": "+montitors;
    }
}
