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
        Properties props = new Properties();
        props.put("bootstrap.servers", servers);
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
