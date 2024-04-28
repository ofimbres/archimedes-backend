package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.dynamodb.TopicEntity;
import com.binomiaux.archimedes.model.pojo.Topic;

public class TopicEntityConverter implements EntityConverter<TopicEntity, Topic> {
    @Override
    public Topic transform(TopicEntity entity) {
        Topic model = new Topic(entity.getId(), entity.getTopicName());
        return model;
    }

    @Override
    public TopicEntity untransform(Topic model) {
        TopicEntity record = new TopicEntity("TOPIC", "TOPIC#", model.id(), model.name());
        return record;
    }
}
