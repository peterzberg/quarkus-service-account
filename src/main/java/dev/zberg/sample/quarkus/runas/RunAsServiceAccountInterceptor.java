package dev.zberg.sample.quarkus.runas;

import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

@Interceptor
@RunAsServiceAccount
@Priority(Interceptor.Priority.APPLICATION)
public class RunAsServiceAccountInterceptor {
    private static final Logger LOGGER = Logger.getLogger(RunAsServiceAccountInterceptor.class.getName());
    private final RunAsServiceAccountExecutor runAsServiceAccountExecutor;

    @Inject
    public RunAsServiceAccountInterceptor(RunAsServiceAccountExecutor runAsServiceAccountExecutor) {
        this.runAsServiceAccountExecutor = runAsServiceAccountExecutor;
    }

    @AroundInvoke
    public Object runAsServiceAccount(final InvocationContext context) throws Exception {
        LOGGER.debugf("Running method as service account: %s#%s", context.getMethod().getDeclaringClass().getName(), context.getMethod().getName());

        if (!Infrastructure.canCallerThreadBeBlocked()) {
            throw new IllegalStateException("Annotation @" + RunAsServiceAccount.class.getName() + " can't be called on a non blockable thread");
        }
        return this.runAsServiceAccountExecutor.runAsServiceAccount(context::proceed);
    }

}
