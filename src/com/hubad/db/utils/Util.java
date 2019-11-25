package com.hubad.db.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hubad.db.enums.AnnotationEnum;
import com.hubad.db.enums.CommonEnum;
import com.hubad.db.enums.PathEnum;
import com.hubad.db.parser.entity.DataInfo;

import lombok.Data;

@Data
public class Util {

    public static Map<String, String> extractKeyValuePair(AnnotationEnum annotationEnum, String str) {
        Map<String, String> result = new HashMap<String, String>();
        String[] items = str.replaceAll(annotationEnum.getValue() + "|\\s|\"|[(]|[)]", CommonEnum.EMPTY.getValue())
            .split(CommonEnum.COMMA.getValue());

        for (String item : items) {
            if (item != null && !item.isEmpty() && item.contains(CommonEnum.EQUAL_SEPARATOR.getValue())) {
                String[] s = item.split(CommonEnum.EQUAL_SEPARATOR.getValue());
                result.put(s[0], s[1]);
            }
        }

        return result;
    }

    public static void fillUpDataField(DataInfo dataInfo, String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        String[] item = str.trim().replaceAll(CommonEnum.SEMI_COLON.getValue(), CommonEnum.EMPTY.getValue())
            .split(CommonEnum.SPACE_SEPARATOR.getValue());
        if (item.length == 3) {
            dataInfo.setModifier(item[0]);
            dataInfo.setDataType(item[1]);
            dataInfo.setName(item[2]);
        }
    }

    public static List<File> getFiles(String path) throws IOException {
        return getFiles(Paths.get(path));
    }

    public static List<File> getFiles(Path path) throws IOException {
        return Files.walk(path).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
    }

    public static List<File> getFolders(String path) throws IOException {
        return getFolders(path, 1);
    }

    public static List<File> getFolders(String path, int depth) throws IOException {
        return Files.walk(Paths.get(path), depth).filter(Files::isDirectory).map(Path::toFile).collect(Collectors.toList());
    }

    public static Path getDataEntityPath(String workspace) throws IOException {
        return getJavaSourcePath(workspace, PathEnum.ENTITY);
    }

    public static Path getConditionsPath(String path) throws IOException {
        return getJavaSourcePath(path, PathEnum.CONDITION);
    }
    
    public static Path getOutDtoPath(String workspace) throws IOException {
        return getJavaSourcePath(workspace, PathEnum.WEB);
    }

    public static Path getFileFormatterPath(String workspace) throws IOException {
        return getJavaSourcePath(workspace, PathEnum.FILE_WEB);
    }

    public static Path getJavaSourcePath(String path, PathEnum pathEnum) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(path);
        
        do {
            List<File> dirList = getFolders(sb.toString());
            for (File file : dirList) {
                if(file.getPath().contains(pathEnum.getValue())) {
                    Path p = Paths.get(file.getPath() + PathEnum.JAVA_SOURCE.getValue());
                    if (Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                        return p;
                    }  
                }
            }
            sb.append(CommonEnum.BACK_SLASH.getValue());
            sb.append(pathEnum.getValue());
        }while(Files.exists(Paths.get(sb.toString()), LinkOption.NOFOLLOW_LINKS));

        //throw new IOException("Can't retrieve source!");
        throw new IOException("Path did not exist!");
    }

    public static boolean isValid(String path, PathEnum pathEnum) {
        try {
            getJavaSourcePath(path, pathEnum);
            return true;
        } catch (IOException e) {
        }

        return false;
    }
}
