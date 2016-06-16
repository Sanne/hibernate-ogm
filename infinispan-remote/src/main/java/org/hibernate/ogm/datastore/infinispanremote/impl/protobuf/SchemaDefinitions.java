/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.AssertionFailure;
import org.hibernate.ogm.datastore.infinispanremote.logging.impl.Log;
import org.hibernate.ogm.datastore.infinispanremote.logging.impl.LoggerFactory;
import org.hibernate.ogm.datastore.infinispanremote.spi.schema.SchemaCapture;
import org.hibernate.ogm.datastore.infinispanremote.spi.schema.SchemaOverride;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

public class SchemaDefinitions {

	private static final Log LOG = LoggerFactory.getLogger();

	private final String packageName;
	private final Map<String,TableDefinition> tableDefinitionsByName = new HashMap<>();

	public SchemaDefinitions(String packageName) {
		this.packageName = packageName;
	}

	// N.B. all messages to the server need to be wrapped in /org/infinispan/protostream/message-wrapping.proto
	// (both the schema definitions and the key/value pairs)
	// This resource is defined in the Protostream jar

	public void deploySchema(String generatedProtobufName, RemoteCache<String, String> protobufCache, SchemaCapture schemaCapture, SchemaOverride schemaOverrideService) {
		final String generatedProtoschema = schemaOverrideService == null ? generateProtoschema() : schemaOverrideService.createProtobufSchema();
		protobufCache.put( generatedProtobufName, generatedProtoschema );
		if ( schemaCapture != null ) {
			schemaCapture.put( generatedProtobufName, generatedProtoschema );
		}
		final String schemaDeployErrors = protobufCache.get( ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX );
		if ( schemaDeployErrors != null ) {
			throw LOG.errorAtSchemaDeploy( generatedProtobufName, schemaDeployErrors );
		}
		else {
			LOG.successfullSchemaDeploy( generatedProtobufName );
		}
	}

	private String generateProtoschema() {
		TypeDeclarationsCollector typesDefCollector = new TypeDeclarationsCollector();
		StringBuilder sb = new StringBuilder( 400 );
		sb.append( "package " ).append( packageName ).append( ";\n" );
		tableDefinitionsByName.forEach( ( k, v ) -> v.collectTypeDeclarations( typesDefCollector ) );
		typesDefCollector.exportProtobufEntries( sb );
		tableDefinitionsByName.forEach( ( k, v ) -> v.exportProtobufEntry( sb ) );
		String fullSchema = sb.toString();
		LOG.generatedSchema( fullSchema );
		return fullSchema;
	}

	public void registerTableDefinition(TableDefinition td) {
		TableDefinition previous = tableDefinitionsByName.put( td.getName(), td );
		if ( previous != null ) {
			throw new AssertionFailure( "There should be no duplicate table definitions" );
		}
	}

}
