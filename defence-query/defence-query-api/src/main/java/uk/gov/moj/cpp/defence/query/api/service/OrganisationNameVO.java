package uk.gov.moj.cpp.defence.query.api.service;


public class OrganisationNameVO {

    private final String organisationId;

    private final String organisationName;

    public OrganisationNameVO(final String organisationId, final String organisationName) {

        this.organisationId = organisationId;
        this.organisationName = organisationName;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }
}
