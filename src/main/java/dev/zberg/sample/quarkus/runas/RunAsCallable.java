package dev.zberg.sample.quarkus.runas;

@FunctionalInterface
public interface RunAsCallable<T, E extends Throwable> {

    T call() throws E;
}
