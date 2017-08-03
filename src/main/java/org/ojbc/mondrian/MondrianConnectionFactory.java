package org.ojbc.mondrian;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MondrianConnectionFactory {
	
	private final Log log = LogFactory.getLog(MondrianConnectionFactory.class);
	
	static final class MondrianConnectionCollection {
		private String sourceFilePath;
		private Map<String, MondrianConnection> connections;
		public Map<String, MondrianConnection> getConnections() {
			return Collections.unmodifiableMap(connections);
		}
		public String getSourceFilePath() {
			return sourceFilePath;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	static final class MondrianConnection {
		
		private final Log log = LogFactory.getLog(MondrianConnection.class);
		
		@JsonProperty("JdbcDrivers")
		private String jdbcDrivers;
		@JsonProperty("Jdbc")
		private String jdbcConnectionString;
		@JsonProperty("JdbcUser")
		private String jdbcUser;
		@JsonProperty("JdbcPassword")
		private String jdbcPassword;
		@JsonProperty("Description")
		private String description;
		@JsonProperty("Catalog")
		private String rawMondrianSchemaPath;
		private String resolvedMondrianSchemaURL;
		private URL catalogUrl;
		private String catalogContent;
		private String sourceResourcePath;
		
		public String getSourceResourcePath() {
			return sourceResourcePath;
		}
		public String getJdbcDrivers() {
			return jdbcDrivers;
		}
		public String getJdbcConnectionString() {
			return jdbcConnectionString;
		}
		public String getRawMondrianSchemaPath() {
			return rawMondrianSchemaPath;
		}
		public String getResolvedMondrianSchemaURL() {
			return resolvedMondrianSchemaURL;
		}
		public String getDescription() {
			return description;
		}
		public String getJdbcUser() {
			return jdbcUser;
		}
		public String getJdbcPassword() {
			return jdbcPassword;
		}
		
		public void setRawMondrianSchemaPath(String path) {
			if (path != null) {
				catalogUrl = MondrianConnection.class.getResource(path);
				if (catalogUrl == null) {
					try {
						catalogUrl = new URL(path);
					} catch (MalformedURLException e) {
						log.warn("Invalid URL specified for Mondrian schema: " + path);
					}
				}
				if (catalogUrl != null) {
					resolvedMondrianSchemaURL = catalogUrl.toExternalForm();
					try {
						InputStream stream = catalogUrl.openStream();
						catalogContent = IOUtils.toString(stream);
					} catch (IOException e) {
						log.warn("Exception occurred when attempting to read Mondrian schema from URL " + catalogUrl.toExternalForm());
					}
				}
			}
			rawMondrianSchemaPath = path;
		}

		public java.sql.Connection getOlap4jConnection() throws SQLException, ClassNotFoundException {

			Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
			String url = "jdbc:mondrian:";

			Properties props = new Properties();
			setPropertyValue(props, "Jdbc", jdbcConnectionString);
			setPropertyValue(props, "JdbcDrivers", jdbcDrivers);
			setPropertyValue(props, "CatalogContent", catalogContent);
			setPropertyValue(props, "JdbcUser", jdbcUser);
			setPropertyValue(props, "JdbcPassword", jdbcPassword);

			return DriverManager.getConnection(url, props);

		}
		
		public boolean validate() {
			boolean ret = true;
			if (jdbcConnectionString == null) {
				log.warn("JDBC Connection String (specified by json property \"Jdbc\") is null");
				ret = false;
			}
			if (jdbcDrivers == null) {
				log.warn("JDBC Driver (specified by json property \"JdbcDrivers\") is null");
				ret = false;
			}
			if (rawMondrianSchemaPath == null) {
				log.warn("Mondrian Schema/Catalog (specified by json property \"Catalog\") is null");
				ret = false;
			}
			if (catalogContent == null) {
				log.warn("Mondrian Schema content (read from catalog URL) is null");
				ret = false;
			}
			return ret;
		}
		
		private static final Properties setPropertyValue(Properties props, String name, String value) {
			if (value != null) {
				props.setProperty(name, value);
			}
			return props;
		}
		
	}
	
	private List<MondrianConnectionCollection> connectionCollections = new ArrayList<>();
	private Map<String, MondrianConnection> connections = new HashMap<>();
	
	public void init() throws Exception {
		
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:*mondrian-connections.json");
		
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, MondrianConnection>> typeRef = new TypeReference<Map<String, MondrianConnection>>() {};
		
		for (Resource resource : resources) {
			String resourceSourcePath = resource.getFile().getCanonicalPath();
			log.info("Processing connection definition json found at " + resourceSourcePath);
			Map<String, MondrianConnection> connections = mapper.readValue(resource.getInputStream(), typeRef);
			Set<String> invalidConnections = new HashSet<>();
			for (String nm : connections.keySet()) {
				MondrianConnection c = connections.get(nm);
				if (!c.validate()) {
					log.warn("Ignoring connection " + nm + " due to invalid/missing properties (see prior messages for details)");
					invalidConnections.add(nm);
				} else {
					log.info("Adding valid connection " + nm + ": connection string=" + c.getJdbcConnectionString() + ", Mondrian schema path=" + c.getResolvedMondrianSchemaURL());
					c.sourceResourcePath = resourceSourcePath;
				}
			}
			for (String nm : invalidConnections) {
				connections.remove(nm);
			}
			MondrianConnectionCollection collection = new MondrianConnectionCollection();
			collection.connections = connections;
			collection.sourceFilePath = resource.getFile().getCanonicalPath();
			connectionCollections.add(collection);
		}
		
		Collections.reverse(connectionCollections);
		
		for (MondrianConnectionCollection mcc : connectionCollections) {
			Map<String, MondrianConnection> c = mcc.getConnections();
			for (String name : c.keySet()) {
				if (connections.containsKey(name)) {
					MondrianConnection conn = connections.get(name);
					log.warn("Overriding connection " + name + " defined at " + conn.getSourceResourcePath() +
							" with connection defined \"higher\" on the classpath, at " + c.get(name).getSourceResourcePath());
				}
				connections.put(name, c.get(name));
			}
		}
		
	}
	
	List<MondrianConnectionCollection> getConnectionCollections() {
		return Collections.unmodifiableList(connectionCollections);
	}
	
	public Map<String, MondrianConnection> getConnections() {
		return Collections.unmodifiableMap(connections);
	}

}
