package uk.gov.moj.cpp.defence.query.api;


import static org.mockito.Mockito.verify;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.defence.query.view.DefenceAssociationQueryView;
import uk.gov.moj.cpp.defence.query.view.DefenceGrantAccessQueryView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceGrantAccessQueryApiTest {

    @Mock
    private JsonEnvelope query;

    @Mock
    private DefenceGrantAccessQueryView defenceGrantAccessQueryView;


    @InjectMocks
    private DefenceGrantAccessQueryApi defenceGrantAccessQueryApi;

    @Test
    public void getDefenceGrantAccessQueryApi() {
        defenceGrantAccessQueryApi.getDefenceClientGrantees(query);
        verify(defenceGrantAccessQueryView).getDefenceClientGrantees(query);
    }

    @Test
    public void getCaseGrantAccessQueryApi() {
        defenceGrantAccessQueryApi.getDefenceAccessForCase(query);
        verify(defenceGrantAccessQueryView).getCaseGrantee(query);

    }


}
