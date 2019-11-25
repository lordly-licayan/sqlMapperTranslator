package com.hubad.db.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.PathEnum;
import com.hubad.db.enums.SqlMapperEnum;
import com.hubad.db.parser.conditions.ConditionInfo;
import com.hubad.db.parser.conditions.ConditionsCollection;
import com.hubad.db.parser.conditions.ConditionsExtractor;
import com.hubad.db.parser.entity.DatabaseInfo;
import com.hubad.db.parser.entity.Extractor;
import com.hubad.db.parser.sql.Translator;
import com.hubad.db.utils.Util;

import lombok.Data;

@Data
public abstract class Processor implements ICommunicator {

    private DatabaseInfo dbInfo;
    private ConditionsCollection conditionsCollection;
    private Extractor extractor;
    private ConditionsExtractor conditionsExtractor;
    private Translator translator;
    private Properties properties;

    private String workspace;
    private String dataEntityPath;
    private String conditionsPath;
    private String reportPath;
    private String sourceCodePath;

    // 1.) Read settings file.
    // If settings file exists then get the workspace.
    // If workspace exists then check if path exist. Otherwise, throw error.
    // Else, throw error. User must input workspace.
    // Otherwise, throw an error.
    // 2.) Collect data entities information.
    //

    public void init() {
        try {
            workspace = this.getWorkspace();
            dataEntityPath = (dataEntityPath == null) ? workspace : dataEntityPath;
            conditionsPath = (conditionsPath == null) ? workspace : conditionsPath;

            this.commFeedback("Collecting info for data entities.");
            Path path = Util.getDataEntityPath(dataEntityPath);
            List<File> files = Util.getFiles(path);

            extractor = new Extractor();
            extractor.setComm(this);

            dbInfo = extractor.getDatabaseInfo(files);
            this.commFeedback("No. of tables counted: " + dbInfo.getTablePhysicalNameMap().size());
            System.out.println("No. of tables counted: " + dbInfo.getTablePhysicalNameMap().size());

            this.commFeedback("Collecting info for conditions.");
            path = Util.getConditionsPath(conditionsPath);
            List<File> conditionsFiles = Util.getFiles(path);

            conditionsExtractor = new ConditionsExtractor();
            conditionsExtractor.setComm(this);
            
            conditionsCollection= conditionsExtractor.getConditionsCollections(conditionsFiles);
            this.commFeedback("No. of conditions counted: " + conditionsCollection.getTablePhysicalNameMap().size());
            System.out.println("No. of conditions counted: " + conditionsCollection.getTablePhysicalNameMap().size());
            
            translator = new Translator(dbInfo, conditionsCollection, properties);
            translator.setComm(this);
        } catch (IOException e) {
            this.commFeedback(e.getMessage());
        }
    }

    public boolean isValidWorkspace(String path) {
        return Util.isValid(path, PathEnum.ENTITY);
    }

    public boolean isValidDataEntityPath(String path) {
        return Util.isValid(path, PathEnum.ENTITY);
    }

    public boolean isValidReportPath(String path) {
        return Util.isValid(path, PathEnum.FILE_WEB);
    }

    public boolean isValidConditionsPath(String path) {
        return Util.isValid(path, PathEnum.CONDITION);
    }

    public boolean isValidSourceCodePath(String path) {
        return Util.isValid(path, PathEnum.WEB);
    }

    public boolean isValidWorkspace() {
        return isValidWorkspace(getWorkspace());
    }

    public void translate(Path path) throws IOException {
        commFeedback("Translation started...");
        List<String> str = Files.readAllLines(path, StandardCharsets.UTF_8);
        translator.translate(str);
    }

    public void translate(List<String> str) throws IOException {
        commFeedback("Translation started...");
        translator.translate(str);
    }
    
    public void translate(String str) {
        commFeedback("Translation started...");
        List<String> strList = Arrays.asList(str.split(CommonEnum.NEW_LINE.getValue()));
        translator.translate(strList);
    }

    public String getWorkspace() {
        return this.properties.getProperty(PathEnum.WORKSPACE.getValue());
    }

    public void setWorkspace(String workspace) throws FileNotFoundException, IOException {
        this.properties.setProperty(PathEnum.WORKSPACE.getValue(), workspace);
        this.properties.store(new FileOutputStream(PathEnum.CONFIG_FILE.getValue()), null);
    }

    public void preInit() {
        properties = new Properties();

        if (Files.notExists(Paths.get(PathEnum.CONFIG_FILE.getValue()), LinkOption.NOFOLLOW_LINKS)) {
            try {
                System.out.println("Creating properties file.");
                properties.setProperty(CommonEnum.EXTRACTION_ITEMS.getValue(), SqlMapperEnum.EXTRACTION_ITEMS_EN.getValue());
                properties.setProperty(CommonEnum.EXTRACTION_TABLE.getValue(), SqlMapperEnum.EXTRACTION_TABLE_EN.getValue());
                properties.setProperty(CommonEnum.COMBINATION_CONDITION.getValue(), SqlMapperEnum.COMBINATION_CONDITION_EN.getValue());
                properties.setProperty(CommonEnum.EXTRACTION_CONDITION.getValue(), SqlMapperEnum.EXTRACTION_CONDITION_EN.getValue());
                properties.setProperty(CommonEnum.SORT_CONDITIONS.getValue(), SqlMapperEnum.SORT_CONDITIONS_EN.getValue());
                properties.setProperty(SqlMapperEnum.EXCLUDED_WORDS.getValue(), SqlMapperEnum.EXCLUDED_WORDS_LIST.getValue());
                properties.setProperty(SqlMapperEnum.NO_INDENTION_WORDS.getValue(), SqlMapperEnum.NO_INDENTION_WORDS_LIST.getValue());
                properties.store(new FileOutputStream(PathEnum.CONFIG_FILE.getValue()), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                InputStream input = new FileInputStream(PathEnum.CONFIG_FILE.getValue());
                properties.load(input);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
