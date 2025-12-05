package com.iiitd.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UniversityErpApplicationTests {

    @Test
    void contextLoads() {
        // verifies the app context starts with the test profile (H2)
    }
}
