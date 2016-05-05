/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.AssertionFailure;
import org.hibernate.mapping.Column;
import org.hibernate.ogm.type.impl.DoubleType;
import org.hibernate.ogm.type.impl.EnumType;
import org.hibernate.ogm.type.impl.IntegerType;
import org.hibernate.ogm.type.impl.LongType;
import org.hibernate.ogm.type.impl.StringType;
import org.hibernate.ogm.type.spi.GridType;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;

public class TableDefinition {

	private final String tableName;
	private final List<ProtofieldWriter> keyFields = new ArrayList<ProtofieldWriter>();
	private final List<ProtofieldWriter> valueFields = new ArrayList<ProtofieldWriter>();
	private int uniqueTagAssigningCounter = 0;

	public TableDefinition(String name) {
		this.tableName = name;
	}

	public void addPrimaryKeyColumn(Column pkColumn, GridType gridType, Type type, Type ormType) {
		uniqueTagAssigningCounter++;
		String name = pkColumn.getName();
		addMapping( keyFields, name, uniqueTagAssigningCounter, gridType, ormType, false );
	}

	public void addValueColumn(Column column, GridType gridType, Type ormType) {
		uniqueTagAssigningCounter++;
		final String name = column.getName();
		final boolean nullable = column.isNullable();
		addMapping( valueFields, name, uniqueTagAssigningCounter, gridType, ormType, nullable );
	}

	private void addMapping(List<ProtofieldWriter> fieldset, String name, int labelCounter, GridType gridType, Type ormType, boolean nullable) {
		if ( gridType instanceof StringType ) {
			fieldset.add( new StringProtofieldWriter( uniqueTagAssigningCounter, name, nullable ) );
		}
		else if ( gridType instanceof IntegerType ) {
			fieldset.add( new IntegerProtofieldWriter( uniqueTagAssigningCounter, name, nullable ) );
		}
		else if ( gridType instanceof LongType ) {
			fieldset.add( new LongProtofieldWriter( uniqueTagAssigningCounter, name, nullable ) );
		}
		else if ( gridType instanceof DoubleType ) {
			fieldset.add( new DoubleProtofieldWriter( uniqueTagAssigningCounter, name, nullable ) );
		}
		else if ( gridType instanceof EnumType ) {
			EnumType etype = (EnumType) gridType;
			if ( ormType instanceof CustomType ) {
				CustomType customOrmType = (CustomType) ormType;
				UserType userType = customOrmType.getUserType();
				org.hibernate.type.EnumType enumtype = (org.hibernate.type.EnumType) userType;
				Class returnedClass = enumtype.returnedClass();
				fieldset.add( new EnumProtofieldWriter( uniqueTagAssigningCounter, name, returnedClass ) );
			}
			else {
				throw new AssertionFailure( "Type not implemented yet! " );
			}
		}
		else {
			throw new AssertionFailure( "Type not implemented yet! " + gridType.getName() );
		}
	}

	public String getName() {
		return tableName;
	}

	public void exportProtobufEntry(StringBuilder sb) {
		sb.append( "\nmessage " ).append( tableName ).append( " {" );
		keyFields.forEach( ( v ) -> v.exportProtobufFieldDefinition( sb ) );
		valueFields.forEach( ( v ) -> v.exportProtobufFieldDefinition( sb ) );
		sb.append( "\n}\n" );
	}

	void collectTypeDeclarations(TypeDeclarationsCollector typesDefCollector) {
		keyFields.forEach( ( v ) -> v.collectTypeDefinitions( typesDefCollector ) );
		valueFields.forEach( ( v ) -> v.collectTypeDefinitions( typesDefCollector ) );
	}

}
