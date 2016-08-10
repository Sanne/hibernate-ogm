/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protostream;

import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;


/**
 * The original ProtoStreamMarshaller expects 1:1 mapping between a Java class
 * and a protostream schema. We need flexibility to override this by providing
 * a custom org.infinispan.protostream.SerializationContext, but also we
 * need to extend org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
 * to maintain general encoding compatibility and server side query capabilities.
 * 
 * Note: see the implementation of
 * org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller.getSerializationContext(RemoteCacheManager)
 * to understand why it's important to extend it.
 */
public final class OgmProtoStreamMarshaller extends ProtoStreamMarshaller {

	private static final ThreadLocal<SerializationContext> currentSerializationContext = new ThreadLocal<>();

	public OgmProtoStreamMarshaller() {
	}

	@Override
	public SerializationContext getSerializationContext() {
		return currentSerializationContext.get();
	}

	public static void setCurrentSerializationContext(SerializationContext sc) {
		currentSerializationContext.set( sc );
	}

}
