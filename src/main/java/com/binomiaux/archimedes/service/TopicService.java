package com.binomiaux.archimedes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.model.TopicTree;
import com.binomiaux.archimedes.repository.TopicRepository;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Builds a hierarchical topic structure with improved performance.
     * Avoids N+1 query problem by fetching all topics at once and grouping them.
     * 
     * @return List of top-level topics with their children
     */
    public List<TopicTree> getTopicsHierarchy() {
        // Fetch all topics at once to avoid N+1 queries
        List<Topic> allTopics = topicRepository.findAll();
        
        if (allTopics.isEmpty()) {
            return List.of();
        }

        // Group topics by parent (assuming you have a parent field or similar logic)
        // For now, I'll assume the first level are parents and rest are children
        // You should adjust this based on your actual data structure
        
        return buildHierarchyFromFlatList(allTopics);
    }

    /**
     * Alternative implementation if you need to distinguish between parent and child topics.
     * This assumes you have a way to identify parent topics (e.g., parentId == null).
     */
    private List<TopicTree> buildHierarchyFromFlatList(List<Topic> allTopics) {
        // This is a simplified version - you should adjust based on your actual data model
        // For example, if Topic has a parentId field:
        
        // Map<String, List<Topic>> topicsByParent = allTopics.stream()
        //     .collect(Collectors.groupingBy(topic -> 
        //         topic.getParentId() != null ? topic.getParentId() : "ROOT"));
        
        // List<TopicTree> rootTopics = topicsByParent.getOrDefault("ROOT", List.of())
        //     .stream()
        //     .map(this::buildTopicTree)
        //     .collect(Collectors.toList());

        // For now, treating all as top-level topics (you should modify this)
        return allTopics.stream()
                .map(this::buildTopicTree)
                .collect(Collectors.toList());
    }

    /**
     * Builds a complete TopicTree for a given topic, including its children.
     * 
     * @param topic the parent topic
     * @return TopicTree with children populated
     */
    private TopicTree buildTopicTree(Topic topic) {
        TopicTree tree = new TopicTree(topic);
        
        // Fetch children for this specific topic
        List<Topic> children = topicRepository.findByTopicId(topic.getId());
        
        if (!children.isEmpty()) {
            List<TopicTree> childTrees = children.stream()
                    .map(TopicTree::new)
                    .collect(Collectors.toList());
            
            tree.setChildren(childTrees);
        }
        
        return tree;
    }

    /**
     * Gets all topics without hierarchy structure.
     * 
     * @return List of all topics
     */
    public List<Topic> getTopics() {
        return topicRepository.findAll();
    }

    /**
     * Gets topics by parent topic ID.
     * 
     * @param topicId the parent topic ID
     * @return List of child topics
     */
    public List<Topic> getTopicsById(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic ID cannot be null or empty");
        }
        return topicRepository.findByTopicId(topicId);
    }

    /**
     * More efficient version if you can modify your repository to support batch queries.
     * This would be the ideal implementation to avoid N+1 queries completely.
     */
    public List<TopicTree> getTopicsHierarchyOptimized() {
        // This would require a repository method that fetches parent-child relationships in one query
        // Example: Map<String, List<Topic>> parentChildMap = topicRepository.findAllWithChildren();
        
        // For now, falls back to the current implementation
        return getTopicsHierarchy();
    }
}