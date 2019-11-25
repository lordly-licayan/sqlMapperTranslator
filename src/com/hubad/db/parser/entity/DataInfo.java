package com.hubad.db.parser.entity;

import lombok.Data;

@Data
public class DataInfo {
    private String physicalName;
    private String logicalName;
    
    private String modifier;
    private String dataType;
    private String name;
}

