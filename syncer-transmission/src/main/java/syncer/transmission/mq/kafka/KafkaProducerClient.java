package syncer.transmission.mq.kafka;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import syncer.transmission.util.UrlUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
@Slf4j
public class KafkaProducerClient {
    private String servers;
    private KafkaProducer<String,String> producer;

    public KafkaProducerClient(String servers) {
        this.servers=servers;
        init();
    }

    /*
    初始化配置
     */
    private Properties initConfig(String servers){
        String[]data=servers.split("\\?");
        String address=servers;
        Properties props = new Properties();
        if(data.length>1){
            address=data[0];
            Map<String,String> params=UrlUtils.urlSplit(servers);
            String username=params.get("username");
            String password=params.get("password");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            props.put("security.protocol","SASL_PLAINTEXT");
            props.put("sasl.mechanism","SCRAM-SHA-256");
            props.put("sasl.jaas.config", jaasCfg);
        }

        props.put("bootstrap.servers", address);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }


    private void init(){
        System.out.println(servers);
        this.producer = new KafkaProducer<String, String>(initConfig(servers));
    }


    public boolean send(String topic,String key,String value){
        try {
            ProducerRecord<String,String> record = new ProducerRecord(topic, key,value);
//          producer.send(record);
            //发送消息
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (null != e){
                        log.error("send error {}",e.getMessage());
                    }
                }
            });
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public void close(){
        if(Objects.nonNull(producer)){
            producer.close();
        }
    }

}
