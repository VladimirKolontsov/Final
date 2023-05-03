package com.javarush.jira.login.internal.jwtauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.UserTo;
import com.javarush.jira.login.internal.jwtauth.model.AuthenticationResponse;
import com.javarush.jira.login.internal.jwtauth.model.Token;
import com.javarush.jira.login.internal.jwtauth.model.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    public AuthenticationResponse authenticate(UserTo userTo) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userTo.getEmail(),
                        userTo.getPassword()
                )
        );
        AuthUser authUser = (AuthUser) userDetailsService.loadUserByUsername(userTo.getEmail());
        String accessToken = jwtService.generateToken(authUser);
        String refreshToken = jwtService.generateRefreshToken(authUser);
        revokeAllUserTokens(authUser);
        saveUserToken(authUser, accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (nonNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            AuthUser authUser = (AuthUser) userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(refreshToken, authUser)) {
                String accessToken = jwtService.generateToken(authUser);
                revokeAllUserTokens(authUser);
                saveUserToken(authUser, accessToken);
                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private void saveUserToken(AuthUser authUser, String jwtToken) {
        tokenRepository.save(
                Token.builder()
                        .user(authUser.getUser())
                        .token(jwtToken)
                        .tokenType(TokenType.BEARER)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    private void revokeAllUserTokens(AuthUser authUser) {
        List<Token> allValidTokens = tokenRepository.findAllValidTokenByUser(authUser.id());
        if (allValidTokens.isEmpty()) {
            return;
        }
        allValidTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(allValidTokens);
    }
}
