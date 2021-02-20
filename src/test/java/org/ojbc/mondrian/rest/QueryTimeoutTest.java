package org.ojbc.mondrian.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "queryTimeout=1" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QueryTimeoutTest extends AbstractMondrianRestControllerTest {
	
	@LocalServerPort
	private String port;
	
	@Test
	@Disabled // because we can't guarantee the query will always timeout; this is included more to document the behavior
	public void testQueryTimeout() throws Exception {
		
		HttpEntity<String> requestEntity = buildQueryRequestEntity("foodmart", "SELECT NON EMPTY CrossJoin(Hierarchize({{[Product].[Products].[Product Family].Members}, {[Product].[Products].[Product Department].Members}, {[Product].[Products].[Product Category].Members}, {[Product].[Products].[Product Subcategory].Members}}), Hierarchize({{[Store].[Stores].[Store Country].Members}, {[Store].[Stores].[Store State].Members}, {[Store].[Stores].[Store City].Members}})) ON ROWS, NON EMPTY {[Measures].[Units Ordered]} ON COLUMNS FROM [Warehouse]");
		
		//ResponseEntity<String> response = restTemplate.postForEntity(new URI("http://localhost:" + port + "/query"), requestEntity, String.class);
		//log.info(response.getBody());
		
		ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<Map<String, String>>() {};
		
		ResponseEntity<Map<String, String>> errorResponse = restTemplate.exchange(new URI("http://localhost:" + port + "/query"), HttpMethod.POST, requestEntity, responseType);
		assertEquals(500, errorResponse.getStatusCode().value());
				
		Map<String, String> errorMap = errorResponse.getBody();
		assertTrue(errorMap.get("reason").matches(".+Query timeout of.+reached"));


	}

}
