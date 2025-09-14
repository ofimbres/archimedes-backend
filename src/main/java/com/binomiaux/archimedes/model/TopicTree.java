package com.binomiaux.archimedes.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class TopicTree {
    @NotNull(message = "Topic ID cannot be null")
    private String id;
    
    @NotNull(message = "Topic name cannot be null")
    private String name;
    
    @Valid
    private List<TopicTree> children;

    /**
     * Default constructor for JSON serialization/deserialization.
     */
    public TopicTree() {
        this.children = new ArrayList<>();
    }

    /**
     * Creates a TopicTree from a Topic model.
     */
    public TopicTree(@NotNull Topic topic) {
        this.id = topic.getTopicId();
        this.name = topic.getName();
        this.children = new ArrayList<>();
    }

    /**
     * Creates a TopicTree with specified id and name.
     */
    public TopicTree(@NotNull String id, @NotNull String name) {
        this.id = id;
        this.name = name;
        this.children = new ArrayList<>();
    }

    /**
     * Adds a child topic to this tree node.
     */
    public void addChild(TopicTree child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    /**
     * Checks if this topic has any children.
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    // Getters and setters
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

    public List<TopicTree> getChildren() {
        return children != null ? children : new ArrayList<>();
    }

    public void setChildren(List<TopicTree> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}