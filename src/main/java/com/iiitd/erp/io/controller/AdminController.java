package com.iiitd.erp.io.controller;


import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.io.dto.response.CreateUserResponse;
import com.iiitd.erp.io.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.RequestToDomainMapper.REQUEST_TO_DOMAIN_MAPPER_INSTANCE;

@RestController
@RequestMapping("/api/admin/")
public class AdminController {

    @PostMapping("/create/student")
    public ResponseEntity<CreateUserResponse> login(@RequestBody StudentCreateRequest studentCreateRequest, HttpServletRequest req){







        AuthResultDomain authResultDomain = authSessionService.loginAndCreateSession(loginRequestDomain.getUsername(), loginRequestDomain.getPassword(),req);
        LoginResponse loginResponse = DOMAIN_TO_RESPONSE_MAPPER_INSTANCE.domainToLoginResponse(authResultDomain);
        return ResponseEntity.ok(loginResponse);
    }
}


//createUser -- student,instructor
//--student (name,roll_no,program_id, yearOfRegistration)


//createCourse
//createSection
//toggleMaintain