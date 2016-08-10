/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protostream;

import java.io.IOException;
import java.util.Map;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.descriptors.Descriptor;
import org.infinispan.protostream.descriptors.EnumDescriptor;
import org.infinispan.protostream.descriptors.FileDescriptor;

public final class OgmSerializationContext implements SerializationContext {

	private final MarshallingAdapterPayload valueMarshaller;
	private final MarshallingAdapterId idMarshaller;

	public OgmSerializationContext(MarshallingAdapterPayload valueMarshaller, MarshallingAdapterId idMarshaller) {
		this.valueMarshaller = valueMarshaller;
		this.idMarshaller = idMarshaller;
	}

	@Override
	public Configuration getConfiguration() {
		verifyNotUsed();
		return null;
	}

	@Override
	public void registerProtoFiles(FileDescriptorSource source) throws IOException, DescriptorParserException {
		verifyNotUsed();
	}

	@Override
	public void unregisterProtoFile(String name) {
		verifyNotUsed();
	}

	@Override
	public Map<String, FileDescriptor> getFileDescriptors() {
		verifyNotUsed();
		return null;
	}

	@Override
	public <T> void registerMarshaller(BaseMarshaller<T> marshaller) {
		verifyNotUsed();
	}

	@Override
	public Descriptor getMessageDescriptor(String fullName) {
		verifyNotUsed();
		return null;
	}

	@Override
	public EnumDescriptor getEnumDescriptor(String fullName) {
		verifyNotUsed();
		return null;
	}

	@Override
	public boolean canMarshall(Class clazz) {
		return ProtostreamPayload.class.equals( clazz ) || ProtostreamId.class.equals( clazz );
	}

	@Override
	public boolean canMarshall(String descriptorFullName) {
		verifyNotUsed();
		return false;
	}

	@Override
	public <T> BaseMarshaller<T> getMarshaller(String descriptorFullName) {
		verifyNotUsed();
		return null;
	}

	@Override
	public <T> BaseMarshaller<T> getMarshaller(Class<T> clazz) {
		if ( ProtostreamPayload.class.equals( clazz ) ) {
			return (BaseMarshaller<T>) valueMarshaller;
		}
		else if ( ProtostreamId.class.equals( clazz ) ) {
			return (BaseMarshaller<T>) idMarshaller;
		}
		else {
			throw new IllegalStateException( "getMarshaller() invoked for class " + clazz + " : unsupported type" );
		}
	}

	@Override
	public String getTypeNameById(Integer typeId) {
		verifyNotUsed();
		return null;
	}

	@Override
	public Integer getTypeIdByName(String descriptorFullName) {
		verifyNotUsed();
		return null;
	}

	private void verifyNotUsed() {
		throw new IllegalStateException( "This method should never be called. If you see this error, please report a bug with the full stacktrace." );
	}

}
