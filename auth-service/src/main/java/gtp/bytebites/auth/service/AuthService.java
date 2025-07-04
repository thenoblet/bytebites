package gtp.bytebites.auth.service;

import gtp.bytebites.auth.dto.request.LoginRequest;
import gtp.bytebites.auth.dto.request.RegisterRequest;
import gtp.bytebites.auth.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
}