package com.hubad.db.parser.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.SqlMapperEnum;
import com.hubad.db.parser.conditions.ConditionInfo;
import com.hubad.db.parser.conditions.ConditionItemInfo;
import com.hubad.db.parser.conditions.ConditionsCollection;
import com.hubad.db.parser.entity.DataInfo;
import com.hubad.db.parser.entity.DatabaseInfo;
import com.hubad.db.parser.entity.TableInfo;
import com.hubad.db.processor.ICommunicator;

import lombok.Data;

@Data
public class Translator {

    private static final int STARTING_STATE = -1;
    private static final int EXTRACT_ITEM_STATE = 0;
    private static final int EXTRACT_TABLE_STATE = 1;
    private static final int EXTRACT_JOIN_STATE = 2;
    private static final int EXTRACT_WHERE_STATE = 3;
    private static final int EXTRACT_ORDER_STATE = 4;

    private ICommunicator comm;
    private Properties properties;

    private int currentState;
    private DatabaseInfo dbInfo;
    private ConditionsCollection conditionsCollection;
    private Map<String, String> sqlMapperMap;
    private Map<String, String> stateMap;
    private Map<String, TableInfo> tableInfoMap;
    private Map<String, TableInfo> tableAliasMap;
    private Map<String, String> tableAliasReferenceMap;

    private List<String> nonExistingTables;
    private List<String> nonExistingFields;
    private List<String> excludedWords;
    private List<String> noIndentionWords;

    public Translator(DatabaseInfo dbInfo, ConditionsCollection conditionsCollection, Properties properties) {
        this(dbInfo, properties);
        this.conditionsCollection = conditionsCollection;
    }

    public Translator(DatabaseInfo dbInfo, Properties properties) {
        this.dbInfo = dbInfo;
        this.properties = properties;
    }

    public void translate(List<String> rawInput) {
        this.doMapping();

        List<String> processedLines = process(rawInput);
        this.sendProcessedSql(processedLines);

        // If tableInfoMap is empty then it means no state machine keywords are
        // used.
        String sqlTranslation = null;

        if (tableInfoMap.isEmpty()) {
            sqlTranslation = getTranslationWithoutTableInfo(processedLines);
        } else {
            sqlTranslation = getTranslationWithTableInfo(processedLines);
        }

        String translatedSql = sqlTranslation.replaceAll(CommonEnum.OPEN_PARENTHESIS_FUNC.getValue(),
            CommonEnum.OPEN_PARENTHESIS_NO_SPACE_FUNC.getValue());

        this.sendTranslatedSql(translatedSql);
    }

    private String getTranslationWithoutTableInfo(List<String> processedLines) {
        StringBuffer sb = new StringBuffer();
        List<String> lines = new ArrayList<String>();

        init();

        // Identify the tables first.
        for (String str : processedLines) {
            String line = str.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.contains(SqlMapperEnum.JOIN_SIGNAL.getValue())) {
                int index = line.indexOf(SqlMapperEnum.JOIN_SIGNAL.getValue()) + SqlMapperEnum.JOIN_SIGNAL.getValue().length();
                String str1 = line.substring(index).replaceAll(SqlMapperEnum.ON_SQL.getValue(), CommonEnum.EMPTY.getValue()).trim();
                if (!str1.isEmpty()) {
                    getProcessedTableInfo(str1);
                }
                lines.add(line);
            } else if (!(line.contains(CommonEnum.DOT.getValue()) || sqlMapperMap.containsKey(line))) {
                lines.add(getProcessedTableInfo(line));
            } else {
                lines.add(line);
            }
        }

        // Process line per line
        for (String line : lines) {
            boolean haveToIndent = true;
            String extractedLine = transformExtractionItemLine(line);

            for (String noIndentionWord : noIndentionWords) {
                if (extractedLine.contains(noIndentionWord)) {
                    haveToIndent = false;
                    break;
                }
            }

            if (haveToIndent) {
                sb.append(CommonEnum.TAB.getValue());
            }

            sb.append(extractedLine);
            sb.append(CommonEnum.NEW_LINE.getValue());
        }

