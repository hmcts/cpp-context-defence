package uk.gov.moj.defence.domain.common.pojo;

import java.io.Serializable;
import java.util.UUID;

public class CaseHearingAssignmentDetails implements Serializable {

    private final UUID assigneeId;

    private final UUID caseId;

    private final UUID hearingId;

    private final Boolean isAssigneeDefendingCase;

    private final Boolean isCps;

    private final Boolean isPolice;

    private final UUID prosecutionAuthorityId;

    private final String failureReason;

    private final Integer errorCode;


    public CaseHearingAssignmentDetails(UUID assigneeId, UUID caseId, UUID hearingId, Boolean isAssigneeDefendingCase,
                                        Boolean isCps, Boolean isPolice, UUID prosecutionAuthorityId, String failureReason, Integer errorCode) {
        this.assigneeId = assigneeId;
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.isAssigneeDefendingCase = isAssigneeDefendingCase;
        this.isCps = isCps;
        this.isPolice = isPolice;
        this.prosecutionAuthorityId = prosecutionAuthorityId;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Boolean getAssigneeDefendingCase() {
        return isAssigneeDefendingCase;
    }

    public Boolean getCps() {
        return isCps;
    }

    public Boolean getPolice() {
        return isPolice;
    }

    public UUID getProsecutionAuthorityId() {
        return prosecutionAuthorityId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
