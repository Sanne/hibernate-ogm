/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

public abstract class BaseProtofieldWriter<T> implements ProtofieldWriter<T> {

	protected final int tag;
	protected final String name;
	protected final boolean nullable;
	protected final ProtofieldEncoder<T> writingFunction;

	public BaseProtofieldWriter(int fieldLabel, String fieldName, boolean nullable,
			ProtofieldEncoder<T> writingFunction) {
		this.tag = fieldLabel;
		this.name = fieldName;
		this.nullable = nullable;
		this.writingFunction = NullableProtofieldEncoder.makeNullableFieldEncoder( writingFunction, nullable );
	}

	@Override
	public void writeTo(CodedOutputStream outProtobuf, T value) throws IOException {
		writingFunction.encode( outProtobuf, value );
	}

	@Override
	public void exportProtobufFieldDefinition(StringBuilder sb) {
		if ( nullable ) {
			sb.append( "\n\toptional " );
		}
		else {
			sb.append( "\n\trequired " );
		}
		sb.append( getProtobufTypeName() )
			.append( " " )
			.append( name )
			.append( " = " )
			.append( tag )
			.append( ";" );
	}

	protected abstract String getProtobufTypeName();

}
