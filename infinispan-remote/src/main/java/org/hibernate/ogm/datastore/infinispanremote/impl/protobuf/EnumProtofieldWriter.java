/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.io.IOException;

import com.google.protobuf.CodedOutputStream;

/**
 * See also: https://developers.google.com/protocol-buffers/docs/proto#enum
 * @author Sanne Grinovero
 */
public class EnumProtofieldWriter implements ProtofieldWriter<Enum> {

	private final int fieldLabel;
	private final String name;
	private final Class<? extends Enum> type;
	private final Enum[] enumConstants;

	public EnumProtofieldWriter(int labelCounter, String name, Class<? extends Enum> type) {
		this.fieldLabel = labelCounter;
		this.name = name;
		this.enumConstants = type.getEnumConstants();
		this.type = type;
	}

	@Override
	public void writeTo(CodedOutputStream outProtobuf, Enum value) throws IOException {
		outProtobuf.writeEnum( fieldLabel, value.ordinal() );
	}

	@Override
	public void collectTypeDefinitions(TypeDeclarationsCollector typesDefCollector) {
		typesDefCollector.createTypeDefinition( new EnumTypeDefinition( type ) );
	};

	@Override
	public void exportProtobufFieldDefinition(StringBuilder sb) {
		sb.append( "\n\trequired " );
		sb.append( type.getSimpleName() );
		sb.append( " " );
		sb.append( name );
		sb.append( " = " );
		sb.append( fieldLabel );
		sb.append( ";" );
	}

	private static final class EnumTypeDefinition implements TypeDefinition {

		private final Class<? extends Enum> type;

		public EnumTypeDefinition(Class<? extends Enum> type) {
			if ( type == null ) {
				throw new NullPointerException( "The 'type' parameter shall not be null" );
			}
			this.type = type;
		}

		@Override
		public void exportProtobufTypeDefinition(StringBuilder sb) {
			Enum[] enumConstants = type.getEnumConstants();
			sb.append( "\nenum " );
			sb.append( type.getSimpleName() );
			sb.append( " {" );
			for ( int i = 0; i < enumConstants.length; i++ ) {
				sb.append( "\n\t" );
				sb.append( enumConstants[i].name() );
				sb.append( " = " );
				sb.append( i );
				sb.append( ";" );
			}
			sb.append( "\n}\n" );
		}

		@Override
		public String getTypeName() {
			return type.getSimpleName();
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			else if ( obj == null ) {
				return false;
			}
			else if ( EnumTypeDefinition.class != obj.getClass() ) {
				return false;
			}
			else {
				EnumTypeDefinition other = (EnumTypeDefinition) obj;
				return type.equals( other.type );
			}
		}

	}

}
