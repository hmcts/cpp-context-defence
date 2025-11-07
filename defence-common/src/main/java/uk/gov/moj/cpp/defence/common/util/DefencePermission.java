package uk.gov.moj.cpp.defence.common.util;

import static uk.gov.moj.cpp.defence.common.util.ActionTypes.UPLOAD;
import static uk.gov.moj.cpp.defence.common.util.ActionTypes.VIEW;
import static uk.gov.moj.cpp.defence.common.util.ObjectTypes.DEFENCE_CLIENT;
import static uk.gov.moj.cpp.defence.common.util.ObjectTypes.DOCUMENT;

public enum DefencePermission {

    VIEW_DEFENDANT_PERMISSION(DEFENCE_CLIENT.getObjectName(), VIEW.getActionName()),
    VIEW_DOCUMENT_PERMISSION(DOCUMENT.getObjectName(), VIEW.getActionName()),
    UPLOAD_DOCUMENT_PERMISSION(DOCUMENT.getObjectName(), UPLOAD.getActionName());

    private String objectType;
    private String actionType;

    DefencePermission(String objectType, final String actionType) {
        this.objectType = objectType;
        this.actionType = actionType;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getActionType() {
        return actionType;
    }

}
