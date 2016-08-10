/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protostream;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public final class MarshallingAdapterPayload implements MessageMarshaller<ProtostreamPayload> {

	private final String typeName;
	private final MainOgmCoDec delegate;

	public MarshallingAdapterPayload(String typeName, MainOgmCoDec delegate) {
		this.typeName = typeName;
		this.delegate = delegate;
	}

	@Override
	public Class<ProtostreamPayload> getJavaClass() {
		return ProtostreamPayload.class;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public ProtostreamPayload readFrom(org.infinispan.protostream.MessageMarshaller.ProtoStreamReader reader) throws IOException {
		return delegate.readPayloadFrom( reader );
	}

	@Override
	public void writeTo(org.infinispan.protostream.MessageMarshaller.ProtoStreamWriter writer, ProtostreamPayload payload) throws IOException {
		delegate.writePayloadTo( writer, payload );
	}

}
