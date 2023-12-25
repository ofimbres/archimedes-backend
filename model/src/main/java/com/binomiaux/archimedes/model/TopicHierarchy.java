package com.binomiaux.archimedes.model;

import lombok.Data;

import java.util.List;

@Data
public class TopicHierarchy {
    public TopicHierarchy(Topic topic) {
        this.id = topic.id();
        this.name = topic.name();
    }

    private String id;
    private String name;
    private List<TopicHierarchy> descendants;
}