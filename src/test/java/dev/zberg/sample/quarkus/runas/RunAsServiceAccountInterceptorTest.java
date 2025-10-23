package dev.zberg.sample.quarkus.runas;

import io.quarkus.oidc.client.OidcClients;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunAsServiceAccountInterceptorTest {
    @Mock
    private CurrentIdentityAssociation identityAssociation;
    @Mock
    private OidcClients oidcClients;

    @InjectMocks
    private RunAsServiceAccountInterceptor testee;

    @Test
    void runAsServiceAccount_exceptionOptainingToken_exceptionIgnoredProceed() throws Exception {
        final InvocationContext context = mock(InvocationContext.class);
        when(oidcClients.getClient()).thenThrow(new IllegalStateException("error"));

        testee.runAsServiceAccount(context);

        verify(identityAssociation, never()).setIdentity(Mockito.isA(SecurityIdentity.class));
        verify(context).proceed();

    }

}