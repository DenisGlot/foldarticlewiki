package com.denisgl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiHandler extends RecursiveAction {

    private static final Properties SETTINGS = Startup.getSettings();
    private static final int MAX_PAGES = 400;


    private CategoryMember cm;
    private static List<CategoryMember> cmToProcess = new ArrayList<>();

    private static AtomicInteger allowedSubCatLevel = new AtomicInteger(0);

    public WikiHandler(CategoryMember cm) {
        this.cm = cm;
        if (cm.getParent() == null) {
            allowedSubCatLevel.set(0);
        }
    }

    @Override
    protected void compute() {
        for (CategoryMember categoryMember : cm.getChildren()) {
            WikiReader.fillPageText(categoryMember);
            WikiWriter.writeInCVS(categoryMember);
            WikiWriter.createFolderOrFile(categoryMember);
            if (categoryMember.getLevel() <= allowedSubCatLevel.get()) {
                process(categoryMember);
            } else {
                cmToProcess.add(categoryMember);
            }
        }

        CategoryMember lastOnLevel = cm.getLastOnLevel();
        boolean isFilled = lastOnLevel != null && lastOnLevel.isFilled();
        if (isFilled && cm.getCountPages().get() > MAX_PAGES) {
            cmToProcess.clear();
        } else if (isFilled) {
            allowedSubCatLevel.set(cm.getLevel() + 1);
            cmToProcess.removeIf(cm -> process(cm) != null);
            cmToProcess.clear();
        }


    }

    private WikiHandler process(CategoryMember categoryMember) {
        WikiReader.fillCmWithChildren(categoryMember);
        WikiHandler handler = new WikiHandler(categoryMember);
        handler.fork();
        handler.join();

        String delay = SETTINGS.getProperty("delay.requests.seconds");
        int delaySeconds = Integer.parseInt(delay) * 1000;
        try {
            Thread.sleep(delaySeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return handler;
    }


}
