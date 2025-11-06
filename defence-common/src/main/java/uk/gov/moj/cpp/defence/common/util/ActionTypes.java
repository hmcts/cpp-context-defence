package uk.gov.moj.cpp.defence.common.util;

public enum ActionTypes {
    VIEW("View"),
    UPLOAD("Upload");

    private final String actionName;

    ActionTypes(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return actionName;
    }

    public String getActionName() {
        return actionName;
    }
}

