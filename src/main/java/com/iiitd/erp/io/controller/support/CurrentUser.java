package com.iiitd.erp.io.controller.support;

import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.entity.erp_db.Instructor;
import com.iiitd.erp.persistent.persistence.repository.InstructorRepository;
import com.iiitd.erp.persistent.persistence.repository.UserAuthRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the logged-in user from the security context. The session only holds the
 * username, so path identifiers (student user id, instructor code) are checked against
 * the account they belong to rather than compared with the username directly.
 */
@Component
public class CurrentUser {

    private final UserAuthRepository users;
    private final InstructorRepository instructors;

    public CurrentUser(UserAuthRepository users, InstructorRepository instructors) {
        this.users = users;
        this.instructors = instructors;
    }

    public Optional<UserAuth> account() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return users.findByUsername(auth.getName());
    }

    public Optional<UserAuth> student() {
        return account().filter(u -> "student".equalsIgnoreCase(u.getRole()));
    }

    public Optional<Instructor> instructor() {
        return account()
                .filter(u -> "instructor".equalsIgnoreCase(u.getRole()))
                .flatMap(u -> instructors.findByInstructorId(u.getUserId()));
    }

    /** True when the caller is a student and {@code studentUserId} is their own user id. */
    public boolean isStudent(String studentUserId) {
        return student().map(u -> u.getUserId().toString().equalsIgnoreCase(studentUserId)).orElse(false);
    }

    /** True when the caller is an instructor and {@code instructorCode} is their own code. */
    public boolean isInstructor(String instructorCode) {
        return instructor().map(i -> i.getInstructorCode().equalsIgnoreCase(instructorCode)).orElse(false);
    }

    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : auth.getName();
    }
}
