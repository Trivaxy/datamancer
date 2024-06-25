package xyz.trivaxy.datamancer.tracker;

import java.util.*;

public class TrackerList {

    private final List<String> templates;

    public TrackerList(List<String> templates) {
        this.templates = templates;
    }

    public TrackerList() {
        this.templates = new ArrayList<>();
    }

    public void addTemplate(String template) {
        templates.add(template);
    }

    public List<String> getTemplates() {
        return templates;
    }

    public int size() {
        return templates.size();
    }

    public void removeAt(int index) {
        if (index < 0 || index >= templates.size())
            return;

        templates.remove(index);
    }

    public void clear() {
        templates.clear();
    }
}
