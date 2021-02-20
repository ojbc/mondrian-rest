package org.ojbc.mondrian.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.SchemaWrapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "preCacheMetadata=true" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PreCachedMetadataTest extends AbstractMondrianRestControllerTest {
	
	@LocalServerPort
	private String port;
	
	@Test
	public void testCachedMetadata() throws Exception {
		ResponseEntity<SchemaWrapper> response = restTemplate.getForEntity(new URI("http://localhost:" + port + "/getMetadata?connectionName=test"), SchemaWrapper.class);
		List<String> responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertNotNull(responseHeaders);
		responseHeaders = response.getHeaders().get("mondrian-rest-cached-result");
		assertEquals(1, responseHeaders.size());
		assertEquals("true", responseHeaders.get(0));
	}
	
}
