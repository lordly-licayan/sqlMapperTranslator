package com.hubad.db.parser.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class DatabaseInfo {
    private Map<String, TableInfo> tablePhysicalNameMap= new HashMap<String, TableInfo>();
    private Map<String, TableInfo> tableLogicalNameMap= new HashMap<String, TableInfo>();
}
