package com.javarush.jira.login.internal.jwtauth;

import com.javarush.jira.login.UserTo;
import com.javarush.jira.login.internal.jwtauth.model.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(AuthenticationRestController.PATH_ROOT)
@RequiredArgsConstructor
public class AuthenticationRestController {
    public final static String PATH_ROOT = "/api/v1/auth";
    private final AuthenticationService authService;

    @PostMapping
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody UserTo userTo) {
        return ResponseEntity.ok(authService.authenticate(userTo));
    }

    @PostMapping("/refresh")
    public void refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authService.refreshToken(request, response);
    }
}
