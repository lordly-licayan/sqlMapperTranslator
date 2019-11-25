package com.hubad.db.parser.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.hubad.db.enums.AnnotationEnum;
import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.PathEnum;
import com.hubad.db.enums.TypeEnum;
import com.hubad.db.processor.ICommunicator;
import com.hubad.db.utils.Util;

import lombok.Data;

@Data
public class Extractor {

    private ICommunicator comm;

    public DatabaseInfo getDatabaseInfo(List<File> files) throws FileNotFoundException, IOException {
        return getDatabaseInfo(files, PathEnum.DB_TABLE.getValue());
    }

    public DatabaseInfo getDatabaseInfo(List<File> files, String pattern) throws FileNotFoundException, IOException {
        DatabaseInfo dbInfo = new DatabaseInfo();

        for (File file : files) {
            if (file.getName().indexOf(pattern) > 0) {
                TableInfo tblInfo = null;
                DataInfo dataInfo = null;
                boolean moveToNextLine = false;

                List<String> lineItem = Files.readAllLines(file.toPath());
                for (String line : lineItem) {
                    if (line.contains(AnnotationEnum.TABLE.getValue())) {
                        // Found an annotation for table.
                        // Instantiate TableInfo.
                        Map<String, String> m = Util.extractKeyValuePair(AnnotationEnum.TABLE, line);

                        tblInfo = new TableInfo();
                        tblInfo.setPhysicalName((String) m.get(TypeEnum.PHYSICAL_NAME.getValue()));
                        tblInfo.setLogicalName((String) m.get(TypeEnum.LOGICAL_NAME.getValue()));

                        dbInfo.getTablePhysicalNameMap().put(tblInfo.getPhysicalName(), tblInfo);
                        dbInfo.getTableLogicalNameMap().put(tblInfo.getLogicalName(), tblInfo);

                        log(tblInfo.getPhysicalName() + CommonEnum.ARROW.getValue() + tblInfo.getLogicalName());

                    } else if (tblInfo != null && line.contains(AnnotationEnum.COLUMN.getValue())) {
                        // Found column information. Extract it.
                        Map<String, String> m = Util.extractKeyValuePair(AnnotationEnum.COLUMN, line);

                        dataInfo = new DataInfo();
                        dataInfo.setPhysicalName((String) m.get(TypeEnum.PHYSICAL_NAME.getValue()));
                        dataInfo.setLogicalName((String) m.get(TypeEnum.LOGICAL_NAME.getValue()));

                        tblInfo.getColumnPhysicalNameMap().put(dataInfo.getPhysicalName(), dataInfo);
                        tblInfo.getColumnLogicalNameMap().put(dataInfo.getLogicalName(), dataInfo);
                        
                        log(CommonEnum.TAB.getValue() + dataInfo.getPhysicalName() + CommonEnum.ARROW.getValue()  + dataInfo.getLogicalName());

                        moveToNextLine = true;
                    } else if (moveToNextLine) {
                        // Get the detail of the data member.
                        Util.fillUpDataField(dataInfo, line);
                        moveToNextLine = false;
                    }
                }
                // Count the number of columns.
                if(tblInfo!=null) {
                    //Set the optimistic lock ID
                    dataInfo = new DataInfo();
                    dataInfo.setPhysicalName(CommonEnum.OPTIMISTIC_LOCK_ID.getValue());
                    dataInfo.setLogicalName(CommonEnum.OPTIMISTIC_LOCK.getValue());

                    tblInfo.getColumnPhysicalNameMap().put(dataInfo.getPhysicalName(), dataInfo);
                    tblInfo.getColumnLogicalNameMap().put(dataInfo.getLogicalName(), dataInfo);
                    
                    log(CommonEnum.TAB.getValue() + dataInfo.getPhysicalName() + CommonEnum.ARROW.getValue()  + dataInfo.getLogicalName());
                    log(CommonEnum.TAB.getValue() + "No. of data fields: " + tblInfo.getColumnLogicalNameMap().size());
                }
            }
        }

        return dbInfo;
    }

    private void log(String message) {
        comm.consoleLog(message);
    }
}
