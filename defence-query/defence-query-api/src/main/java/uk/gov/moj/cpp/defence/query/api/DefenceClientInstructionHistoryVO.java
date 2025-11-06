package uk.gov.moj.cpp.defence.query.api;

import java.util.UUID;

public class DefenceClientInstructionHistoryVO {

    private UUID id;

    private UUID userId;

    private UUID organisationId;

    private String instructionDate;

    private String name;

    public DefenceClientInstructionHistoryVO(final UUID id, final UUID userId, final UUID organisationId, final String instructionDate, final String name) {
        this.id = id;
        this.userId = userId;
        this.organisationId = organisationId;
        this.instructionDate = instructionDate;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getInstructionDate() {
        return instructionDate;
    }

    public String getName() {
        return name;
    }
}
