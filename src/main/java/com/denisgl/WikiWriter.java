package com.denisgl;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Properties;

public class WikiWriter {

    private static final String WIKI_PAGE = "https://ru.wikipedia.org/wiki/";
    private static final Properties SETTINGS = Startup.getSettings();

    private static CSVWriter csvWriter;

    static {
        try {
            String csv = SETTINGS.getProperty("path") + SETTINGS.getProperty("cvs.name") + ".cvs";
            csvWriter = new CSVWriter(new FileWriter(csv));
            csvWriter.writeNext(new String[]{"File id", "Название\nстатьи", "URL", "Категория", "Уровень", "Размер\nстатьи\n(в символах!)"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeInCVS(CategoryMember cm) {
        if (cm.getType() == Startup.CmType.page) {
            String[] row = new String[] {
                    cm.getFileId("_"),
                    cm.getTitle(), WIKI_PAGE + cm.getTitle(),
                    cm.getRoot().getNumber(),
                    String.valueOf(cm.getLevel()),
                    String.valueOf(cm.getText().length())
            };
            csvWriter.writeNext(row);
        }
    }

    public static void closeCvsWriter() throws IOException {
        csvWriter.close();
    }

    public static void createFolderOrFile(CategoryMember cm) {
        try {
            String pathRoot = SETTINGS.getProperty("path");
            if (cm.getType() == Startup.CmType.page) {
                Path path = Paths.get(pathRoot + "/" + cm.getFileId("_") + ".txt");
                Files.write(path, Collections.singletonList(cm.getText()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Path path = Paths.get(pathRoot + "/" + cm.getFileId("/"));
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
