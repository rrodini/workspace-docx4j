package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.rodini.contestgen.Environment.*;

class TestEnvironment {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnums() {
		for (Environment env: Environment.values()) {
			if (env != TEST &&
				env != INTEGRATION &&
				env != PRODUCTION) {
				fail();
			}
		}
		
	}

}
