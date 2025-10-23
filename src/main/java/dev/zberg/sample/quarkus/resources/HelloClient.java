package dev.zberg.sample.quarkus.resources;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@OidcClientFilter
@Path("/hello")
public interface HelloClient {
    @GET
    @Produces("text/plain")
    String hello();
}
