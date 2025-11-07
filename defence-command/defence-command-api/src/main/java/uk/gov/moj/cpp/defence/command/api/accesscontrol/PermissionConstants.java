package uk.gov.moj.cpp.defence.command.api.accesscontrol;

import static javax.json.Json.createObjectBuilder;

public class PermissionConstants {

    private static final String PLEA_ACCESS = "plea-access";
    private static final String PLEA_ALLOCATION = "PLEA_ALLOCATION";
    private static final String OBJECT = "object";
    private static final String ACTION = "action";

    private PermissionConstants() {
    }

    public static String[] eligibleOnlinePleaPermissions() {
        return new String[] {
                createObjectBuilder().add(OBJECT, PLEA_ALLOCATION).add(ACTION, PLEA_ACCESS).build().toString()
        };
    }

}