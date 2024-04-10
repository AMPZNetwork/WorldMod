package org.comroid.mcsd.core.util;

import lombok.extern.java.Log;
import org.comroid.api.Polyfill;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.java.StackTraceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Optional;
import java.util.logging.Level;

/**
 * brought to you by ChatGPT
 */
@Log
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static Wrap<ApplicationContext> get() {
        return ()->applicationContext;
    }

    public static <T, R extends T> R bean(Class<T> type) {
        return bean(type, null);
    }

    public static <T, R extends T> R bean(Class<?super T> type, @Nullable String name) {
        try {
            var context = get().assertion("Context not set");
            return Polyfill.uncheckedCast(name == null ? context.getBean(type) : context.getBean(name, type));
        } catch (NoUniqueBeanDefinitionException nubde) {
            throw new RuntimeException("More than one bean definition found for Bean '%s' of type %s".formatted(name, StackTraceUtils.lessSimpleName(type)), nubde);
        } catch (NoSuchBeanDefinitionException nsbde) {
            throw new RuntimeException("No bean definition found for Bean of type "+ StackTraceUtils.lessSimpleName(type), nsbde);
        }
    }

    public static <T, R extends T> Optional<R> wrap(Class<T> type) {
        return wrap(type, null);
    }

    public static <T, R extends T> Optional<R> wrap(Class<T> type, @Nullable String name) {
        try {
            return Polyfill.uncheckedCast(name == null
                    ? get().wrap().map(x -> x.getBean(type))
                    : get().wrap().map(x -> x.getBean(name, type)));
        } catch (Throwable t) {
            log.log(Level.WARNING, "Bean '%s' of type %s could not be obtained; %s".formatted(name, StackTraceUtils.lessSimpleName(type), t.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}
