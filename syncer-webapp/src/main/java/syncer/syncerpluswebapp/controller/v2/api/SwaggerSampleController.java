package syncer.syncerpluswebapp.controller.v2.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluswebapp.entity.Echo;
import syncer.syncerpluswebapp.entity.Message;

import java.util.concurrent.atomic.AtomicLong;

@Api(value = "/SwaggerSample")
@RestController
@RequestMapping("/SwaggerSample")
public class SwaggerSampleController {
    private final AtomicLong counter = new AtomicLong();
    private static final String echoTemplate2 = "%s speak to %s %s";

    @ApiOperation("Post示例")
    @RequestMapping(value = "/postsample", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Echo setterMessage1(@RequestBody Message message) {
        return new Echo(counter.incrementAndGet(), String.format(echoTemplate2, message.getFrom(), message.getTo(), message.getContent()));
    }

    @ApiOperation("验证ResultMap")
    public ResultMap setResultMap(){
        return new ResultMap();
    }

}
