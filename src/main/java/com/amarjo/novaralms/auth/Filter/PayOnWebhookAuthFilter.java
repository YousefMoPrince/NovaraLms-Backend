package com.amarjo.novaralms.auth.Filter;

import com.amarjo.novaralms.payment.Model.MainServerDB;
import com.amarjo.novaralms.payment.Repo.MainServerRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class PayOnWebhookAuthFilter extends OncePerRequestFilter {

    @Autowired
    private MainServerRepo mainServerRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/web/v1/payment/webhook")) {

            String apiKey = request.getHeader("X-PAYON-API-KEY");
            String apiSecret = request.getHeader("X-PAYON-API-SECRET");

            if (apiKey != null && apiSecret != null) {

                Optional<MainServerDB> mainServerOpt = mainServerRepo.findFirstBy();

                if (mainServerOpt.isPresent() &&
                        apiKey.equals(mainServerOpt.get().getCompanyApiKey()) &&
                        apiSecret.equals(mainServerOpt.get().getCompanyApiSecret())) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            "PayOnSystem", null, List.of(new SimpleGrantedAuthority("ROLE_PAYON_SYSTEM")));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }


        filterChain.doFilter(request, response);
    }
}