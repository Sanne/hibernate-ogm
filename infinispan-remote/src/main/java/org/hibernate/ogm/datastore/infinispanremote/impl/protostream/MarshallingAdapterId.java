/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protostream;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

public final class MarshallingAdapterId implements MessageMarshaller<ProtostreamId> {

	private final String typeName;
	private final MainOgmCoDec delegate;

	public MarshallingAdapterId(String typeName, MainOgmCoDec delegate) {
		this.typeName = typeName;
		this.delegate = delegate;
	}

	@Override
	public Class<ProtostreamId> getJavaClass() {
		return ProtostreamId.class;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public ProtostreamId readFrom(org.infinispan.protostream.MessageMarshaller.ProtoStreamReader reader) throws IOException {
		return delegate.readProtostreamId( reader );
	}

	@Override
	public void writeTo(org.infinispan.protostream.MessageMarshaller.ProtoStreamWriter writer, ProtostreamId id) throws IOException {
		delegate.writeIdTo( writer, id );
	}

}
