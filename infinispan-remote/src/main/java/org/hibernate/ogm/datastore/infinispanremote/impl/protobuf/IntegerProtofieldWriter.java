/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import com.google.protobuf.CodedOutputStream;

public final class IntegerProtofieldWriter extends BaseProtofieldWriter<Integer> implements ProtofieldWriter<Integer> {

	public IntegerProtofieldWriter(int fieldNumber, String name, boolean nullable) {
		super(fieldNumber, name, nullable,
				(CodedOutputStream outProtobuf, Integer value) -> outProtobuf.writeInt32( fieldNumber, value )
				);
	}

	@Override
	protected String getProtobufTypeName() {
		return "int32";
	}

}
