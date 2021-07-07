package syncer.transmission.strategy.commandprocessing.factory;

import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.mq.kafka.KafkaProducerClient;
import syncer.transmission.strategy.commandprocessing.CommonProcessingStrategy;

import java.util.List;

public interface KafkaCommonProcessingStrategyListFactory {
    List<CommonProcessingStrategy> getStrategyList(TaskModel taskModel, KafkaProducerClient client);

}

