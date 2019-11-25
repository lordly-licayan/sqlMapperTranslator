package com.hubad.db.processor;

import java.util.List;

public interface ICommunicator {
    void commFeedback(String message);
    void consoleLog(String message);
    void updateSourceSql(List<String> processedSqlStatements);
    void updateTranslatedSql(String translatedSqlStatements);
}
