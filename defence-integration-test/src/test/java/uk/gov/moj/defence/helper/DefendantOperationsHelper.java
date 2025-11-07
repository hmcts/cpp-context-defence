package uk.gov.moj.defence.helper;

import static uk.gov.moj.defence.util.TestUtils.getPayloadForCreatingRequest;
import static uk.gov.moj.defence.util.TestUtils.postMessageToTopic;

public class DefendantOperationsHelper {

    private static final String DEFENDANT_CHANGE_PAYLOAD_FOR_INDIVIDUAL = "stub-data/public-events/public.progression.case-defendant-changed-individual.json";
    private static final String DEFENDANT_CHANGE_PAYLOAD_FOR_CORPORATE = "stub-data/public-events/public.progression.case-defendant-changed-corporate.json";
    private static final String OFFENCES_CHANGE_ADD_DELETE_PAYLOAD = "stub-data/public-events/public.progression.defendant-offences-changed.json";
    private static final String OFFENCES_UPDATE_ADD_DELETE_PAYLOAD = "stub-data/public-events/public.progression.defendant-offences-updated.json";

    private String getPayload(final String mockFile, final String defendantId, final String caseId) {
        return getPayloadForCreatingRequest(mockFile)
                .replace("DEFENDANT_ID", defendantId)
                .replace("CASE_ID", caseId);
    }

    private String getPayload(final String mockFile, final String defendantId, final String caseId, final String offenceId1, final String offenceId2) {
        return getPayloadForCreatingRequest(mockFile)
                .replace("DEFENDANT_ID", defendantId)
                .replace("CASE_ID", caseId)
                .replace("OFFENCE_ID1", offenceId1)
                .replace("OFFENCE_ID2", offenceId2);
    }

    public void updateIndividualDefendant(final String defendantId, final String caseId) {
        final String updatePayload = getPayload(DEFENDANT_CHANGE_PAYLOAD_FOR_INDIVIDUAL, defendantId, caseId);
        postMessageToTopic(updatePayload, "public.progression.case-defendant-changed");
    }

    public void updateCorporateDefendant(final String defendantId, final String caseId) {
        final String updatePayload = getPayload(DEFENDANT_CHANGE_PAYLOAD_FOR_CORPORATE, defendantId, caseId);
        postMessageToTopic(updatePayload, "public.progression.case-defendant-changed");
    }

    public void updateOffenderDetails(final String defendantId, final String caseId, final String offenceId1, final String offenceId2) {
        final String updatePayload = getPayload(OFFENCES_CHANGE_ADD_DELETE_PAYLOAD, defendantId, caseId, offenceId1, offenceId2);
        postMessageToTopic(updatePayload, "public.progression.defendant-offences-changed");
    }

    public void updateExistOffender(final String defendantId, final String caseId, final String offenceId) {
        final String updatePayload = getPayload(OFFENCES_UPDATE_ADD_DELETE_PAYLOAD, defendantId, caseId, offenceId, "");
        postMessageToTopic(updatePayload, "public.progression.defendant-offences-changed");
    }


}