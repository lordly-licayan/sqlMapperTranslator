package com.hubad.db.enums;

public enum CommonEnum {
    
    BACK_SLASH("\\"),
    TAB("\t"),
    NEW_LINE("\n"),
    EMPTY(""),
    SEMI_COLON(";"),
    DOUBLE_COLON("::"),
    COMMA(","),
    DOUBLE_QUOTE("\""),
    ARROW(" := "),
    ID_REGEX("ＩＤ"),
    OPTIMISTIC_LOCK("楽観ロックID"),
    
    SPACE_REGEX("\\s+"),
    EQUAL_REGEX("[=]|[＝]"),
    
    OPEN_PARENTHESIS_REGEX("[\\(]|[（]"),
    CLOSE_PARENTHESIS_REGEX("[\\)]|[）]"),
    
    NOT_EQUAL_REGEX("[≠]"),
    LESS_THAN_EQUAL_REGEX("[≦]"),
    GREATER_THAN_EQUAL_REGEX("[≧]"),
    FULLWIDTH_LESS_THAN_REGEX("[＜]"),
    FULLWIDTH_GREATER_THAN_REGEX("[＞]"),
    LESS_THAN_OR_EQUAL_TO_REGEX("[≤]"),
    GREATER_THAN_OR_EQUAL_TO_REGEX("[≥]"),
    LESS_THAN_BUT_NOT_EQUAL_TO_REGEX("[≨]"),
    GREATER_THAN_BUT_NOT_EQUAL_TO_REGEX("[≩]"),

    NOT_EQUAL_SEPARATOR(" ≠ "),
    LESS_THAN_EQUAL_SEPARATOR(" ≦ "),
    GREATER_THAN_EQUAL_SEPARATOR(" ≧ "),
    FULLWIDTH_LESS_THAN_SEPARATOR(" ＜ "),
    FULLWIDTH_GREATER_THAN_SEPARATOR(" ＞ "),
    LESS_THAN_OR_EQUAL_TO_SEPARATOR(" ≤ "),
    GREATER_THAN_OR_EQUAL_TO_SEPARATOR(" ≥ "),
    LESS_THAN_BUT_NOT_EQUAL_TO_SEPARATOR(" ≨ "),
    GREATER_THAN_BUT_NOT_EQUAL_TO_SEPARATOR(" ≩ "),
    
    SPACE_SQL_REGEX("[　]|[ ]"),
    DOT("."),
    PARAM_DOT_REGEX("[．]|[.]"),
    SQL_PARAM_REGEX("[Ｐ.]|[P.]|[P:]|[P：]|[Ｐ：]"),
    SQL_PARAM_1("Ｐ："),
    SQL_PARAM_2("Ｐ."),
    SQL_PARAM_3("P."),
    SQL_PARAM_4("P:"),
    SQL_PARAM_5("P："),
    
    ID_PARAM("ID"),
    OPTIMISTIC_LOCK_ID("OPTIMISTIC_LOCK_ID"),
    OPEN_PARENTHESIS_FUNC(" \\( "),
    OPEN_PARENTHESIS_NO_SPACE_FUNC("\\( "),
    OPEN_PARENTHESIS_SEPARATOR(" ( "),
    CLOSE_PARENTHESIS_SEPARATOR(" ) "),
    COMMA_SEPARATOR(","),
    COMMA_SQL_SEPARATOR(" , "),
    AS_SQL_SEPARATOR(" AS "),
    AS_SEPARATOR("AS"),
    DOT_SQL_SEPARATOR("[．]"),
    DOT_SQL_SEPARATOR_SPACE("[. ]"),
    EQUAL_SQL_SEPARATOR(" = "),
    DOT_SEPARATOR("\\."),
    SPACE_SEPARATOR(" "),
    EQUAL_SEPARATOR("="),
    
    CONDITION_SIGNAL("コンディション"),
    CONDITION_START("「"),
    CONDITION_END("」"),
    CONDITION_DATA_START("("),
    CONDITION_DATA_END(")"),
    CONDITION_DATA_SIGNAL("[(]|[)]|[,]"),
    CONDITION_CLASS_START("public enum"),
    CONDITION_CLASS_END("implements condition"),
    
    EXTRACTION_ITEMS("extractionItems"),
    EXTRACTION_TABLE("extractionTable"),
    COMBINATION_CONDITION("combinationCondition"),
    EXTRACTION_CONDITION("extractionCondition"),
    SORT_CONDITIONS("sortConditions");
 
    private String value;
    
    CommonEnum(String value){
        this.value= value;
    }
    
    public String getValue() {
        return this.value;
    }
}
