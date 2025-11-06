package uk.gov.moj.cpp.defence.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.events.IdpcAccessRecorded;
import uk.gov.moj.cpp.defence.persistence.IdpcAccessHistoryRepository;
import uk.gov.moj.cpp.defence.persistence.entity.IdpcAccess;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IdpcAccessEventListenerTest {

    @Mock
    private IdpcAccessHistoryRepository idpcAccessHistoryRepository;

    @Mock
    private Envelope<IdpcAccessRecorded> envelope;

    @InjectMocks
    private IdpcAccessEventListener idpcAccessEventListener;

    @Test
    public void shouldRecordIdpcAccessHistory() {

        UUID idpcId = randomUUID();
        UUID orgId = randomUUID();
        UUID usrId = randomUUID();
        UUID defId = randomUUID();
        UUID materialId = randomUUID();
        ZonedDateTime accessTime = ZonedDateTime.now();

        final IdpcAccessRecorded idpcAccess = IdpcAccessRecorded.idpcAccessRecorded()
                .withDefenceClientId(defId)
                .withAccessTimestamp(accessTime)
                .withIdpcDetailsId(idpcId)
                .withOrganisationId(orgId)
                .withUserId(usrId)
                .withMaterialId(materialId)
                .build();

        when(envelope.payload()).thenReturn(idpcAccess);

        idpcAccessEventListener.recordIdpcAccess(envelope);

        verify(idpcAccessHistoryRepository,  times(1)).save(any(IdpcAccess.class));
    }
}
