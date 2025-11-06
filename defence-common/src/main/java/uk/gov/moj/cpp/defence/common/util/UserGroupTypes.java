package uk.gov.moj.cpp.defence.common.util;

public enum UserGroupTypes {
    CHAMBERS_ADMIN("Chambers Admin"),
    CHAMBERS_CLERK("Chambers Clerk"),
    ADVOCATES("Advocates"),
    DEFENCE_LAWYERS("Defence Lawyers");

    private final String roleName;

    UserGroupTypes(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}

