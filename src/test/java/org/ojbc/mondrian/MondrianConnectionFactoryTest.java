package org.ojbc.mondrian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.MondrianConnectionFactory.MondrianConnection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MondrianConnectionFactoryTest {
	
	private MondrianConnectionFactory factory;
	
	@BeforeEach
	public void setUp() throws Exception {
		log.debug("setUp");
		factory = new MondrianConnectionFactory();
		factory.init();
	}
	
	@Test
	public void testClasspathScan() throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:*mondrian-connections.json");
		assertEquals(2, resources.length);
		assertEquals("test-mondrian-connections.json", resources[0].getFilename());
		assertEquals("mondrian-connections.json", resources[1].getFilename());
	}
	
	@Test
	public void testMondrianConnectionFactory() throws Exception {
		
		List<MondrianConnectionFactory.MondrianConnectionCollection> connectionCollections = factory.getConnectionCollections();
		assertEquals(2, connectionCollections.size());
		
		MondrianConnectionFactory.MondrianConnectionCollection connectionCollection = connectionCollections.get(0);
		assertTrue(connectionCollection.getSourceFilePath().endsWith("/mondrian-connections.json"));
		assertFalse(connectionCollection.getSourceFilePath().endsWith("/test-mondrian-connections.json"));
		Map<String, MondrianConnectionFactory.MondrianConnection> connections = connectionCollection.getConnections();
		assertEquals(2, connections.size());
		assertTrue(connections.keySet().contains("test"));
		
		connectionCollection = connectionCollections.get(1);
		assertFalse(connectionCollection.getSourceFilePath().endsWith("/mondrian-connections.json"));
		assertTrue(connectionCollection.getSourceFilePath().endsWith("/test-mondrian-connections.json"));
		connections = connectionCollection.getConnections();
		assertEquals(1, connections.size());
		assertEquals(Collections.singleton("test"), connections.keySet());
		
		connections = factory.getConnections();
		assertEquals(2, connections.size());
		assertTrue(connections.keySet().contains("test"));
		MondrianConnection testConnection = connections.get("test");
		assertEquals("Test version of test connection", testConnection.getDescription());
		
		// expect no exception here...
		testConnection.getOlap4jConnection();
		
	}
	
	@Test
	@Disabled
	public void testJsonSerialization() throws Exception {
		// This test isn't really a test, just a convenient way to dump out some json when needed
		Map<String, MondrianConnectionFactory.MondrianConnection> connections = factory.getConnections();
		String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(connections);
		log.info(json);
	}
	
	@Test
	public void testFoodMartConnection() throws Exception {
		
		Map<String, MondrianConnectionFactory.MondrianConnection> connections = factory.getConnections();
		MondrianConnection foodmartConnection = connections.get("foodmart");
		
		// expect no exception here...
		foodmartConnection.getOlap4jConnection();
		
	}

}
