package uk.gov.moj.defence.helper;

import static java.lang.System.currentTimeMillis;
import static java.text.MessageFormat.format;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.defence.util.RestHelper.pollForResponse;
import static uk.gov.moj.defence.util.TestUtils.getPayloadForCreatingRequest;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

import java.util.Date;
import java.util.List;

import com.jayway.jsonpath.ReadContext;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.hamcrest.Matcher;

public class AdvocateAccessHelper {


    private static final String CASE_ACCESS_QUERY_ENDPOINT = "/cases/{0}/cps-assignees";
    private static final String CASE_ACCESS_QUERY_MEDIA_TYPE = "application/vnd.defence.query.case-cps-assignees+json";

    private static final String CASE_ID = "CASE_ID";
    private static final String ASSIGNEE_USER_ID = "ASSIGNEE_USER_ID";
    private static final String ASSIGNEE_ORG_ID = "ASSIGNEE_ORG_ID";
    private static final String ASSIGNOR_USER_ID = "ASSIGNOR_USER_ID";
    private static final String ASSIGNOR_ORG_ID = "ASSIGNOR_ORG_ID";
    private static final String ASSIGNMENT_TIMESTAMP = "ASSIGNMENT_TIMESTAMP";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";


    public void sendCaseAssignedToAdvocateEvent(final String caseId,
                                                final String assigneeUserId, final String assigneeOrgId,
                                                final String assignorUserId, final String assignorOrgId) {
        final String updatePayload = getPayloadForCreatingRequest("stub-data/private-events/defence.events.case-assigned-to-advocate.json")
                .replace(CASE_ID, caseId)
                .replace(ASSIGNEE_USER_ID, assigneeUserId)
                .replace(ASSIGNEE_ORG_ID, assigneeOrgId)
                .replace(ASSIGNOR_USER_ID, assignorUserId)
                .replace(ASSIGNOR_ORG_ID, assignorOrgId)
                .replace(ASSIGNMENT_TIMESTAMP, DateFormatUtils.format(new Date(), DATETIME_FORMAT));

        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

        try (final JMSTopicHelper jmsTopicHelper = new JMSTopicHelper()) {
            jmsTopicHelper.startProducer("defence.event");
            jmsTopicHelper.sendEventMessage("defence.events.case-assigned-to-advocate",
                    stringToJsonObjectConverter.convert(updatePayload),
                    randomUUID(),
                    currentTimeMillis());
        }
    }

    public static void verifyUserAssignmentToTheCase(final String caseId, final String userId, List<Matcher<? super ReadContext>> matchers) {
        pollForResponse(format(CASE_ACCESS_QUERY_ENDPOINT, caseId),
                CASE_ACCESS_QUERY_MEDIA_TYPE,
                userId,
                matchers);
    }
}
