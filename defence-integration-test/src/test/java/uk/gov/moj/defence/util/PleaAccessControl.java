package uk.gov.moj.defence.util;


import static uk.gov.moj.defence.util.UsersGroupStub.stubUserWithPermission;

public class PleaAccessControl {

    public static void mockDefenceUserPleaAccessControl(final String userId) {
        final String permission = TestUtils.getPayloadForCreatingRequest("stub-data/usersgroup-service/usersgroups.defence-user-permission-for-plea.json");
        stubUserWithPermission(userId, permission);
    }

}
