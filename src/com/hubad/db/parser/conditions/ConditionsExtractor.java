package com.hubad.db.parser.conditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.PathEnum;
import com.hubad.db.processor.ICommunicator;

import lombok.Data;

@Data
public class ConditionsExtractor {

    private ICommunicator comm;

    public ConditionsCollection getConditionsCollections(List<File> files) throws FileNotFoundException, IOException {
        return getConditionsCollections(files, PathEnum.JAVA_FILE.getValue());
    }

    public ConditionsCollection getConditionsCollections(List<File> files, String pattern) throws FileNotFoundException, IOException {
        ConditionsCollection conditionsCollection = new ConditionsCollection();

        for (File file : files) {
            if (file.getName().indexOf(pattern) > 0) {
                ConditionInfo conditionInfo = null;
                boolean signalFound = false;

                List<String> lineItem = Files.readAllLines(file.toPath());
                for (String line : lineItem) {
                    
                    if (line.contains(CommonEnum.CONDITION_SIGNAL.getValue())) {
                        if (signalFound) {
                            break;
                        }

                        // Get the logical name
                        int start = line.indexOf(CommonEnum.CONDITION_START.getValue());
                        int end = line.indexOf(CommonEnum.CONDITION_END.getValue());
                        String logicalName = line.substring(start + 1, end);

                        conditionInfo = new ConditionInfo();
                        conditionInfo.setLogicalName(logicalName);

                        signalFound = true;
                    } else if (line.contains(CommonEnum.CONDITION_CLASS_END.getValue())) {
                        // Get the physical name
                        int end = line.indexOf(CommonEnum.CONDITION_CLASS_END.getValue());

                        String physicalName = line.substring(CommonEnum.CONDITION_CLASS_START.getValue().length() + 1, end).trim();
                        conditionInfo.setPhysicalName(physicalName);

                        conditionsCollection.getTableLogicalNameMap().put(conditionInfo.getLogicalName(), conditionInfo);
                        conditionsCollection.getTablePhysicalNameMap().put(conditionInfo.getPhysicalName(), conditionInfo);
                        
                        log(conditionInfo.getPhysicalName() + CommonEnum.ARROW.getValue() + conditionInfo.getLogicalName());
                    } else if (line.contains(CommonEnum.CONDITION_DATA_START.getValue())
                        && line.contains(CommonEnum.CONDITION_DATA_END.getValue())
                        && line.contains(CommonEnum.COMMA_SEPARATOR.getValue())) {
                        // Get the values
                        String[] data = line.trim().replaceAll(CommonEnum.CONDITION_DATA_SIGNAL.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                            .replaceAll(CommonEnum.DOUBLE_QUOTE.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                            .replaceAll(CommonEnum.SEMI_COLON.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                            .replaceAll(CommonEnum.SPACE_REGEX.getValue(), CommonEnum.SPACE_SEPARATOR.getValue())
                            .split(CommonEnum.SPACE_SEPARATOR.getValue());
                        if (data.length == 3) {
                            String name = data[0];
                            String value = data[1];
                            String label = data[2];

                            ConditionItemInfo conditionItemInfo = new ConditionItemInfo();
                            conditionItemInfo.setName(name);
                            conditionItemInfo.setValue(value);
                            conditionItemInfo.setLabel(label);

                            conditionInfo.getConditionPhysicalNameMap().put(name, conditionItemInfo);
                            conditionInfo.getConditionLogicalNameMap().put(label, conditionItemInfo);

                            log(CommonEnum.TAB.getValue() + conditionItemInfo.getName() + CommonEnum.ARROW.getValue()
                                + conditionItemInfo.getValue() + CommonEnum.DOUBLE_COLON.getValue() + conditionItemInfo.getLabel());
                        }
                    }
                }
                // Count the number of columns.
                if (conditionInfo != null) {
                    log(CommonEnum.TAB.getValue() + "No. of condition data found: " + conditionInfo.getConditionPhysicalNameMap().size());
                }
            }
        }

        return conditionsCollection;
    }

    private void log(String message) {
        comm.consoleLog(message);
    }
}
