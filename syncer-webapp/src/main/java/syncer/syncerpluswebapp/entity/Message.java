package syncer.syncerpluswebapp.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@ApiModel(value="消息model",description="消息model描述")
public class Message {
        @ApiModelProperty(name = "from",value = "消息來源",required = true, allowableValues = "all,bynames,byids,bystatus")
        private String from;
        @ApiModelProperty(value = "消息目標")
        private String to;
        @ApiModelProperty(value = "消息內容")
        private String content;
}


