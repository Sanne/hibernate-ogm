/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote;

import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.ogm.datastore.infinispanremote.impl.EntityAccessor;
import org.hibernate.ogm.datastore.infinispanremote.impl.InfinispanRemoteDatastoreProvider;
import org.hibernate.ogm.dialect.spi.AssociationContext;
import org.hibernate.ogm.dialect.spi.AssociationTypeContext;
import org.hibernate.ogm.dialect.spi.BaseGridDialect;
import org.hibernate.ogm.dialect.spi.ModelConsumer;
import org.hibernate.ogm.dialect.spi.NextValueRequest;
import org.hibernate.ogm.dialect.spi.TupleContext;
import org.hibernate.ogm.model.key.spi.AssociationKey;
import org.hibernate.ogm.model.key.spi.AssociationKeyMetadata;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.key.spi.EntityKeyMetadata;
import org.hibernate.ogm.model.spi.Association;
import org.hibernate.ogm.model.spi.Tuple;

public class InfinispanRemoteDialect<EK,AK,ISK> extends BaseGridDialect {

	//FIXME temporary surrogate counter to focus on other tasks first
	private static final AtomicLong al = new AtomicLong();

	private final InfinispanRemoteDatastoreProvider provider;

	public InfinispanRemoteDialect(InfinispanRemoteDatastoreProvider provider) {
		this.provider = provider;
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
		//TODO
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

}
