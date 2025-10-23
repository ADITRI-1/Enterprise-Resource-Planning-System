package com.iiitd.erp.core.service;


import com.iiitd.erp.Domain.AuthResultDomain;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthSessionService {
    AuthResultDomain loginAndCreateSession(String username, String password, HttpServletRequest request);
    AuthResultDomain currentUser(HttpServletRequest req);
}
