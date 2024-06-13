package com.binomiaux.archimedes.repository.converter;

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
        TopicRecord record = new TopicRecord();
        record.setId(model.id());
        record.setTopicName(model.name());

        return record;
    }
}
