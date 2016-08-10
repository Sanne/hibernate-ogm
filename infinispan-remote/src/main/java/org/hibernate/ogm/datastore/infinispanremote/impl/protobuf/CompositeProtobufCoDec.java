/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.util.List;

import org.hibernate.ogm.datastore.infinispanremote.impl.protostream.MainOgmCoDec;
import org.hibernate.ogm.datastore.infinispanremote.impl.protostream.ProtostreamId;
import org.hibernate.ogm.datastore.infinispanremote.impl.protostream.ProtostreamPayload;
import org.hibernate.ogm.dialect.spi.TupleContext;
import org.hibernate.ogm.model.key.spi.EntityKey;
import org.hibernate.ogm.model.spi.Tuple;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.protostream.MessageMarshaller.ProtoStreamReader;
import org.infinispan.protostream.MessageMarshaller.ProtoStreamWriter;


public class CompositeProtobufCoDec implements MainOgmCoDec {

	private final String tableName;
	private final RemoteCache remoteCache;

	private List<ProtofieldWriter> keyFields;
	private List<ProtofieldWriter> valueFields;

	public CompositeProtobufCoDec(String tableName, List<ProtofieldWriter> keyFields, List<ProtofieldWriter> valueFields, RemoteCache remoteCache) {
		this.tableName = tableName;
		this.remoteCache = remoteCache;
		this.keyFields = keyFields;
		this.valueFields = valueFields;
	}

	@Override
	public RemoteCache getLinkedCache() {
		return remoteCache;
	}

	@Override
	public ProtostreamId readProtostreamId(ProtoStreamReader reader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeIdTo(ProtoStreamWriter writer, ProtostreamId id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProtostreamPayload readPayloadFrom(ProtoStreamReader reader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writePayloadTo(ProtoStreamWriter writer, ProtostreamPayload payload) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProtostreamId createIdPayload(String[] columnNames, Object[] columnValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProtostreamPayload createValuePayload(EntityKey key, Tuple tuple, TupleContext tupleContext) {
		// TODO Auto-generated method stub
		return null;
	}

}
