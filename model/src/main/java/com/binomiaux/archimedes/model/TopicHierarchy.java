package com.binomiaux.archimedes.model;

import java.util.List;

public record TopicHierarchy(String id, String name, List<TopicHierarchy> descendants) { }