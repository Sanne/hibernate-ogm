/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl;

import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.infinispan.client.hotrod.RemoteCacheManager;

public class EntityAccessor {

	private final EntityKeyMetadata entityMetadata;
	private final RemoteCacheManager hotrodClient;

	public EntityAccessor(EntityKeyMetadata entityMetadata, RemoteCacheManager hotrodClient) {
		this.entityMetadata = entityMetadata;
		this.hotrodClient = hotrodClient;
	}

	public void loadEntity(EntityKey key) {
		//FIXME implement me
	}

}
