package uk.gov.moj.cpp.defence.query.api.accesscontrol;

import static javax.json.Json.createObjectBuilder;

@SuppressWarnings("WeakerAccess")
public class RuleConstants {

    private static final String GROUP_DEFENCE_LAWYER_USER = "Defence Lawyers";
    private static final String GROUP_CHAMBERS_CLERK_USER = "Chambers Clerk";
    private static final String GROUP_CHAMBERS_ADMIN_USER = "Chambers Admin";
    private static final String GROUP_ADVOCATES_USER = "Advocates";
    private static final String GROUP_JUDGE = "Judge";
    private static final String GROUP_SYSTEM_USERS = "System Users";
    private static final String GROUP_CROWN_COURT_ADMIN = "Crown Court Admin";
    private static final String GROUP_LISTING_OFFICERS = "Listing Officers";
    private static final String GROUP_COURT_ASSOCIATE = "Court Associate";
    private static final String GROUP_JUDICIARY = "Judiciary";
    private static final String GROUP_COURT_CLERKS = "Court Clerks";
    private static final String GROUP_LEGAL_ADVISERS = "Legal Advisers";
    private static final String GROUP_COURT_ADMINISTRATORS = "Court Administrators";
    private static final String GROUP_POLICE_ADMIN = "Police Admin";
    private static final String GROUP_RECORDERS = "Recorders";
    private static final String GROUP_DJMC = "DJMC";
    private static final String GROUP_DEPUTIES = "Deputies";
    private static final String NCES = "NCES";
    private static final String GROUP_VICTIMS_WITNESS_CARE_ADMIN = "Victims & Witness Care Admin";
    private static final String GROUP_PROBATION_ADMIN = "Probation Admin";
    private static final String GROUP_CPS = "CPS";
    private static final String GROUP_YOS = "Youth Offending Service Admin";
    private static final String GROUP_MAGISTRATES = "Magistrates";
    private static final String PLEA_ACCESS = "plea-access";
    private static final String PLEA_VIEW = "plea-view";
    private static final String PLEA_ALLOCATION = "PLEA_ALLOCATION";
    private static final String OBJECT = "object";
    private static final String ACTION = "action";
    private static final String NON_CPS_PROSECUTOR_GROUP = "Non CPS Prosecutors";
    private static final String OPERATIONAL_DELIVERY_ADMIN = "Operational Delivery Admin";


    private RuleConstants() {
    }

    public static String[] getQueryClientByCriteriaGroups() {
        return new String[]{GROUP_DEFENCE_LAWYER_USER, GROUP_ADVOCATES_USER, NON_CPS_PROSECUTOR_GROUP};
    }

    public static String[] getOnlyCPSUsers() {
        return new String[]{GROUP_CPS, NON_CPS_PROSECUTOR_GROUP};
    }

    public static String[] getQueryClientByOrganisationIdGroups() {
        return new String[]{GROUP_DEFENCE_LAWYER_USER, GROUP_CHAMBERS_CLERK_USER, GROUP_CHAMBERS_ADMIN_USER, GROUP_ADVOCATES_USER};
    }

    public static String[] getQueryClientOrganisationGroups() {

        return new String[]{GROUP_SYSTEM_USERS, GROUP_DEFENCE_LAWYER_USER, GROUP_COURT_CLERKS, GROUP_COURT_ADMINISTRATORS, GROUP_CROWN_COURT_ADMIN,
                GROUP_LISTING_OFFICERS, GROUP_LEGAL_ADVISERS, GROUP_JUDICIARY, GROUP_COURT_ASSOCIATE, GROUP_JUDGE, GROUP_ADVOCATES_USER, NCES,
                GROUP_VICTIMS_WITNESS_CARE_ADMIN, GROUP_PROBATION_ADMIN, GROUP_CPS, GROUP_YOS, NON_CPS_PROSECUTOR_GROUP,OPERATIONAL_DELIVERY_ADMIN
        };
    }

    public static String[] getQueryClientAssociatedOrganisationGroups() {
        return new String[]{GROUP_SYSTEM_USERS, GROUP_DEFENCE_LAWYER_USER, GROUP_COURT_CLERKS, GROUP_COURT_ADMINISTRATORS, GROUP_CROWN_COURT_ADMIN,
                GROUP_LISTING_OFFICERS, GROUP_LEGAL_ADVISERS, GROUP_JUDICIARY, GROUP_COURT_ASSOCIATE, GROUP_JUDGE, GROUP_ADVOCATES_USER, NCES, GROUP_POLICE_ADMIN,
                GROUP_RECORDERS, GROUP_DJMC, GROUP_DEPUTIES, GROUP_VICTIMS_WITNESS_CARE_ADMIN, GROUP_PROBATION_ADMIN, GROUP_CPS, GROUP_YOS, GROUP_MAGISTRATES,OPERATIONAL_DELIVERY_ADMIN
        };
    }

    public static String[] eligibleOnlinePleaPermissions() {
        return new String[] {
                createObjectBuilder().add(OBJECT, PLEA_ALLOCATION).add(ACTION, PLEA_ACCESS).build().toString(),
                createObjectBuilder().add(OBJECT, PLEA_ALLOCATION).add(ACTION, PLEA_VIEW).build().toString()
        };
    }

    public static String[] pleaAndAllocationPermissions() {
        return new String[] {
                createObjectBuilder().add(OBJECT, PLEA_ALLOCATION).add(ACTION, PLEA_ACCESS).build().toString(),
                createObjectBuilder().add(OBJECT, PLEA_ALLOCATION).add(ACTION, PLEA_VIEW).build().toString()
        };
    }

    public static String[] getSystemUsersGroup() {
        return new String[]{GROUP_SYSTEM_USERS
        };
    }


}
