package uk.gov.moj.cpp.defence.query.view;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DefenceClientIdpcAccessOrganisations {
    private final UUID defenceClientId;
    private final UUID caseId;
    private final String prosecutionAuthorityCode;
    private final UUID defendantId;
    private final AssociatedOrganisationVO associatedOrganisationVO;
    private final AssociatedOrganisationVO lastAssociatedOrganisationVO;
    private final List<DefenceClientInstructionHistoryVO> instructionHistory;
    private final String caseUrn;

    private final List<OrderedOrganisationDetailsVO> idpcAccessingOrganisations;
    private final boolean lockedByRepOrder;

    public DefenceClientIdpcAccessOrganisations(final UUID defenceClientId,
                                                final UUID caseId,
                                                final UUID defendantId,
                                                final AssociatedOrganisationVO associatedOrganisationVO,
                                                final AssociatedOrganisationVO lastAssociatedOrganisationVO,
                                                final List<OrderedOrganisationDetailsVO> idpcAccessingOrganisations,
                                                final List<DefenceClientInstructionHistoryVO> instructionHistory,
                                                final boolean lockedByRepOrder,
                                                final String prosecutionAuthorityCode,
                                                final String caseUrn) {

        this.defenceClientId = defenceClientId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.associatedOrganisationVO = associatedOrganisationVO;
        this.lastAssociatedOrganisationVO = lastAssociatedOrganisationVO;
        this.idpcAccessingOrganisations = new ArrayList<>(idpcAccessingOrganisations);
        this.instructionHistory = new ArrayList<>(instructionHistory);
        this.lockedByRepOrder = lockedByRepOrder;
        this.prosecutionAuthorityCode = prosecutionAuthorityCode;
        this.caseUrn = caseUrn;
    }

    public UUID getDefenceClientId() {
        return defenceClientId;
    }

    public List<OrderedOrganisationDetailsVO> getIdpcAccessingOrganisations() {
        return unmodifiableList(idpcAccessingOrganisations);
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public AssociatedOrganisationVO getAssociatedOrganisationVO() {
        return associatedOrganisationVO;
    }

    public AssociatedOrganisationVO getLastAssociatedOrganisationVO() {
        return lastAssociatedOrganisationVO;
    }

    public List<DefenceClientInstructionHistoryVO> getInstructionHistory() {
        return unmodifiableList(instructionHistory);
    }

    public boolean isLockedByRepOrder() {
        return lockedByRepOrder;
    }

    public String getProsecutionAuthorityCode() { return prosecutionAuthorityCode; }

    public String getCaseUrn() {
        return caseUrn;
    }
}
