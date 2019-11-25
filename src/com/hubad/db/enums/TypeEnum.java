package com.hubad.db.enums;

public enum TypeEnum {
    
    PHYSICAL_NAME("physicalName"),
    LOGICAL_NAME("logicalName");
    
    private String value;
    
    TypeEnum(String value){
        this.value= value;
    }
    
    public String getValue() {
        return this.value;
    }
}
