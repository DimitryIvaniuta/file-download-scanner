package com.scanner.download.service;

import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

    // Simulated authentication. In a real application, perform an OAuth2 resource owner password or another backend flow.
    public String authenticate(String email, String password) {
        // For demonstration, accept only one specific credential.
        if ("user@gmail.com".equals(email) && "secret".equals(password)) {
            return "dummy-google-token";
        }
        return null;
    }
}
