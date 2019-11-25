package com.hubad.db.parser.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TableInfo {
    
    private String physicalName;
    private String logicalName;
    
    //Indexed using the physicalName of columns/data members.
    private Map<String, DataInfo> columnPhysicalNameMap= new HashMap<String, DataInfo>();
    
    //Indexed using the logicalName of columns/data members.
    private Map<String, DataInfo> columnLogicalNameMap= new HashMap<String, DataInfo>();
}
