/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protostream;

import org.hibernate.ogm.datastore.infinispanremote.impl.CacheOperation;
import org.hibernate.ogm.datastore.infinispanremote.impl.ProtoStreamMappingAdapter;
import org.hibernate.ogm.dialect.spi.TupleContext;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.spi.Tuple;

public final class ProtoDataMapper implements ProtoStreamMappingAdapter {

	private final OgmSerializationContext serContext;
	private final MainOgmCoDec delegate;

	public ProtoDataMapper(String typeName, MainOgmCoDec delegate) {
		this.delegate = delegate;
		this.serContext = new OgmSerializationContext( new MarshallingAdapterPayload( typeName, delegate ) , new MarshallingAdapterId( typeName, delegate ) );
	}

	@Override
	public ProtostreamPayload createValuePayload(EntityKey key, Tuple tuple, TupleContext tupleContext) {
		return delegate.createValuePayload( key, tuple, tupleContext );
	}

	@Override
	public ProtostreamId createIdPayload(String[] columnNames, Object[] columnValues) {
		return delegate.createIdPayload( columnNames, columnValues );
	}

	@Override
	public <T> T withinCacheEncodingContext(CacheOperation<T> function) {
		try {
			OgmProtoStreamMarshaller.setCurrentSerializationContext( serContext );
			return function.doOnCache( delegate.getLinkedCache() );
		}
		finally {
			OgmProtoStreamMarshaller.setCurrentSerializationContext( null );
		}
	}

}
