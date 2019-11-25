package com.hubad.db.enums;

public enum SqlMapperEnum {
    EXTRACTION_ITEMS("【抽出項目】"),
    EXTRACTION_TABLE("【抽出テーブル】"),
    COMBINATION_CONDITION("【結合条件】"),
    EXTRACTION_CONDITION("【抽出条件】"),
    SORT_CONDITIONS("【ソート条件】"),
    
    EXTRACTION_ITEMS_EN("[EXTRACTION ITEMS]"),
    EXTRACTION_TABLE_EN("[EXTRACTION TABLES]"),
    COMBINATION_CONDITION_EN("[COMBINATION CONDITION]"),
    EXTRACTION_CONDITION_EN("[EXTRACTION_CONDITION]"),
    SORT_CONDITIONS_EN("[SORT CONDITIONS]"),
    
    EXCLUDED_WORDS("excludedWordsForFeedbacking"),
    EXCLUDED_WORDS_LIST("SELECT,FROM,(,),MAX,WHERE,GROUP,SUM,<,>,UNION,/*,*/,ORDER,"),
    
    NO_INDENTION_WORDS("noIndentionWords"),
    NO_INDENTION_WORDS_LIST("SELECT,FROM,WHERE,ORDER,"),
    
    COMMENT("--"),
    ON_SQL("ON"),
    JOIN_SIGNAL("JOIN");
    
    
    private String value;
    
    SqlMapperEnum(String value){
        this.value= value;
    }
    
    public String getValue() {
        return this.value;
    }
}
