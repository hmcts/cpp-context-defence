package uk.gov.moj.cpp.defence.query.view;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ProsecutionCaseAssigneeVO {

    private UUID assigneeUserId;
    private String assigneeName;
    private UUID assigneeOrganisationId;
    private String assigneeOrganisationName;

    private String status;
    private String representation;
    private String representing;
    private ZonedDateTime assignedDate;

    public ProsecutionCaseAssigneeVO(UUID assigneeUserId, String assigneeName, UUID assigneeOrganisationId,
                                     String assigneeOrganisationName, String status, String representation,
                                     String representing, ZonedDateTime assignedDate) {
        this.assigneeUserId = assigneeUserId;
        this.assigneeName = assigneeName;
        this.assigneeOrganisationId = assigneeOrganisationId;
        this.assigneeOrganisationName = assigneeOrganisationName;
        this.status = status;
        this.representation = representation;
        this.representing = representing;
        this.assignedDate = assignedDate;
    }

    public UUID getAssigneeUserId() {
        return assigneeUserId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public UUID getAssigneeOrganisationId() {
        return assigneeOrganisationId;
    }

    public String getAssigneeOrganisationName() {
        return assigneeOrganisationName;
    }

    public String getStatus() {
        return status;
    }

    public String getRepresentation() {
        return representation;
    }

    public String getRepresenting() {
        return representing;
    }

    public ZonedDateTime getAssignedDate() {
        return assignedDate;
    }
}
