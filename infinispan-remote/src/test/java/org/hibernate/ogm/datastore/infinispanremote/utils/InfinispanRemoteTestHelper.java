/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

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
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.util.concurrent.NotifyingFuture;

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

	private static Cache<?, ?> getEntityCache(SessionFactory sessionFactory, EntityKeyMetadata entityKeyMetadata) {
		final String tableName = entityKeyMetadata.getTable();
		final InfinispanRemoteDatastoreProvider hotrodProvider = getProvider( sessionFactory );
		return (Cache<?, ?>) hotrodProvider.getRemoteCacheManager().getCache( tableName );
	}

	public static InfinispanRemoteDatastoreProvider getProvider(SessionFactory sessionFactory) {
		DatastoreProvider provider = ( (SessionFactoryImplementor) sessionFactory ).getServiceRegistry().getService( DatastoreProvider.class );
		if ( !( InfinispanRemoteDatastoreProvider.class.isInstance( provider ) ) ) {
			throw new RuntimeException( "Not testing with Infinispan Remote, cannot extract underlying cache" );
		}
		return InfinispanRemoteDatastoreProvider.class.cast( provider );
	}

	private static Cache<?, ?> getAssociationCache(SessionFactory sessionFactory, AssociationKeyMetadata associationKeyMetadata) {
		final String tableName = associationKeyMetadata.getTable();
		final InfinispanRemoteDatastoreProvider hotrodProvider = getProvider( sessionFactory );
		return (Cache<?, ?>) hotrodProvider.getRemoteCacheManager().getCache( tableName );
	}

	@Override
	public boolean backendSupportsTransactions() {
		return false;
	}

	@Override
	public void dropSchemaAndDatabase(SessionFactory sessionFactory) {
		final InfinispanRemoteDatastoreProvider datastoreProvider = getProvider( sessionFactory );
		final RemoteCacheManager cacheManager = datastoreProvider.getRemoteCacheManager();
		final Set<String> mappedCacheNames = datastoreProvider.getMappedCacheNames();
		final List<NotifyingFuture<Void>> tasks = new ArrayList<>( mappedCacheNames.size() );
		mappedCacheNames.forEach( cacheName -> {
			tasks.add( cacheManager.getCache( cacheName ).clearAsync() );
		});
		//Now block and wait for all clear operation to be performed:
		tasks.forEach( resetOperation -> {
			try {
				resetOperation.get();
			}
			catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				throw new RuntimeException( ie );
			}
			catch (ExecutionException ee) {
				throw new RuntimeException( ee );
			}
		} );
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
	public long getNumberOfEntities(Session session) {
		//FIXME this approach is effectively counting both entities and associations, mixed,
		//as we only know about defined tables.
		final InfinispanRemoteDatastoreProvider datastoreProvider = getProvider( session.getSessionFactory() );
		final RemoteCacheManager cacheManager = datastoreProvider.getRemoteCacheManager();
		final AtomicLong counter = new AtomicLong();
		datastoreProvider.getMappedCacheNames().forEach( cacheName -> {
			counter.addAndGet( cacheManager.getCache().size() );
		} );
		return counter.get();
	}

}
