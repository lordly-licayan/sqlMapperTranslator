package com.hubad.db.parser.conditions;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ConditionsCollection {
    private Map<String, ConditionInfo> tablePhysicalNameMap= new HashMap<String, ConditionInfo>();
    private Map<String, ConditionInfo> tableLogicalNameMap= new HashMap<String, ConditionInfo>();
}
