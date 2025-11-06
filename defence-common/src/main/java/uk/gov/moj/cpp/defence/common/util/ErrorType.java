package uk.gov.moj.cpp.defence.common.util;

public enum ErrorType {
    UNAUTHORIZED_GRANTING("001", "Granter has no permission to grant access someone!"),
    UNAUTHORIZED_REMOVE_GRANTING("002", "User has no permission to remove this granting!"),
    PERMISSION_NOT_FOUND("003", "Permission not found!"),
    USER_NOT_FOUND("004", "User not found for the given e-mail."),
    USER_NOT_IN_ALLOWED_GROUPS("005", "Grantee is not in one of the allowed groups."),
    ALREADY_GRANTED("006", "User already granted on the specified attribute for the given action."),
    ASSIGNEE_NOT_IN_ALLOWED_GROUPS("007", "Assignee is not in one of the allowed groups."),
    ALREADY_ASSIGNED("008", "User already assigned to the case"),
    ASSIGNEE_DEFENDING_CASE("009", "Assignee is defending this case"),
    USER_NOT_ASSIGNED("010", "Failed to remove case assignment. User not assigned to the case"),
    ORGANISATION_HAS_ADVOCATES_ASSIGNED("011", "Failed to remove organisation case assignment. Organisation has other advocate assignments to the case"),
    CASE_NOT_FOUND("012", "Case not found"),
    ORGANISATION_NOT_PROSECUTING_AUTHORITY("013", "Organisation not Prosecuting Authority");

    private final String code;
    private final String message;

    private ErrorType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}





