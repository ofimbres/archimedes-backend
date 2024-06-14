package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.entities.TopicEntity;

public class TopicEntityTransformer implements EntityTransform<TopicEntity, Topic> {
    @Override
    public Topic transform(TopicEntity entity) {
        Topic model = new Topic(entity.getId(), entity.getTopicName());
        return model;
    }

    @Override
    public TopicEntity untransform(Topic model) {
        TopicEntity record = new TopicEntity();
        record.setId(model.id());
        record.setTopicName(model.name());

        return record;
    }
}
