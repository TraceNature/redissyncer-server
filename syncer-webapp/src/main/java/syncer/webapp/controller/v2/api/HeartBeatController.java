package syncer.webapp.controller.v2.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Eq Zhan
 * @create: 2021-03-11
 **/
@RestController
public class HeartBeatController {
    @GetMapping(value = "/health")
    public String heartBeat(){
        return "200";
    }
}
