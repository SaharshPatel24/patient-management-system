package com.pm.billingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BillingServiceApplicationTests {

	@Test
	void contextLoads() {
		// Test that the Spring Boot application context loads successfully
	}

}
