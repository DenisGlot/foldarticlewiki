package com.denisgl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

public class Startup {

    private static final Properties SETTINGS = getSettings();

    enum CmAtrribute {
        pageid,
        title,
        type
    }

    enum CmType {
        page,
        cat,
        subcat;

        public static CmType getByName(String name) {
            for(CmType s : values()) {
                if(s.name().equalsIgnoreCase(name)) return s;
            }
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String categories = SETTINGS.getProperty("categories");
        String[] split = categories.split(",");
        List<WikiHandler> wikiHandlers = new ArrayList<>(split.length);
        int countNumber = 0;
        for (String category : split) {
            CategoryMember cm = new CategoryMember();
            cm.setTitle(category);
            cm.setType(CmType.cat);
            cm.setNumber(String.format("%02d", ++countNumber));
            cm.setCountPages(0);
            WikiReader.fillCmWithChildren(cm);

            WikiWriter.createFolderOrFile(cm);

            wikiHandlers.add(new WikiHandler(cm));
        }

        int threads = Integer.parseInt(SETTINGS.getProperty("threads"));
        ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
        wikiHandlers.forEach(forkJoinPool::invoke);

        forkJoinPool.shutdown();
        WikiWriter.closeCvsWriter();
    }

    public static Properties getSettings() {
        if (SETTINGS == null) {
            Properties prop = new Properties();
            try(InputStream input = Startup.class.getResourceAsStream("/settings.xml")) {
                prop.loadFromXML(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prop;
        }

        return SETTINGS;
    }

}
