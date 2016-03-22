/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

public class StringProtofieldWriter implements ProtofieldWriter<String> {

	private final int fieldLabel;
	private final String name;

	public StringProtofieldWriter(int labelCounter, String name) {
		this.fieldLabel = labelCounter;
		this.name = name;
	}

	@Override
	public void writeTo(CodedOutputStream outProtobuf, String value) throws IOException {
		outProtobuf.writeString( fieldLabel, value );
	}

	@Override
	public void exportProtobufFieldDefinition(StringBuilder sb) {
		sb.append( "\n\trequired string " );
		sb.append( name );
		sb.append( " = " );
		sb.append( fieldLabel );
		sb.append( ";" );
	}

}
