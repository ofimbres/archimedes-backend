package com.binomiaux.archimedes.model;


import java.util.List;

public class TopicHierarchy {
    private String id;
    private String name;
    private List<TopicHierarchy> descendants;

    public TopicHierarchy(Topic topic) {
        this.id = topic.getId();
        this.name = topic.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TopicHierarchy> getDescendants() {
        return descendants;
    }

    public void setDescendants(List<TopicHierarchy> descendants) {
        this.descendants = descendants;
    }
}