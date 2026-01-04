package dev.zberg.sample.quarkus.runas;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Optional;

@Dependent
public class RunAsServiceAccountExecutor {
    private final CurrentIdentityAssociation identityAssociation;
    private final OidcClients oidcClients;

    @Inject
    public RunAsServiceAccountExecutor(CurrentIdentityAssociation identityAssociation, OidcClients oidcClients) {
        this.identityAssociation = identityAssociation;
        this.oidcClients = oidcClients;
    }

    public <T, E extends Exception> T runAsServiceAccount(RunAsCallable<T, E> callable) throws E {
        final SecurityIdentity originalIdentity = identityAssociation.getIdentity();
        try {
            final String accessToken = getAccessTokenOfServiceAccount();
            final QuarkusSecurityIdentity newIdentity = buildServiceAccountIdentity(accessToken);
            identityAssociation.setIdentity(newIdentity);
            return callable.call();
        } finally {
            identityAssociation.setIdentity(originalIdentity);
        }
    }

    private String getAccessTokenOfServiceAccount() {
        return oidcClients.getClient().getTokens()
                .await()
                .atMost(Duration.ofSeconds(2))
                .getAccessToken();
    }

    private static QuarkusSecurityIdentity buildServiceAccountIdentity(final String accessToken) {
        return QuarkusSecurityIdentity.builder()
                .setPrincipal(() -> getUsernameFromAccessToken(accessToken).orElse("service-account"))
                .addCredential(new AccessTokenCredential(accessToken))
                .build();
    }

    private static Optional<String> getUsernameFromAccessToken(final String accessToken) {
        try {
            final DecodedJWT jwt = JWT.decode(accessToken);
            return Optional.ofNullable(jwt.getClaim("preferred_username").asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
