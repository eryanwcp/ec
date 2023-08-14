package com.eryansky.core.dialect.processor;

import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import org.thymeleaf.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.singleton;

public final class ShiroFacade {

    private ShiroFacade() {
        throw new UnsupportedOperationException();
    }

    public static boolean isAuthenticated() {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        return sessionInfo != null && sessionInfo.isAuthenticated();
    }

    public static boolean isNotAuthenticated() {
        return !ShiroFacade.isAuthenticated();
    }

    public static boolean isUser() {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        return sessionInfo != null && sessionInfo.getPrincipal() != null;
    }

    public static boolean isGuest() {
        return !ShiroFacade.isUser();
    }

    public static boolean hasPermission(final String permission) {
        return hasAllPermissions(singleton(permission));
    }

    public static boolean lacksPermission(final String permission) {
        return !ShiroFacade.hasPermission(permission);
    }

    public static boolean hasAnyPermissions(final Collection<String> permissions) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            for (final String permission : permissions) {
                if (SecurityUtils.isPermitted(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyPermissions(final String... permissions) {
        return hasAnyPermissions(Arrays.asList(permissions));
    }


    public static boolean hasAllPermissions(Collection<String> permissions) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            if (permissions.isEmpty()) {
                return false;
            }

            for (final String permission : permissions) {
                if (!SecurityUtils.isPermitted(permission)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean hasAllPermissions(String... permissions) {
        return hasAllPermissions(Arrays.asList(permissions));
    }

    public static boolean hasRole(final String roleName) {
        return hasAllRoles(singleton(roleName));
    }

    public static boolean lacksRole(final String roleName) {
        return !hasRole(roleName);
    }

    public static boolean hasAnyRoles(final Collection<String> roles) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            for (final String role : roles) {
                if (SecurityUtils.isPermittedRole(StringUtils.trim(role))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyRoles(final String... roles) {
        return hasAnyRoles(Arrays.asList(roles));
    }

    public static boolean hasAllRoles(final Collection<String> roles) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            if (roles.isEmpty()) {
                return false;
            }

            for (final String role : roles) {
                if (!SecurityUtils.isPermittedRole(StringUtils.trim(role))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean hasAllRoles(final String... roles) {
        return hasAllRoles(Arrays.asList(roles));
    }

    public static String getPrincipalText(final String type, final String property) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo == null) {
            return "";
        }

        Object principal = sessionInfo.getPrincipal();

        if (type != null || property != null) {
            if (type != null) {
                principal = getPrincipalFromClassName(type);
            }
            if (principal != null) {
                return (property == null) ? principal.toString() : getPrincipalProperty(principal, property);
            }
        }

        return principal != null ? principal.toString() : "";
    }

    public static Object getPrincipalFromClassName(final String type) {
        Object principal;

        try {
            final Class<?> cls = Class.forName(type);
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            principal = sessionInfo.getPrincipal();
        } catch (final ClassNotFoundException e) {
            String message = "Unable to find class for name [" + type + "]";
            throw new IllegalArgumentException(message, e);
        }

        return principal;
    }

    public static String getPrincipalProperty(final Object principal, final String property) {
        try {
            final BeanInfo bi = Introspector.getBeanInfo(principal.getClass());
            for (final PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if (pd.getName().equals(property)) {
                    final Object value = pd.getReadMethod().invoke(principal, (Object[]) null);
                    return String.valueOf(value);
                }
            }
        } catch (final Exception e) {
            String message = "Error reading property [" + property + "] from principal of type [" + principal.getClass().getName() + "]";
            throw new IllegalArgumentException(message, e);
        }

        throw new IllegalArgumentException("Property [" + property + "] not found in principal of type [" + principal.getClass().getName() + "]");
    }

}