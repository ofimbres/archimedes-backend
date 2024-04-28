package com.binomiaux.archimedes.model.pojo;

import java.util.List;

public record TopicHierarchy(String id, String name, List<TopicHierarchy> descendants) { }