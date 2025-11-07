package uk.gov.moj.cpp.defence.query.view;

import java.util.UUID;

public class AssociatedOrganisationVO {

    private final UUID organisationId;
    private final String organisationName;

    public AssociatedOrganisationVO(final UUID organisationId, final String organisationName) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

}
