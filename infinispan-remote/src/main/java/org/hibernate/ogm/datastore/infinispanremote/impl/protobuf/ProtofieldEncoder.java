/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

@FunctionalInterface
public interface ProtofieldEncoder<T> {

	void encode(CodedOutputStream outProtobuf, T value) throws IOException;

}
