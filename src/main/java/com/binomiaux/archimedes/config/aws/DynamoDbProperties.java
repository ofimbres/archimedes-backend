package com.binomiaux.archimedes.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for DynamoDB operations.
 * Centralizes configuration values and reduces magic numbers in code.
 */
@Component
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbProperties {
    
    private String tableName;
    private int defaultMaxPeriods = 6;
    private String defaultType = "METADATA";
    
    // GSI names
    private String gsi1Name = "gsi1";
    private String gsi2Name = "gsi2";
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public int getDefaultMaxPeriods() {
        return defaultMaxPeriods;
    }
    
    public void setDefaultMaxPeriods(int defaultMaxPeriods) {
        this.defaultMaxPeriods = defaultMaxPeriods;
    }
    
    public String getDefaultType() {
        return defaultType;
    }
    
    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }
    
    public String getGsi1Name() {
        return gsi1Name;
    }
    
    public void setGsi1Name(String gsi1Name) {
        this.gsi1Name = gsi1Name;
    }
    
    public String getGsi2Name() {
        return gsi2Name;
    }
    
    public void setGsi2Name(String gsi2Name) {
        this.gsi2Name = gsi2Name;
    }
}
