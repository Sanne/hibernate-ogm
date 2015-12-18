/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.model.key.spi.IdSourceKeyMetadata;
import org.infinispan.client.hotrod.RemoteCacheManager;

public class CacheAccessor {

	private final Map<String,IdAccessor> idAccessors;
	private final Map<String,EntityAccessor> entityAccessors;
	private final Map<String,AssociationAccessor> associationAccessors;

	CacheAccessor(Set<EntityKeyMetadata> allEntityKeyMetadata,
			Set<AssociationKeyMetadata> allAssociationKeyMetadata,
			Set<IdSourceKeyMetadata> allIdSourceKeyMetadata,
			RemoteCacheManager hotrodClient) {
		this.idAccessors = buildIdSources( hotrodClient, allIdSourceKeyMetadata );
		this.entityAccessors = buildEntityAccessors( hotrodClient, allEntityKeyMetadata );
		this.associationAccessors = buildAssociationAccessor( hotrodClient, allAssociationKeyMetadata );
	}

	public EntityAccessor getEntityAccessor(EntityKey key) {
		return entityAccessors.get( key.getTable() );
	}

	private static Map<String, AssociationAccessor> buildAssociationAccessor(RemoteCacheManager hotrodClient, Set<AssociationKeyMetadata> allAssociationKeyMetadata) {
		HashMap<String,AssociationAccessor> map = new HashMap<>( allAssociationKeyMetadata.size() );
		for ( AssociationKeyMetadata associationMetadata : allAssociationKeyMetadata ) {
			map.put( associationMetadata.getCollectionRole(), buildAssociationAccessor( associationMetadata, hotrodClient ) );
		}
		return Collections.unmodifiableMap( map );
	}

	private static AssociationAccessor buildAssociationAccessor(AssociationKeyMetadata associationMetadata, RemoteCacheManager hotrodClient) {
		return new AssociationAccessor( associationMetadata, hotrodClient );
	}

	private static Map<String, EntityAccessor> buildEntityAccessors(RemoteCacheManager hotrodClient, Set<EntityKeyMetadata> allEntityKeyMetadata) {
		HashMap<String,EntityAccessor> map = new HashMap<>( allEntityKeyMetadata.size() );
		for ( EntityKeyMetadata entityMetadata : allEntityKeyMetadata ) {
			map.put( entityMetadata.getTable(), buildEntityAccessor( entityMetadata, hotrodClient ) );
		}
		return Collections.unmodifiableMap( map );
	}

	private static EntityAccessor buildEntityAccessor(EntityKeyMetadata entityMetadata, RemoteCacheManager hotrodClient) {
		return new EntityAccessor( entityMetadata, hotrodClient );
	}

	private static Map<String, IdAccessor> buildIdSources(RemoteCacheManager hotrodClient, Set<IdSourceKeyMetadata> allIdSourceKeyMetadata) {
		HashMap<String,IdAccessor> map = new HashMap<>( allIdSourceKeyMetadata.size() );
		for ( IdSourceKeyMetadata idSource : allIdSourceKeyMetadata ) {
			map.put( idSource.getName(), buildIdSourceAccessor( idSource, hotrodClient ) );
		}
		return Collections.unmodifiableMap( map );
	}

	private static IdAccessor buildIdSourceAccessor(IdSourceKeyMetadata idSource, RemoteCacheManager hotrodClient) {
		return new IdAccessor( idSource, hotrodClient );
	}

}
