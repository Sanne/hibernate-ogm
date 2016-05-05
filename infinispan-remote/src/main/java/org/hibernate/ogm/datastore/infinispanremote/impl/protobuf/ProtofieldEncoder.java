package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

@FunctionalInterface
public interface ProtofieldEncoder<T> {

	void encode(CodedOutputStream outProtobuf, T value) throws IOException;

}
