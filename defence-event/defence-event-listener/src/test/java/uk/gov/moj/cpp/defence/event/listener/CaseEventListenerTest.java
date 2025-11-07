package uk.gov.moj.cpp.defence.event.listener;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.events.ProsecutionCaseReceived;
import uk.gov.moj.cpp.defence.persistence.DefenceCaseRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;

import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
public class CaseEventListenerTest {

    @Mock
    private DefenceCaseRepository defenceCaseRepository;

    @Mock
    private Envelope<ProsecutionCaseReceived> envelope;

    @InjectMocks
    private CaseEventListener caseEventListener;

    @Test
    public void shouldRecordIdpcAccessHistory() {

        final UUID caseId = randomUUID();
        final String urn = "urn";

        final ProsecutionCaseReceived prosecutionCaseReceived = ProsecutionCaseReceived.prosecutionCaseReceived()
                .withCaseId(caseId)
                .withUrn(urn)
                .withIsCivil(TRUE)
                .withIsGroupMember(TRUE)
                .withProsecutingAuthority("prosecutingAuthority")
                .build();

        when(envelope.payload()).thenReturn(prosecutionCaseReceived);

        caseEventListener.prosecutionCaseReceived(envelope);

        verify(defenceCaseRepository).save(any(DefenceCase.class));
    }
}
