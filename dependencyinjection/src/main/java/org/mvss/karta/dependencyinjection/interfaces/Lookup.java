package org.mvss.karta.dependencyinjection.interfaces;

@FunctionalInterface
public interface Lookup<T> {
    T lookup(String name);
}
