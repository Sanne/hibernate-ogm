/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

public class IntegerProtofieldWriter implements ProtofieldWriter<Integer> {

	private final int fieldLabel;
	private final String name;

	public IntegerProtofieldWriter(int labelCounter, String name) {
		this.fieldLabel = labelCounter;
		this.name = name;
	}

	@Override
	public void writeTo(CodedOutputStream outProtobuf, Integer value) throws IOException {
		outProtobuf.writeInt32( fieldLabel, value );
	}

	@Override
	public void exportProtobufFieldDefinition(StringBuilder sb) {
		sb.append( "\n\trequired int32 " );
		sb.append( name );
		sb.append( " = " );
		sb.append( fieldLabel );
		sb.append( ";" );
	}

}
