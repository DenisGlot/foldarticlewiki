package com.denisgl;

import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiHandler extends RecursiveAction {

    private static final Properties SETTINGS = Startup.getSettings();
    private static final int MAX_PAGES = 400;
    private static final int MAX_LEVEL = 2;

    private static Queue<CategoryMember> cmToProcess = new ConcurrentLinkedQueue<>();
    private static AtomicInteger allowedSubCatLevel = new AtomicInteger(-1);

    private CategoryMember cm;

    public WikiHandler(CategoryMember cm) {
        this.cm = cm;
        if (cm.getParent() == null) {
            allowedSubCatLevel.set(-1);
        }
    }

    @Override
    protected void compute() {
        int cmChildLevel = cm.getLevel() + 1;
        for (CategoryMember categoryMember : cm.getChildren()) {
            WikiReader.fillPageText(categoryMember);
            WikiWriter.writeInCVS(categoryMember);
            WikiWriter.createFolderOrFile(categoryMember);
            if (categoryMember.getLevel() <= allowedSubCatLevel.get()) {
                process(categoryMember);
            } else {
                if (cmChildLevel < MAX_LEVEL) {
                    cmToProcess.add(categoryMember);
                }
            }
        }

        CategoryMember lastOnLevel = cm.getLastOnLevel();
        boolean isFilled = lastOnLevel != null && lastOnLevel.isFilled();
        if (isFilled && (cm.getCountPages().get() > MAX_PAGES || cmChildLevel >= MAX_LEVEL)) {
            cmToProcess.clear();
        } else if (isFilled) {
            allowedSubCatLevel.incrementAndGet();
            Iterator<CategoryMember> iterator = cmToProcess.iterator();
            while (iterator.hasNext()) {
                CategoryMember next = iterator.next();
                iterator.remove();
                process(next);
            }
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
