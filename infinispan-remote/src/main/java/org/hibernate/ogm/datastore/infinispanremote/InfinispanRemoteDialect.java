/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote;

import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.ogm.datastore.infinispanremote.impl.ProtoStreamMappingAdapter;
import org.hibernate.ogm.datastore.infinispanremote.impl.EntityAccessor;
import org.hibernate.ogm.datastore.infinispanremote.impl.InfinispanRemoteDatastoreProvider;
import org.hibernate.ogm.datastore.infinispanremote.impl.protostream.ProtostreamId;
import org.hibernate.ogm.datastore.infinispanremote.impl.protostream.ProtostreamPayload;
import org.hibernate.ogm.dialect.spi.AssociationContext;
import org.hibernate.ogm.dialect.spi.AssociationTypeContext;
import org.hibernate.ogm.dialect.spi.BaseGridDialect;
import org.hibernate.ogm.dialect.spi.DuplicateInsertPreventionStrategy;
import org.hibernate.ogm.dialect.spi.ModelConsumer;
import org.hibernate.ogm.dialect.spi.NextValueRequest;
import org.hibernate.ogm.dialect.spi.TupleContext;
import org.hibernate.ogm.model.key.spi.AssociationKey;
import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.model.spi.Association;
import org.hibernate.ogm.model.spi.Tuple;
import org.infinispan.client.hotrod.RemoteCacheManager;

public class InfinispanRemoteDialect<EK,AK,ISK> extends BaseGridDialect {

	//FIXME temporary surrogate counter to focus on other tasks first
	private static final AtomicLong al = new AtomicLong();

	private final InfinispanRemoteDatastoreProvider provider;

	private final RemoteCacheManager hr;

	public InfinispanRemoteDialect(InfinispanRemoteDatastoreProvider provider) {
		this.provider = provider;
		this.hr = provider.getRemoteCacheManager();
	}

	@Override
	public Tuple getTuple(EntityKey key, TupleContext tupleContext) {
		EntityAccessor entityAccessor = provider.getCacheAccessor().getEntityAccessor( key );
		entityAccessor.loadEntity( key );
		//TODO
		return null;
	}

	@Override
	public Tuple createTuple(EntityKey key, TupleContext tupleContext) {
		return new Tuple();
	}

	@Override
	public void insertOrUpdateTuple(EntityKey key, Tuple tuple, TupleContext tupleContext) {
		final String cacheName = key.getTable();
		ProtoStreamMappingAdapter mapper = provider.getDataMapperForCache( cacheName );
		ProtostreamPayload valuePayload = mapper.createValuePayload( key, tuple, tupleContext );
		ProtostreamId idBuffer = mapper.createIdPayload( key.getColumnNames(), key.getColumnValues() );
		mapper.withinCacheEncodingContext( c -> c.put( idBuffer, valuePayload ) );
	}

	@Override
	public void removeTuple(EntityKey key, TupleContext tupleContext) {
		//TODO
	}

	@Override
	public Association getAssociation(AssociationKey key, AssociationContext associationContext) {
		//TODO
		return null;
	}

	@Override
	public Association createAssociation(AssociationKey key, AssociationContext associationContext) {
		//TODO
		return null;
	}

	@Override
	public void insertOrUpdateAssociation(AssociationKey key, Association association, AssociationContext associationContext) {
		//TODO
	}

	@Override
	public void removeAssociation(AssociationKey key, AssociationContext associationContext) {
		//TODO
	}

	@Override
	public boolean isStoredInEntityStructure(AssociationKeyMetadata associationKeyMetadata, AssociationTypeContext associationTypeContext) {
		return false;
	}

	@Override
	public Number nextValue(NextValueRequest request) {
		//TODO
		return al.incrementAndGet();
	}

	@Override
	public void forEachTuple(ModelConsumer consumer, TupleContext tupleContext, EntityKeyMetadata entityKeyMetadata) {
		// TODO Auto-generated method stub
	}


	@Override
	public DuplicateInsertPreventionStrategy getDuplicateInsertPreventionStrategy(EntityKeyMetadata entityKeyMetadata) {
		//We can implement duplicate insert detection by this by using Infinispan's putIfAbsent
		//support and verifying the return on any insert
		//TODO Not implemented yet as Hot Rod's support for atomic operations is complex, so default to the naive impl:
		return DuplicateInsertPreventionStrategy.LOOK_UP;
	}

}
