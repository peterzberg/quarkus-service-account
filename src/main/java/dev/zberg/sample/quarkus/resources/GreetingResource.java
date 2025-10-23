package dev.zberg.sample.quarkus.resources;

import dev.zberg.sample.quarkus.runas.RunAsServiceAccount;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
public class GreetingResource {

    private final SecurityIdentity securityIdentity;
    private final HelloClient helloClient;

    @Inject
    public GreetingResource(final SecurityIdentity securityIdentity, @RestClient final HelloClient helloClient) {
        this.securityIdentity = securityIdentity;
        this.helloClient = helloClient;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        final String name = securityIdentity.isAnonymous() ? "stranger" : securityIdentity.getPrincipal().getName();
        return "Hello " + name;
    }

    @GET
    @Path("/helloServiceAccount")
    @Produces(MediaType.TEXT_PLAIN)
    @RunAsServiceAccount
    public String helloServiceAccount() {
        return hello();
    }

    @GET
    @Path("/callHelloAsServiceAccount")
    @Produces(MediaType.TEXT_PLAIN)
    @RunAsServiceAccount
    public String callHelloAsServiceAccount() {
        return helloClient.hello();
    }
}
