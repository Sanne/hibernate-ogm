/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.ogm.datastore.infinispanremote.InfinispanRemoteDialect;
import org.hibernate.ogm.datastore.infinispanremote.configuration.impl.InfinispanRemoteConfiguration;
import org.hibernate.ogm.datastore.infinispanremote.impl.protobuf.SchemaDefinitions;
import org.hibernate.ogm.datastore.infinispanremote.logging.impl.Log;
import org.hibernate.ogm.datastore.infinispanremote.logging.impl.LoggerFactory;
import org.hibernate.ogm.datastore.infinispanremote.spi.schema.SchemaCapture;
import org.hibernate.ogm.datastore.infinispanremote.spi.schema.SchemaOverride;
import org.hibernate.ogm.datastore.spi.BaseDatastoreProvider;
import org.hibernate.ogm.datastore.spi.SchemaDefiner;
import org.hibernate.ogm.dialect.spi.GridDialect;
import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.model.key.spi.IdSourceKeyMetadata;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Startable;
import org.hibernate.service.spi.Stoppable;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

/**
 * @author Sanne Grinovero
 */
public class InfinispanRemoteDatastoreProvider extends BaseDatastoreProvider
				implements Startable, Stoppable, Configurable, ServiceRegistryAwareService {

	private static final Log LOG = LoggerFactory.getLogger();

	// Only available during configuration
	private InfinispanRemoteConfiguration config;

	// The Hot Rod client; maintains TCP connections to the datagrid.
	private RemoteCacheManager hotrodClient;

	private CacheAccessor ca;

	//Useful to allow people to dump the generated schema,
	//we use it to capture the schema in tests too.
	private SchemaCapture schemaCapture;

	private ServiceRegistryImplementor serviceRegistry;

	private SchemaOverride schemaOverrideService;

	private Set<String> mappedCacheNames;

	//For each cache we have a schema and a set of encoders/decoders to the generated protobuf schema
	private Map<String,ProtoStreamMappingAdapter> perCacheSchemaMappers;

	@Override
	public Class<? extends GridDialect> getDefaultDialect() {
		return InfinispanRemoteDialect.class;
	}

	@Override
	public void start() {
		hotrodClient = HotRodClientBuilder.builder().withConfiguration( config ).build();
		hotrodClient.start();
		config = null; //no longer needed
	}

	public RemoteCacheManager getRemoteCacheManager() {
		return hotrodClient;
	}

	@Override
	public void stop() {
		hotrodClient.stop();
	}

	@Override
	public void configure(Map configurationValues) {
		this.config = new InfinispanRemoteConfiguration();
		this.config.initConfiguration( configurationValues, serviceRegistry );
		this.schemaCapture = config.getSchemaCaptureService();
		this.schemaOverrideService = config.getSchemaOverrideService();
	}

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public Class<? extends SchemaDefiner> getSchemaDefinerType() {
		return ProtobufSchemaInitializer.class;
	}

	public void provideEntityMetadata(Set<EntityKeyMetadata> allEntityKeyMetadata, Set<AssociationKeyMetadata> allAssociationKeyMetadata,
			Set<IdSourceKeyMetadata> allIdSourceKeyMetadata) {
		this.ca = new CacheAccessor( allEntityKeyMetadata, allAssociationKeyMetadata, allIdSourceKeyMetadata, hotrodClient );
	}

	public CacheAccessor getCacheAccessor() {
		return ca;
	}

	public void registerSchemaDefinitions(SchemaDefinitions sd) {
		RemoteCache<String,String> protobufCache = getProtobufCache();
		//FIXME make this name configurable & give it a sensible default:
		final String generatedProtobufName = "Hibernate_OGM_Generated_schema.proto";
		sd.deploySchema( generatedProtobufName, protobufCache, schemaCapture, schemaOverrideService );
		setMappedCacheNames( sd.getTableNames() );
		startCaches();
		perCacheSchemaMappers = Collections.unmodifiableMap( sd.generateSchemaMappingAdapters( hotrodClient ) );
	}

	private void startCaches() {
		//TODO wrap with some nice validation?
		mappedCacheNames.forEach( cacheName -> hotrodClient.getCache( cacheName ) );
	}

	private void setMappedCacheNames(Set<String> tableNames) {
		this.mappedCacheNames = Collections.unmodifiableSet( new HashSet( tableNames ) );
	}

	private RemoteCache<String, String> getProtobufCache() {
		return getRemoteCacheManager().getCache( ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME );
	}

	@Override
	public boolean allowsTransactionEmulation() {
		// Hot Rod doesn't support "true" transaction yet
		return true;
	}

	public String getProtobufPackageName() {
		return "HibernateOGMGenerated";
	}

	public Set<String> getMappedCacheNames() {
		return mappedCacheNames;
	}

	public ProtoStreamMappingAdapter getDataMapperForCache(String cacheName) {
		return perCacheSchemaMappers.get( cacheName );
	}

}
