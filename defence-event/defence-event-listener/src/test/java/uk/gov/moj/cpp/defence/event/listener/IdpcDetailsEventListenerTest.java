package uk.gov.moj.cpp.defence.event.listener;

import static java.lang.Boolean.FALSE;
import static java.time.LocalDate.now;
import static java.time.LocalDate.of;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.IdpcDetails;
import uk.gov.moj.cpp.defence.events.IdpcDetailsRecorded;
import uk.gov.moj.cpp.defence.persistence.DefenceCaseRepository;
import uk.gov.moj.cpp.defence.persistence.DefenceClientRepository;
import uk.gov.moj.cpp.defence.persistence.IdpcDetailsRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IdpcDetailsEventListenerTest {

    public static final UUID DEFENCE_CLIENT_ID = randomUUID();

    @Mock
    private DefenceClientRepository defenceClientRepository;

    @Mock
    private Envelope<IdpcDetailsRecorded> envelope;

    @Mock
    private IdpcDetailsRepository idpcDetailsRepository;

    @Mock
    private DefenceCaseRepository defenceCaseRepository;

    @Captor
    private ArgumentCaptor<uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails> idpcDetailsArgumentCaptor;

    @InjectMocks
    private IdpcDetailsEventListener idpcDetailsEventListener;


    @Test
    public void shouldRecordIdpcDetailsToDefenceClientEntity() {
        final UUID caseId = randomUUID();
        final DefenceClient defClient = new DefenceClient(DEFENCE_CLIENT_ID, "FIRST NAME", "LAST NAME", caseId, of(1970, 5, 17), randomUUID());

        final IdpcDetails idpcDetailsVo = IdpcDetails.idpcDetails()
                .withMaterialId(randomUUID())
                .withPageCount(11)
                .withSize("2.5Mb")
                .withPublishedDate(now())
                .build();

        final IdpcDetailsRecorded idpcDetails = IdpcDetailsRecorded.idpcDetailsRecorded()
                .withDefenceClientId(DEFENCE_CLIENT_ID)
                .withIdpcDetails(idpcDetailsVo)
                .build();

        when(envelope.payload()).thenReturn(idpcDetails);
        when(defenceClientRepository.findOptionalBy(any())).thenReturn(Optional.of(defClient));
        when(defenceCaseRepository.findBy(any())).thenReturn(new DefenceCase(caseId, "ptiUrn", "TFL", FALSE, FALSE));

        idpcDetailsEventListener.recordIdpcDetails(envelope);

        verify(idpcDetailsRepository).save(idpcDetailsArgumentCaptor.capture());
        final uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails idpcDetailsPersistedEntity = idpcDetailsArgumentCaptor.getValue();

        assertThat(idpcDetailsPersistedEntity.getMaterialId(), is(idpcDetails.getIdpcDetails().getMaterialId()));
        assertThat(idpcDetailsPersistedEntity.getDefenceClientId(), is(DEFENCE_CLIENT_ID));
        assertThat(idpcDetailsPersistedEntity.getPublishedDate(), is(idpcDetailsVo.getPublishedDate()));
        assertThat(idpcDetailsPersistedEntity.getDocumentName(), is("LAST NAME FIRST NAME PTIURN IDPC"));
        assertThat(idpcDetailsPersistedEntity.getSize(), is(idpcDetailsVo.getSize()));
        assertThat(idpcDetailsPersistedEntity.getPageCount(), is(idpcDetailsVo.getPageCount()));
    }

    @Test
    public void shouldRecordIdpcDetailsToDefenceClientEntityForCorporate() {
        final UUID caseId = randomUUID();
        final DefenceClient defClient = new DefenceClient(DEFENCE_CLIENT_ID, "ORG NAME", caseId, randomUUID() );

        final IdpcDetails idpcDetailsVo = IdpcDetails.idpcDetails()
                .withMaterialId(randomUUID())
                .withPageCount(11)
                .withSize("2.5Mb")
                .withPublishedDate(now())
                .build();

        final IdpcDetailsRecorded idpcDetails = IdpcDetailsRecorded.idpcDetailsRecorded()
                .withDefenceClientId(DEFENCE_CLIENT_ID)
                .withIdpcDetails(idpcDetailsVo)
                .build();

        when(envelope.payload()).thenReturn(idpcDetails);
        when(defenceClientRepository.findOptionalBy(any())).thenReturn(Optional.of(defClient));
        when(defenceCaseRepository.findBy(any())).thenReturn(new DefenceCase(caseId, "ptiUrn", "TFL", FALSE, FALSE));

        idpcDetailsEventListener.recordIdpcDetails(envelope);

        verify(idpcDetailsRepository).save(idpcDetailsArgumentCaptor.capture());
        final uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails idpcDetailsPersistedEntity = idpcDetailsArgumentCaptor.getValue();

        assertThat(idpcDetailsPersistedEntity.getMaterialId(), is(idpcDetails.getIdpcDetails().getMaterialId()));
        assertThat(idpcDetailsPersistedEntity.getDefenceClientId(), is(DEFENCE_CLIENT_ID));
        assertThat(idpcDetailsPersistedEntity.getPublishedDate(), is(idpcDetailsVo.getPublishedDate()));
        assertThat(idpcDetailsPersistedEntity.getDocumentName(), is("ORG NAME PTIURN IDPC"));
        assertThat(idpcDetailsPersistedEntity.getSize(), is(idpcDetailsVo.getSize()));
        assertThat(idpcDetailsPersistedEntity.getPageCount(), is(idpcDetailsVo.getPageCount()));
    }

    @Test
    public void shouldNotRecordIdpcDetailsToDefenceClientEntityIsNotThere() {
        final UUID caseId = randomUUID();

        final IdpcDetails idpcDetailsVo = IdpcDetails.idpcDetails()
                .withMaterialId(randomUUID())
                .withPageCount(11)
                .withSize("2.5Mb")
                .withPublishedDate(now())
                .build();

        final IdpcDetailsRecorded idpcDetails = IdpcDetailsRecorded.idpcDetailsRecorded()
                .withDefenceClientId(DEFENCE_CLIENT_ID)
                .withIdpcDetails(idpcDetailsVo)
                .build();

        when(envelope.payload()).thenReturn(idpcDetails);
        when(defenceClientRepository.findOptionalBy(any())).thenReturn(empty());

        idpcDetailsEventListener.recordIdpcDetails(envelope);

        verify(idpcDetailsRepository, never()).save(idpcDetailsArgumentCaptor.capture());

    }
}
