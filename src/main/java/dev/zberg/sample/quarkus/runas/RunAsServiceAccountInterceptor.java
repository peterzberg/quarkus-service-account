package dev.zberg.sample.quarkus.runas;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Optional;

@Interceptor
@RunAsServiceAccount
@Priority(Interceptor.Priority.APPLICATION)
public class RunAsServiceAccountInterceptor {
    private static final Logger LOGGER = Logger.getLogger(RunAsServiceAccountInterceptor.class.getName());
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final CurrentIdentityAssociation identityAssociation;
    private final OidcClients oidcClients;

    @Inject
    public RunAsServiceAccountInterceptor(final CurrentIdentityAssociation identityAssociation, final OidcClients oidcClients) {
        this.identityAssociation = identityAssociation;
        this.oidcClients = oidcClients;
    }

    @AroundInvoke
    public Object runAsServiceAccount(final InvocationContext context) throws Exception {
        final SecurityIdentity originalIdentity = identityAssociation.getIdentity();
        boolean identityChanged = false;
        try {
            final Optional<String> accessToken = getAccessTokenOfServiceAccount();

            if (accessToken.isPresent()) {
                final QuarkusSecurityIdentity newIdentity = buildServiceAccountIdentity(accessToken.get());
                identityAssociation.setIdentity(newIdentity);
                identityChanged = true;
            }

            return context.proceed();
        } catch (final Exception e) {
            LOGGER.warn("Unable to authenticate with service-account", e);
            return context.proceed();
        } finally {
            if (identityChanged) {
                identityAssociation.setIdentity(originalIdentity);
            }
        }
    }

    private static QuarkusSecurityIdentity buildServiceAccountIdentity(final String accessToken) {
        return QuarkusSecurityIdentity.builder()
                .setPrincipal(() -> "service-account")
                .addCredential(new io.quarkus.security.credential.TokenCredential(accessToken, TOKEN_TYPE_BEARER))
                .build();
    }

    private Optional<String> getAccessTokenOfServiceAccount() {
        try {
            final OidcClient serviceClient = oidcClients.getClient(); // get the default oidc client

            final String accessToken = serviceClient.getTokens().await().atMost(Duration.ofSeconds(2))
                    .getAccessToken();

            return Optional.of(accessToken);
        } catch (Exception e) {
            LOGGER.warn("Unable to obtain access token of service-account", e);
            return Optional.empty();
        }
    }
}
