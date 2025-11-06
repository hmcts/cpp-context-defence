package uk.gov.moj.cpp.defence.query.view;

import java.time.LocalDate;
import java.util.UUID;

public class DefenceClientInstructionHistoryVO {

    private UUID id;

    private UUID userId;

    private UUID organisationId;

    private LocalDate instructionDate;

    public DefenceClientInstructionHistoryVO(final UUID id, final UUID userId, final UUID organisationId, final LocalDate instructionDate) {
        this.id = id;
        this.userId = userId;
        this.organisationId = organisationId;
        this.instructionDate = instructionDate;
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

    public LocalDate getInstructionDate() {
        return instructionDate;
    }
}
