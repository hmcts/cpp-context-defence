package uk.gov.moj.cpp.defence.common.util;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static uk.gov.moj.cpp.defence.common.util.UserGroupTypes.ADVOCATES;
import static uk.gov.moj.cpp.defence.common.util.UserGroupTypes.CHAMBERS_ADMIN;
import static uk.gov.moj.cpp.defence.common.util.UserGroupTypes.CHAMBERS_CLERK;
import static uk.gov.moj.cpp.defence.common.util.UserGroupTypes.DEFENCE_LAWYERS;

import uk.gov.justice.cps.defence.Permission;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.events.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GrantAccessUtil {

    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String ACTION = "action";
    public static final String OBJECT = "object";

    protected static final List<String> allowedUserGroupsForGranteeForAllGrantAccess = Arrays.asList(ADVOCATES.getRoleName(), DEFENCE_LAWYERS.getRoleName());
    protected static final List<String> allowedUserGroupsForGranteeToGrantAccess = Arrays.asList(CHAMBERS_CLERK.getRoleName(), CHAMBERS_ADMIN.getRoleName(), ADVOCATES.getRoleName(), DEFENCE_LAWYERS.getRoleName());
    protected static final List<String> allowedUserGroupsForGranterToGrantAccess = Arrays.asList(CHAMBERS_CLERK.getRoleName(), CHAMBERS_ADMIN.getRoleName());
    protected static final List<String> allowedUserGroupsToRemoveGrantAccess = Arrays.asList(CHAMBERS_CLERK.getRoleName(), CHAMBERS_ADMIN.getRoleName());

    protected static final List<String> allowedUserGroupsForAssigneeToGetCaseAccess = Arrays.asList(ADVOCATES.getRoleName(), DEFENCE_LAWYERS.getRoleName());

    private static final List<DefencePermission> GRANTED_PERMISSIONS_FOR_CDES = asList(
            DefencePermission.VIEW_DOCUMENT_PERMISSION,
            DefencePermission.UPLOAD_DOCUMENT_PERMISSION,
            DefencePermission.VIEW_DEFENDANT_PERMISSION
    );

    private static final List<DefencePermission> GRANTED_PERMISSIONS = asList(
            DefencePermission.VIEW_DEFENDANT_PERMISSION
    );

    private GrantAccessUtil() {
    }

    public static List<Permission> preparePermissionList(final UUID target, final UUID source, final boolean isForAssociation, final Optional<List<String>> granteeGroupList) {

        if (isForAssociation) {
            return getListOfActionTypesForAssociation().stream()
                    .map(permissionType ->
                            Permission.permission()
                                    .withId(UUID.randomUUID())
                                    .withTarget(target)
                                    .withObject(permissionType.getObjectType())
                                    .withAction(permissionType.getActionType())
                                    .withSource(source)
                                    .withStatus(Status.ADDED)
                                    .build()
                    ).collect(Collectors.toList());
        } else {
            if (isGranteeInAllowedGroupsForAllGrants(granteeGroupList)) {
                return getListOfActionTypesForAll().stream()
                        .map(permissionType ->
                                Permission.permission()
                                        .withId(UUID.randomUUID())
                                        .withTarget(target)
                                        .withObject(permissionType.getObjectType())
                                        .withAction(permissionType.getActionType())
                                        .withSource(source)
                                        .withStatus(Status.ADDED)
                                        .build()
                        ).collect(Collectors.toList());
            } else {
                return getListOfActionTypes().stream()
                        .map(permissionType ->
                                Permission.permission()
                                        .withId(UUID.randomUUID())
                                        .withTarget(target)
                                        .withObject(permissionType.getObjectType())
                                        .withAction(permissionType.getActionType())
                                        .withSource(source)
                                        .withStatus(Status.ADDED)
                                        .build()
                        ).collect(Collectors.toList());
            }

        }
    }

    public static boolean isGranteeInAllowedGroupsForAllGrants(final Optional<List<String>> granteeGroupList) {
        return granteeGroupList.filter(strings -> !Collections.disjoint(strings, allowedUserGroupsForGranteeForAllGrantAccess)).isPresent();
    }

    public static List<Permission> preparePermissionList(final UUID source) {
        return preparePermissionList(UUID.randomUUID(), source, true, empty());
    }

    public static List<DefencePermission> getListOfActionTypesForAssociation() {
        return getListOfActionTypesForAll();
    }

    public static List<DefencePermission> getListOfActionTypesForAll() {
        return Collections.unmodifiableList(GRANTED_PERMISSIONS_FOR_CDES);
    }

    public static List<DefencePermission> getListOfActionTypes() {
        return Collections.unmodifiableList(GRANTED_PERMISSIONS);
    }

    public static boolean isGranteeInAllowedGroupsToGrantAccess(final List<String> granteeGroupList) {
        return !Collections.disjoint(granteeGroupList, allowedUserGroupsForGranteeToGrantAccess);
    }

    public static boolean isGranterInAllowedGroupsToGrantAccess(final List<String> granterGroupList) {
        return !Collections.disjoint(granterGroupList, allowedUserGroupsForGranterToGrantAccess);
    }

    public static boolean isUserInAllowedGroupsToRemoveGrantAccess(final List<String> granterGroupList) {
        return !Collections.disjoint(granterGroupList, allowedUserGroupsToRemoveGrantAccess);
    }

    public static boolean isUserBelongsToAssociatedOrganisation(final UUID associatedOrganisationId, final UUID orgId) {
        return associatedOrganisationId.equals(orgId);
    }

    public static boolean isUserHasPermissionToGrantSomeone(final UUID associatedOrganisationId, final UUID granterOrganisationId, final UUID granteeOrganisationId, final List<String> granterGroupList) {

        final boolean isUserBelongsToAssociatedOrganisation = isUserBelongsToAssociatedOrganisation(associatedOrganisationId, granterOrganisationId);

        if (isUserBelongsToAssociatedOrganisation) {
            return true;
        }

        final boolean isGranterInAllowedGroups = isGranterInAllowedGroupsToGrantAccess(granterGroupList);
        final boolean isGranteeAndGranterInSameOrganisation = isUsersInSameOrganisation(granterOrganisationId, granteeOrganisationId);

        return isGranterInAllowedGroups && isGranteeAndGranterInSameOrganisation;

    }

    public static boolean isUsersInSameOrganisation(final UUID granterOrganisationId, final UUID granteeOrganisationId) {
        return granterOrganisationId.equals(granteeOrganisationId);
    }

    public static boolean isUserHasPermissionToRemoveGrantAccess(final UUID granteeUserId, final UUID loggedInUserId, final UUID associatedOrganisationId, final Organisation loggedInUserOrganisation, final Organisation granteeOrganisation, final List<String> loggedInUserGroupList) {

        if (loggedInUserOrganisation == null) {
            return false;
        }

        if (granteeUserId.equals(loggedInUserId)) {
            return true;
        }

        if (isUserBelongsToAssociatedOrganisation(associatedOrganisationId, loggedInUserOrganisation.getOrgId())) {
            return true;
        }

        final boolean isUserInAllowedGroupsToRemoveGrantAccess = isUserInAllowedGroupsToRemoveGrantAccess(loggedInUserGroupList);
        final boolean isUsersInSameOrganisation = isUsersInSameOrganisation(loggedInUserOrganisation.getOrgId(), granteeOrganisation.getOrgId());

        return isUserInAllowedGroupsToRemoveGrantAccess && isUsersInSameOrganisation;
    }

    public static boolean isAssigneeInAllowedGroupsToGetCaseAccess(final List<String> assigneeGroupList) {
        return !Collections.disjoint(assigneeGroupList, allowedUserGroupsForAssigneeToGetCaseAccess);
    }

}
