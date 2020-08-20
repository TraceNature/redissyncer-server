package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/2
 */
//@Controller
public class LogController {
    @RequestMapping(value = "/log")
    public String logPage(){
        return "/log";
    }





}
