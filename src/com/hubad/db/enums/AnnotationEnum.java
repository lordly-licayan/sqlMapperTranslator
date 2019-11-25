package com.hubad.db.enums;

public enum AnnotationEnum {

    TABLE("@DatabaseTableInfo"),
    COLUMN("@DatabaseColumnInfo");
    
    private String value;
    
    AnnotationEnum(String value){
        this.value= value;
    }
    
    public String getValue() {
        return this.value;
    }
}