        return sb.toString();
    }

    private String getTranslationWithTableInfo(List<String> processedLines) {
        StringBuffer sb = new StringBuffer();
        currentState = STARTING_STATE;

        // Time to translate the raw SQL mapping to useful one.
        for (String processedLine : processedLines) {
            String line = processedLine.trim();
            log("processedLine: " + line);

            if (stateMap.containsKey(line)) {
                currentState = Integer.parseInt(stateMap.get(line));
                sb.append(sqlMapperMap.get(line));
                sb.append(CommonEnum.NEW_LINE.getValue());
                continue;
            }

            sb.append(CommonEnum.TAB.getValue());

            switch (currentState) {
                case EXTRACT_ITEM_STATE:
                case EXTRACT_JOIN_STATE:
                case EXTRACT_WHERE_STATE:
                case EXTRACT_ORDER_STATE:

                    if (line.contains(CommonEnum.DOT.getValue())) {
                        // Segregate table and its data member.
                        sb.append(transformExtractionItemLine(line));
                    } else {
                        // TO DO: Future expansion. Like for aliasing...
                        sb.append(line);
                    }

                    break;
                case EXTRACT_TABLE_STATE:
                    String[] alias = line.replaceAll(CommonEnum.AS_SQL_SEPARATOR.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                        .split(CommonEnum.SPACE_SEPARATOR.getValue());
                    String tableName = alias[0];

                    TableInfo tableInfo = this.dbInfo.getTableLogicalNameMap().get(tableName);
                    if (tableInfo == null) {
                        sb.append(line);
                    } else {
                        sb.append(line.replaceAll(tableName, tableInfo.getPhysicalName()));
                    }
                    break;
                default:
                    sb.append(processedLine);
                    break;
            }

            sb.append(CommonEnum.NEW_LINE.getValue());
        }

        return sb.toString();
    }

    public List<String> process(List<String> rawInput) {
        List<String> processedData = new ArrayList<String>();

        init();

        currentState = STARTING_STATE;

        for (String str : rawInput) {
            StringBuffer sb = new StringBuffer();

            String input = str.trim().replaceAll(CommonEnum.EQUAL_REGEX.getValue(), CommonEnum.EQUAL_SQL_SEPARATOR.getValue())
                .replaceAll(CommonEnum.DOT_SQL_SEPARATOR.getValue(), CommonEnum.DOT.getValue())
                .replaceAll(CommonEnum.SPACE_SQL_REGEX.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                .replaceAll(CommonEnum.OPEN_PARENTHESIS_REGEX.getValue(), CommonEnum.OPEN_PARENTHESIS_SEPARATOR.getValue())
                .replaceAll(CommonEnum.LESS_THAN_EQUAL_REGEX.getValue(), CommonEnum.LESS_THAN_EQUAL_SEPARATOR.getValue())
                .replaceAll(CommonEnum.GREATER_THAN_EQUAL_REGEX.getValue(), CommonEnum.GREATER_THAN_EQUAL_SEPARATOR.getValue())
                .replaceAll(CommonEnum.NOT_EQUAL_REGEX.getValue(), CommonEnum.NOT_EQUAL_SEPARATOR.getValue())
                .replaceAll(CommonEnum.FULLWIDTH_LESS_THAN_REGEX.getValue(), CommonEnum.FULLWIDTH_LESS_THAN_SEPARATOR.getValue())
                .replaceAll(CommonEnum.FULLWIDTH_GREATER_THAN_REGEX.getValue(), CommonEnum.FULLWIDTH_GREATER_THAN_SEPARATOR.getValue())
                .replaceAll(CommonEnum.LESS_THAN_OR_EQUAL_TO_REGEX.getValue(), CommonEnum.LESS_THAN_OR_EQUAL_TO_SEPARATOR.getValue())
                .replaceAll(CommonEnum.GREATER_THAN_OR_EQUAL_TO_REGEX.getValue(), CommonEnum.GREATER_THAN_OR_EQUAL_TO_SEPARATOR.getValue())
                .replaceAll(CommonEnum.LESS_THAN_BUT_NOT_EQUAL_TO_REGEX.getValue(),
                    CommonEnum.LESS_THAN_BUT_NOT_EQUAL_TO_SEPARATOR.getValue())
                .replaceAll(CommonEnum.GREATER_THAN_BUT_NOT_EQUAL_TO_REGEX.getValue(),
                    CommonEnum.GREATER_THAN_BUT_NOT_EQUAL_TO_SEPARATOR.getValue())
                .replaceAll(CommonEnum.CLOSE_PARENTHESIS_REGEX.getValue(), CommonEnum.CLOSE_PARENTHESIS_SEPARATOR.getValue())
                .replaceAll(CommonEnum.COMMA_SEPARATOR.getValue(), CommonEnum.COMMA_SQL_SEPARATOR.getValue())
                .replaceAll(CommonEnum.ID_REGEX.getValue(), CommonEnum.ID_PARAM.getValue())
                .replaceAll(CommonEnum.SPACE_REGEX.getValue(), CommonEnum.SPACE_SEPARATOR.getValue());

            // List all the tables used.
            // Look up the DatabaseInfo for the tableInfo and add it to the
            // tableInfoList.
            if (currentState == EXTRACT_TABLE_STATE && !stateMap.containsKey(input)) {
                sb.append(getProcessedTableInfo(input));
            } else {
                sb.append(input);
            }

            // sb.append(CommonEnum.NEW_LINE.getValue());
            processedData.add(sb.toString().trim());

            if (input.isEmpty()) {
                continue;
            }

            if (stateMap.containsKey(input)) {
                currentState = Integer.parseInt(stateMap.get(input));
                continue;
            }
        }

        return processedData;
    }

    private void init() {
        tableInfoMap = new HashMap<String, TableInfo>();
        tableAliasMap = new HashMap<String, TableInfo>();
        tableAliasReferenceMap = new HashMap<String, String>();

        // console = new ArrayList<String>();
        nonExistingTables = new ArrayList<String>();
        nonExistingFields = new ArrayList<String>();
        excludedWords = getExcludedWords();
        noIndentionWords = getNoIndentionWords();
    }

    private String getProcessedTableInfo(String input) {
        StringBuffer sb = new StringBuffer();

        // Check if aliasing is used.
        String[] trimmed = input.trim().replaceAll(CommonEnum.AS_SQL_SEPARATOR.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
            .split(CommonEnum.SPACE_SEPARATOR.getValue());
        String name1 = trimmed[0].trim();
        String name2 = (trimmed.length == 2) ? trimmed[1].trim() : null;

        // Assuming table name is declared first in the line.
        if (dbInfo.getTableLogicalNameMap().containsKey(name1)) {
            TableInfo tableInfo = this.dbInfo.getTableLogicalNameMap().get(name1);
            tableInfoMap.put(name1, tableInfo);
            if (name2 != null) {
                tableAliasMap.put(name2, tableInfo);
                tableAliasReferenceMap.put(name1, name2);
            }
            sb.append(input);
        } else if (dbInfo.getTableLogicalNameMap().containsKey(name2)) {
            // For cases in which table alias is first declared in the line.
            TableInfo tableInfo = this.dbInfo.getTableLogicalNameMap().get(name2);
            tableInfoMap.put(name2, tableInfo);
            tableAliasMap.put(name1, tableInfo);
            tableAliasReferenceMap.put(name2, name1);
            sb.append(name2);
            sb.append(CommonEnum.SPACE_SEPARATOR.getValue());
            sb.append(name1);
        } else if (!excludedWords.contains(name1) && !nonExistingTables.contains(name1)
            && !name1.startsWith(SqlMapperEnum.COMMENT.getValue())) {
            nonExistingTables.add(name1);
            sendFeedback("Non-existing table: " + name1);
            sb.append(input);
        } else {
            sb.append(input);
        }

        return sb.toString();
    }

    private String transformExtractionItemLine(String line) {
        StringBuffer sb = new StringBuffer();
        // String[] lines =
        // line.replaceAll(CommonEnum.DOT_SQL_SEPARATOR_SPACE.getValue(),
        // CommonEnum.DOT.getValue()).trim()
        // .split(CommonEnum.SPACE_SEPARATOR.getValue());

        String[] lines = line.trim().split(CommonEnum.SPACE_SEPARATOR.getValue());

        log("line: " + line);

        for (String str : lines) {
            log("  splitted: " + str);

            if (str.contains(CommonEnum.SQL_PARAM_1.getValue()) || str.contains(CommonEnum.SQL_PARAM_2.getValue())
                || str.contains(CommonEnum.SQL_PARAM_3.getValue()) || str.contains(CommonEnum.SQL_PARAM_4.getValue())
                || str.contains(CommonEnum.SQL_PARAM_5.getValue())) {

                if (this.conditionsCollection != null) {
                    String[] result = str.substring(2).split(CommonEnum.PARAM_DOT_REGEX.getValue());
                    String conditionName = result[0];

                    if (conditionsCollection.getTableLogicalNameMap().containsKey(conditionName)) {
                        ConditionInfo conditionInfo = conditionsCollection.getTableLogicalNameMap().get(conditionName);
                        String conditionLabel = result[1];
                        sb.append(CommonEnum.SQL_PARAM_1.getValue());
                        sb.append(conditionInfo.getPhysicalName());
                        sb.append(CommonEnum.DOT.getValue());

                        if (conditionInfo.getConditionLogicalNameMap().containsKey(conditionLabel)) {
                            ConditionItemInfo item = conditionInfo.getConditionLogicalNameMap().get(conditionLabel);
                            sb.append(item.getName());
                        } else {
                            sb.append(conditionLabel);
                        }
                    } else {
                        sb.append(str);
                    }
                } else {
                    sb.append(str);
                }
            } else if (str.contains(CommonEnum.DOT.getValue())) {
                String[] item = str.split(CommonEnum.DOT_SEPARATOR.getValue());
                String tableName = item[0];
                String fieldName = item[1];

                log("    tableName: " + tableName + " ; fieldName: " + fieldName);

                // Assume that tableName is using an alias.
                TableInfo tableInfo = null;

                if (tableAliasReferenceMap.containsKey(tableName)) {
                    tableName = tableAliasReferenceMap.get(tableName);
                    tableInfo = tableAliasMap.get(tableName);
                } else if (tableAliasMap.containsKey(tableName)) {
                    tableInfo = tableAliasMap.get(tableName);
                } else if (tableInfoMap.containsKey(tableName)) {
                    tableInfo = tableInfoMap.get(tableName);
                    tableName = tableInfo.getPhysicalName();
                }

                if (tableInfo == null) {
                    if (!this.nonExistingTables.contains(tableName)) {
                        this.nonExistingTables.add(tableName);
                    } else if (!str.startsWith(SqlMapperEnum.COMMENT.getValue())) {
                        sendFeedback("Non-existing table and data field: " + str);
                    }
                    // Just append the raw line.
                    sb.append(str);
                } else {
                    // Time to process field item.
                    sb.append(tableName);
                    sb.append(CommonEnum.DOT.getValue());

                    DataInfo dataInfo = tableInfo.getColumnLogicalNameMap().get(fieldName);

                    if (dataInfo == null) {
                        nonExistingFields.add(str);
                        sendFeedback("Non-existing data field: " + str);
                        sb.append(fieldName);
                    } else {
                        sb.append(dataInfo.getPhysicalName());
                    }
                }
            } else {
                if (tableInfoMap.containsKey(str)) {
                    sb.append(tableInfoMap.get(str).getPhysicalName());
                } else {
                    sb.append(str);
                }
            }

            // Bring back the space separator.
            if (!str.equals(CommonEnum.EMPTY.getValue())) {
                sb.append(CommonEnum.SPACE_SEPARATOR.getValue());
            }
        }

        return sb.toString();
    }

    private void doMapping() {
        sqlMapperMap = new HashMap<String, String>();
        sqlMapperMap.put(SqlMapperEnum.EXTRACTION_ITEMS.getValue(), properties.getProperty(CommonEnum.EXTRACTION_ITEMS.getValue()).trim());
        sqlMapperMap.put(SqlMapperEnum.EXTRACTION_TABLE.getValue(), properties.getProperty(CommonEnum.EXTRACTION_TABLE.getValue()).trim());
        sqlMapperMap.put(SqlMapperEnum.COMBINATION_CONDITION.getValue(),
            properties.getProperty(CommonEnum.COMBINATION_CONDITION.getValue()).trim());
        sqlMapperMap.put(SqlMapperEnum.EXTRACTION_CONDITION.getValue(),
            properties.getProperty(CommonEnum.EXTRACTION_CONDITION.getValue()).trim());
        sqlMapperMap.put(SqlMapperEnum.SORT_CONDITIONS.getValue(), properties.getProperty(CommonEnum.SORT_CONDITIONS.getValue()).trim());

        stateMap = new HashMap<String, String>();
        stateMap.put(SqlMapperEnum.EXTRACTION_ITEMS.getValue(), String.valueOf(EXTRACT_ITEM_STATE));
        stateMap.put(SqlMapperEnum.EXTRACTION_TABLE.getValue(), String.valueOf(EXTRACT_TABLE_STATE));
        stateMap.put(SqlMapperEnum.COMBINATION_CONDITION.getValue(), String.valueOf(EXTRACT_JOIN_STATE));
        stateMap.put(SqlMapperEnum.EXTRACTION_CONDITION.getValue(), String.valueOf(EXTRACT_WHERE_STATE));
        stateMap.put(SqlMapperEnum.SORT_CONDITIONS.getValue(), String.valueOf(EXTRACT_ORDER_STATE));
    }

    private void log(String message) {
        comm.consoleLog(message);
    }

    private void sendFeedback(String message) {
        comm.commFeedback(message);
    }

    private void sendTranslatedSql(String translatedSql) {
        comm.updateTranslatedSql(translatedSql);
    }

    private void sendProcessedSql(List<String> processedLines) {
        comm.updateSourceSql(processedLines);
    }

    public List<String> getExcludedWords() {
        String propExcludedWords = properties.getProperty(SqlMapperEnum.EXCLUDED_WORDS.getValue());
        String excludedWords = ((propExcludedWords == null) ? SqlMapperEnum.EXCLUDED_WORDS_LIST.getValue() : propExcludedWords);
        return Arrays.asList(excludedWords.trim().split(CommonEnum.COMMA.getValue()));
    }

    public List<String> getNoIndentionWords() {
        String propNoIndentionWords = properties.getProperty(SqlMapperEnum.NO_INDENTION_WORDS.getValue());
        String noIndentionWords
                        = ((propNoIndentionWords == null) ? SqlMapperEnum.NO_INDENTION_WORDS_LIST.getValue() : propNoIndentionWords);
        return Arrays.asList(noIndentionWords.trim().split(CommonEnum.COMMA.getValue()));
    }
}
