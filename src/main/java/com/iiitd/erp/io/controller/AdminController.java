package com.iiitd.erp.io.controller;


import com.iiitd.erp.core.service.AdminService;
import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import com.iiitd.erp.io.dto.response.CreateUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/admin/")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService){
        this.adminService = adminService;
    }


    @PostMapping("/create/student")
    public ResponseEntity<CreateUserResponse> createStudentUser(@RequestBody StudentCreateRequest studentCreateRequest, HttpServletRequest req){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have admin privileges");
        }

        CreateUserResponse createUserResponse = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToCreateUserResponse(adminService.createStudentUser(REQUEST_TO_DOMAIN_MAPPER_INSTANCE.studentUserRequestToDomain(studentCreateRequest)));
        return ResponseEntity.ok(loginResponse);
    }
}


//createUser -- student,instructor
//--student (name,roll_no,program_id, yearOfRegistration)


//createCourse
//createSection
//toggleMaintain