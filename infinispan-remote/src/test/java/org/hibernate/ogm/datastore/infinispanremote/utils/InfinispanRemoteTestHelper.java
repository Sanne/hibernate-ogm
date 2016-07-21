/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.utils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.ogm.datastore.document.options.AssociationStorageType;
import org.hibernate.ogm.datastore.infinispanremote.InfinispanRemoteDataStoreConfiguration;
import org.hibernate.ogm.datastore.infinispanremote.InfinispanRemoteDialect;
import org.hibernate.ogm.datastore.infinispanremote.impl.InfinispanRemoteDatastoreProvider;
import org.hibernate.ogm.datastore.spi.DatastoreConfiguration;
import org.hibernate.ogm.datastore.spi.DatastoreProvider;
import org.hibernate.ogm.dialect.spi.GridDialect;
import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.persister.impl.OgmCollectionPersister;
import org.hibernate.ogm.persister.impl.OgmEntityPersister;
import org.hibernate.ogm.utils.GridDialectTestHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.infinispan.Cache;

/**
 * @author Sanne Grinovero (C) 2015 Red Hat Inc.
 */
public class InfinispanRemoteTestHelper implements GridDialectTestHelper {

	@Override
	public long getNumberOfEntities(SessionFactory sessionFactory) {
		int entityCount = 0;
		Set<Cache<?, ?>> processedCaches = Collections.newSetFromMap( new IdentityHashMap<Cache<?, ?>, Boolean>() );

		for ( EntityPersister entityPersister : ( (SessionFactoryImplementor) sessionFactory ).getEntityPersisters().values() ) {
			Cache<?, ?> entityCache = getEntityCache( sessionFactory, ( (OgmEntityPersister) entityPersister ).getEntityKeyMetadata() );
			if ( !processedCaches.contains( entityCache ) ) {
				entityCount += entityCache.size();
				processedCaches.add( entityCache );
			}
		}

		return entityCount;
	}

	@Override
	public long getNumberOfAssociations(SessionFactory sessionFactory) {
		int associationCount = 0;
		Set<Cache<?, ?>> processedCaches = Collections.newSetFromMap( new IdentityHashMap<Cache<?, ?>, Boolean>() );

		for ( CollectionPersister collectionPersister : ( (SessionFactoryImplementor) sessionFactory ).getCollectionPersisters().values() ) {
			Cache<?, ?> associationCache = getAssociationCache( sessionFactory, ( (OgmCollectionPersister) collectionPersister ).getAssociationKeyMetadata() );
			if ( !processedCaches.contains( associationCache ) ) {
				associationCount += associationCache.size();
				processedCaches.add( associationCache );
			}
		}

		return associationCount;
	}

	private static Cache<?, Map<String, Object>> getEntityCache(SessionFactory sessionFactory, EntityKeyMetadata entityKeyMetadata) {
		//TODO
		return null;
	}

	public static InfinispanRemoteDatastoreProvider getProvider(SessionFactory sessionFactory) {
		DatastoreProvider provider = ( (SessionFactoryImplementor) sessionFactory ).getServiceRegistry().getService( DatastoreProvider.class );
		if ( !( InfinispanRemoteDatastoreProvider.class.isInstance( provider ) ) ) {
			throw new RuntimeException( "Not testing with Infinispan Remote, cannot extract underlying cache" );
		}
		return InfinispanRemoteDatastoreProvider.class.cast( provider );
	}

	private static Cache<?, ?> getAssociationCache(SessionFactory sessionFactory, AssociationKeyMetadata associationKeyMetadata) {
		//TODO
		return null;
	}

	@Override
	public boolean backendSupportsTransactions() {
		return false;
	}

	@Override
	public void dropSchemaAndDatabase(SessionFactory sessionFactory) {
		// TODO
	}

	@Override
	public Map<String, String> getEnvironmentProperties() {
		return null;
	}

	@Override
	public long getNumberOfAssociations(SessionFactory sessionFactory, AssociationStorageType type) {
		throw new UnsupportedOperationException( "This datastore does not support different association storage strategies." );
	}

	@Override
	public GridDialect getGridDialect(DatastoreProvider datastoreProvider) {
		return new InfinispanRemoteDialect( (InfinispanRemoteDatastoreProvider) datastoreProvider );
	}

	@Override
	public Class<? extends DatastoreConfiguration<?>> getDatastoreConfigurationType() {
		return InfinispanRemoteDataStoreConfiguration.class;
	}

	@Override
	public Map<String, Object> extractEntityTuple(Session arg0, EntityKey arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getNumberOfAssociations(Session arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumberOfEntities(Session arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
