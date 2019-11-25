package com.hubad.db.enums;

public enum PathEnum {
    
    DB_TABLE("_TBL.java"),
    JAVA_FILE(".java"),
    WORKSPACE("workspace"),
    
    WEB("web"),
    FILE_WEB("file-web"),
    ENTITY("entity"),
    CONDITION("condition"),
    
    CONFIG_FILE("config.properties"),
    
    OUTPUT_FILE("output.txt"),
    PROCESSED_SQL_FILE("processedSql.txt"),
    FEEDBACK_FILE("feedback.txt"),
    LOG_FILE("log.txt"),
    
    JAVA_SOURCE("\\src\\main\\java\\");
    
    
    private String value;
    
    PathEnum(String value){
        this.value= value;
    }
    
    public String getValue() {
        return this.value;
    }
}
