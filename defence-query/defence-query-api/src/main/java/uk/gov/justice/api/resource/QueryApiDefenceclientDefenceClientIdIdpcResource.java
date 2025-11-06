
package uk.gov.justice.api.resource;

import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("defenceclient/{defenceClientId}/idpc")
public interface QueryApiDefenceclientDefenceClientIdIdpcResource {

    @GET
    @Produces({
            "application/vnd.defence.query.defence-client-idpc+json"
    })
    Response getDefenceclientByDefenceClientIdIdpc(
            @PathParam("defenceClientId") String defenceClientId,
            @HeaderParam(USER_ID) String userId);
}
