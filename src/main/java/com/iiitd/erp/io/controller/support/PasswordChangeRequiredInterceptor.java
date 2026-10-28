package com.iiitd.erp.io.controller.support;

import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Users holding a temporary password can only reach /api/auth/** (login, me, change-password,
 * logout) and /api/system/** until they choose their own password.
 */
@Component
public class PasswordChangeRequiredInterceptor implements HandlerInterceptor {

    private final CurrentUser currentUser;

    public PasswordChangeRequiredInterceptor(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (currentUser.account().map(UserAuth::isMustChangePassword).orElse(false)) {
            throw ErpException.of(ErrorCode.PASSWORD_CHANGE_REQUIRED, Map.of());
        }
        return true;
    }
}
