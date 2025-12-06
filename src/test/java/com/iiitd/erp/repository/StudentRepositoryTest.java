package com.iiitd.erp.repository;

import com.iiitd.erp.persistent.entity.erp_db.Student;
import com.iiitd.erp.persistent.persistence.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class StudentRepositoryTest {

    @Autowired
    StudentRepository repo;

    @Test
    void findByRollNo_returnsStudent() {
        Optional<Student> student = Optional.ofNullable(repo.findByRollNo("2024CS101"));
        assertTrue(student.isPresent());
        assertEquals("Student One", student.get().getStudentName());
    }
}
