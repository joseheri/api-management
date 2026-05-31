package com.lmco.jsf.fmt.dataprovider.security.apikey.resource;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.DisableApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiClientService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/admin/api-clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiClientResource {

    @Inject
    private ApiClientService apiClientService;

    @POST
    public Response createClient(CreateApiClientRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(apiClientService.createClient(request))
                .build();
    }

    @GET
    public Response listClients() {
        return Response.ok(apiClientService.listClients()).build();
    }

    @GET
    @Path("/{clientId}")
    public Response getClient(@PathParam("clientId") Long clientId) {
        return Response.ok(apiClientService.getClient(clientId)).build();
    }

    @POST
    @Path("/{clientId}/disable")
    public Response disableClient(@PathParam("clientId") Long clientId, DisableApiClientRequest request) {
        return Response.ok(apiClientService.disableClient(clientId, request)).build();
    }

    @POST
    @Path("/{clientId}/enable")
    public Response enableClient(@PathParam("clientId") Long clientId) {
        return Response.ok(apiClientService.enableClient(clientId)).build();
    }
}
