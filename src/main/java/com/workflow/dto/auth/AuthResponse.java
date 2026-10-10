package com.workflow.dto.auth;

import com.workflow.entity.enums.Role;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Role role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String username, String email, Role role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
