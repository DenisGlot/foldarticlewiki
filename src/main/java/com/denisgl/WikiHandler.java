package com.denisgl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiHandler extends RecursiveAction {

    private static final Properties SETTINGS = Startup.getSettings();
    private static final int MAX_PAGES = 400;

    private static AtomicInteger categoryLevel = new AtomicInteger(-1);

    private final CategoryMember cm;

    private AtomicBoolean goToSubLevel = new AtomicBoolean(true);

    public WikiHandler(CategoryMember cm) {
        this.cm = cm;
        if (cm.getCountPages().get() > MAX_PAGES && categoryLevel.get() != cm.getLevel()) {
            goToSubLevel.set(false);
        }
    }

    @Override
    protected void compute() {
        List<WikiHandler> subTasks = new ArrayList<>();

        for (CategoryMember categoryMember : cm.getChildren()) {
            WikiReader.fillPageText(categoryMember);
            WikiWriter.writeInCVS(categoryMember);
            WikiWriter.createFolderOrFile(categoryMember);
            if (goToSubLevel.get()) {
                WikiReader.fillCmWithChildren(categoryMember);
                WikiHandler handler = new WikiHandler(categoryMember);
                handler.fork();
                subTasks.add(handler);
            }
        }

        for (WikiHandler subTask : subTasks) {
            subTask.join();

            String delay = SETTINGS.getProperty("delay.requests.seconds");
            int delaySeconds = Integer.parseInt(delay) * 1000;
            try {
                Thread.sleep(delaySeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



}
