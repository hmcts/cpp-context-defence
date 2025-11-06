package uk.gov.moj.cpp.defence.common.util;

public enum ObjectTypes {
    DOCUMENT("DefendantDocuments"),
    DEFENCE_CLIENT("DefenceClient");

    private final String objectName;

    ObjectTypes(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return objectName;
    }

    public String getObjectName() {
        return objectName;
    }
}

