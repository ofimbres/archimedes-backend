package com.binomiaux.archimedes.repository.transform;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.schema.TopicRecord;

public class TopicRecordTransformer implements RecordTransform<TopicRecord, Topic> {
    @Override
    public Topic transform(TopicRecord entity) {
        Topic model = new Topic(entity.getId(), entity.getTopicName());
        return model;
    }

    @Override
    public TopicRecord untransform(Topic model) {
        TopicRecord record = new TopicRecord("TOPIC", "TOPIC#", model.id(), model.name());
        return record;
    }
}
