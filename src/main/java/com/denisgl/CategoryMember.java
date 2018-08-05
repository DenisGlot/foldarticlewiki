package com.denisgl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * pageId equals 0 if type equals CmType.cat
 * text doesn't equals null if type equals CmType.page
 */
public class CategoryMember {

    private int pageId;// 0 if type equals CmType.cat
    private String number;
    private String title;
    private Startup.CmType type;
    private String text;// not null if type eqals CmType.page

    private CategoryMember parent;
    private List<CategoryMember> children;

    private boolean isFilled;

    private AtomicInteger countPages;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public Startup.CmType getType() {
        return type;
    }

    public void setType(Startup.CmType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        isFilled = true;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public CategoryMember getParent() {
        return parent;
    }

    public void setParent(CategoryMember parent) {
        this.parent = parent;
    }

    public List<CategoryMember> getChildren() {
        if (getType() == Startup.CmType.page) {
            return Collections.emptyList();
        }
        return children;
    }

    public void setChildren(List<CategoryMember> children) {
        this.children = children;

        long childrenPages = children.stream()
                .filter(cmChild -> cmChild.getType() == Startup.CmType.page)
                .count();

        getCountPages().addAndGet(Math.toIntExact(childrenPages));
        isFilled = true;
    }

    public AtomicInteger getCountPages() {
        if (parent == null) {
            return countPages;
        }
        return parent.getCountPages();
    }

    public void setCountPages(int countPages) {
        if (parent == null) {
            this.countPages = new AtomicInteger(countPages);
        } else {
            throw new IllegalArgumentException("Attempt to set count of pages in non root directory");
        }
    }

    public CategoryMember getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public String getFileId(String separator) {
        if (parent == null) {
            return getNumber();
        }

        return parent.getFileId(separator) + separator + getNumber();
    }

    public String getDestination() {
        if (parent == null) {
            return getNumber() + "_" + getTitle();
        }

        if (getType() != Startup.CmType.page) {
            return parent.getDestination() + "/" + getNumber() + "_" + getTitle();
        }
        return parent.getDestination() + "/" + getFileId("_") + ".txt" ;
    }

    public int getLevel() {
        if (parent == null) {
            return -1;
        }
        return 1 + parent.getLevel();
    }

    public CategoryMember getLastOnLevel() {
        CategoryMember last = getRoot();
        int i = getLevel();
        while (i >= 0) {
            List<CategoryMember> children = last.getChildren();

            if (children == null || children.isEmpty()) return null;

            last = children.get(children.size() - 1);
            i--;
        }

        return last;
    }

}