package com.hubad.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.PathEnum;
import com.hubad.db.processor.Processor;

import lombok.Data;

@Data
public class ProcessHandler extends Processor {

    private String inputFile;
    private String outputFile = PathEnum.OUTPUT_FILE.getValue();
    private List<String> processedSqlStatements;
    private StringBuffer consoleLog = new StringBuffer();
    private StringBuffer feedback = new StringBuffer();

    @Override
    public void commFeedback(String message) {
        feedback.append(message);
        feedback.append(CommonEnum.NEW_LINE.getValue());
    }

    @Override
    public void updateSourceSql(List<String> processedSqlStatements) {
        this.processedSqlStatements = processedSqlStatements;
    }

    @Override
    public void updateTranslatedSql(String translatedSqlStatements) {
        try {
            if (outputFile == null || outputFile.equals(PathEnum.OUTPUT_FILE.getValue())) {
                Files.write(Paths.get(PathEnum.OUTPUT_FILE.getValue()), translatedSqlStatements.getBytes());
                Files.write(Paths.get(PathEnum.PROCESSED_SQL_FILE.getValue()), processedSqlStatements, StandardCharsets.UTF_8);
                Files.write(Paths.get(PathEnum.LOG_FILE.getValue()), consoleLog.toString().getBytes());
                Files.write(Paths.get(PathEnum.FEEDBACK_FILE.getValue()), feedback.toString().getBytes());
            } else {
                Path pathToFile = Paths.get(outputFile);
                String path = "";

                if (pathToFile.getParent() != null) {
                    path = pathToFile.getParent() + CommonEnum.BACK_SLASH.getValue();
                    Files.createDirectories(pathToFile.getParent());
                }

                Files.write(Paths.get(outputFile), Arrays.asList(translatedSqlStatements.split(CommonEnum.NEW_LINE.getValue())),
                    StandardCharsets.UTF_8);
                Files.write(Paths.get(path + PathEnum.PROCESSED_SQL_FILE.getValue()), processedSqlStatements, StandardCharsets.UTF_8);
                Files.write(Paths.get(path + PathEnum.LOG_FILE.getValue()),
                    Arrays.asList(consoleLog.toString().split(CommonEnum.NEW_LINE.getValue())), StandardCharsets.UTF_8);
                Files.write(Paths.get(path + PathEnum.FEEDBACK_FILE.getValue()),
                    Arrays.asList(feedback.toString().split(CommonEnum.NEW_LINE.getValue())), StandardCharsets.UTF_8);

                System.out.println("Done writing output files.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consoleLog(String message) {
        consoleLog.append(message);
        consoleLog.append(CommonEnum.NEW_LINE.getValue());
    }

    public void translate() throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "Shift_JIS"));
            String line;
            List<String> str = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                str.add(new String(line.getBytes(), Charset.forName("UTF-8")));
            }
            this.translate(str);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
