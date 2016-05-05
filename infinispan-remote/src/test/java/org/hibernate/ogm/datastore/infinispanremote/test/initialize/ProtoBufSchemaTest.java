/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.test.initialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.ogm.backendtck.associations.collection.unidirectional.Cloud;
import org.hibernate.ogm.backendtck.associations.collection.unidirectional.SnowFlake;
import org.hibernate.ogm.backendtck.simpleentity.Helicopter;
import org.hibernate.ogm.backendtck.simpleentity.Hero;
import org.hibernate.ogm.backendtck.simpleentity.Hypothesis;
import org.hibernate.ogm.backendtck.simpleentity.SuperHero;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.datastore.infinispanremote.InfinispanRemoteProperties;
import org.hibernate.ogm.datastore.infinispanremote.spi.schema.MapSchemaCapture;
import org.hibernate.ogm.utils.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class ProtoBufSchemaTest {

	private static final String DEFAULT_SCHEMA_NAME = "Hibernate_OGM_Generated_schema.proto";

	/**
	 * All protobuf files used for these tests should be nicely organized in a resource directory
	 */
	private static final String RESOURCES_NAME_PREFIX = "protoschema-expectations/";

	/**
	 * Name of UTF-8 Charset
	 */
	private static final String CHARSET_UTF8 = "UTF-8";

	@Test
	public void testingBasicCrudSchemaGeneration() throws IOException {
		assertSchemaEquals( "Hypothesis_Helicopter.protobuf", Hypothesis.class, Helicopter.class );
	}

	@Test
	public void testingSchemaGenerationWithJoins() throws IOException {
		assertSchemaEquals( "Cloud_SnowFlake.protobuf", Cloud.class, SnowFlake.class );
	}

	@Test
	public void testingSchemaGenerationInheritance() throws IOException {
		assertSchemaEquals( "Hero_SuperHero.protobuf", Hero.class, SuperHero.class );
	}

	private void assertSchemaEquals(String resourceName, Class<?>... types) throws IOException {
		MapSchemaCapture schemaCapture = new MapSchemaCapture();
		Map<String, Object> settings = new HashMap<>();
		settings.put( OgmProperties.DATASTORE_PROVIDER, "infinispan_remote" );
		settings.put( InfinispanRemoteProperties.CONFIGURATION_RESOURCE_NAME, "hotrod-client-testingconfiguration.properties" );
		settings.put( InfinispanRemoteProperties.SCHEMA_CAPTURE_SERVICE, schemaCapture );
		final String generatedSchema;
		try ( SessionFactory sessionFactory = TestHelper.getDefaultTestSessionFactory( settings, types ) ) {
			generatedSchema = schemaCapture.asMap().get( DEFAULT_SCHEMA_NAME );
		}
		Assert.assertNotNull( generatedSchema );
		final String expectedProtobufSchema;
		try ( InputStream resourceAsStream = ProtoBufSchemaTest.class.getClassLoader().getResourceAsStream( RESOURCES_NAME_PREFIX + resourceName ) ) {
			Assert.assertNotNull( resourceAsStream );
			StringBuilder buffer = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader( new InputStreamReader( resourceAsStream, CHARSET_UTF8 ) );
			while ( ( line = reader.readLine() ) != null ) {
				buffer.append( line ).append( "\n" );
			}
			expectedProtobufSchema = buffer.toString();
		}
		Assert.assertEquals( expectedProtobufSchema, generatedSchema );
	}

}
