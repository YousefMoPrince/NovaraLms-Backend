package com.amarjo.novaralms.auth.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_ROLE_KEY = "user_role";

    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void createSession(String name, String email, String role) {
        httpSession.setAttribute(USER_EMAIL_KEY, email);
        httpSession.setAttribute(USER_NAME_KEY, name);
        httpSession.setAttribute(USER_ROLE_KEY, role);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }

    public String getEmail() {
        Object email = httpSession.getAttribute(USER_EMAIL_KEY);
        return email != null ? email.toString() : null;
    }

    public boolean isSessionActive() {
        return httpSession.getAttribute(USER_EMAIL_KEY) != null;
    }

    public void invalidateSession() {
        SecurityContextHolder.clearContext();
        httpSession.invalidate();
    }
}