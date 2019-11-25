package com.hubad.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.hubad.db.enums.PathEnum;

public class Main {

    public static void main(String[] args) {

        ProcessHandler processHandler= new ProcessHandler();
        processHandler.preInit();
        
        int index= 0;

        for(String arg: args) {
            switch(arg) {
                
                case "-w":
                    String workspace= args[index+1];
                    if(processHandler.isValidWorkspace(workspace)) {
                        try {
                            processHandler.setWorkspace(workspace);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Error: workspace not valid.");
                        return;
                    }
                   break;
                case "-i":
                    String inputFile= args[index+1];
                    if(Files.exists(Paths.get(inputFile), LinkOption.NOFOLLOW_LINKS)){
                        processHandler.setInputFile(inputFile);
                    }else {
                        System.out.println("Input file did not exist!");
                        return;
                    }
                    break;
                case "-o":
                    String customOutputFile= args[index+1];
                    processHandler.setOutputFile(customOutputFile);
                    break;
                case "-t":  //for data-entities
                    String dataEntityPath= args[index+1];
                    if(processHandler.isValidDataEntityPath(dataEntityPath)) {
                        processHandler.setDataEntityPath(dataEntityPath);
                        System.out.println("Data-entity path: " + Paths.get(dataEntityPath));
                    }else {
                        System.out.println("Data-entity path did not exist!");
                        return;
                    }
                    break;
                case "-r":  //for csv/pdf/reports
                    String reportPath= args[index+1];
                    if(processHandler.isValidReportPath(reportPath)) {
                        processHandler.setReportPath(reportPath);
                        System.out.println("CSV/PDF/Report path: " + Paths.get(reportPath));
                    }else {
                        System.out.println("CSV/PDF/Report path did not exist!");
                        return;
                    }
                    break;
                case "-c":  //for conditions
                    String conditionsPath= args[index+1];
                    if(processHandler.isValidConditionsPath(conditionsPath)) {
                        processHandler.setConditionsPath(conditionsPath);
                        System.out.println("Conditions path: " + Paths.get(conditionsPath));
                    }else {
                        System.out.println("Conditions path did not exist!");
                        return;
                    }
                    break;
                case "-s":  //for source codes
                    String sourceCodePath= args[index+1];
                    if(processHandler.isValidSourceCodePath(sourceCodePath)) {
                        processHandler.setSourceCodePath(sourceCodePath);
                        System.out.println("Source code path: " + Paths.get(sourceCodePath));
                    }else {
                        System.out.println("Source code path did not exist!");
                        return;
                    }
                    break;
            }
            index++;
        }
        
        if(index==0) {
            return;
        }
        
        System.out.println("Workspace: " + processHandler.getWorkspace());
        System.out.println("Input file: " + processHandler.getInputFile());
        System.out.println("Output file: " + processHandler.getOutputFile());
        
        try {
            processHandler.init();
            processHandler.translate();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
