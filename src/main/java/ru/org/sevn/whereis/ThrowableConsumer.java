package ru.org.sevn.whereis;

@FunctionalInterface
public interface ThrowableConsumer<T, EXCEPTION extends Throwable> {
   void accept(T t) throws EXCEPTION;
}
