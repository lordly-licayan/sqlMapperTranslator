package com.hubad.db.parser.conditions;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ConditionInfo {
    
    private String physicalName;
    private String logicalName;
    
    //Indexed using the physicalName of the condition.
    private Map<String, ConditionItemInfo> conditionPhysicalNameMap= new HashMap<String, ConditionItemInfo>();
    
    //Indexed using the logicalName of the condition.
    private Map<String, ConditionItemInfo> conditionLogicalNameMap= new HashMap<String, ConditionItemInfo>();
}
