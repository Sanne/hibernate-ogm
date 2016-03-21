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
import org.hibernate.ogm.type.impl.IntegerType;
import org.hibernate.ogm.type.impl.StringType;
import org.hibernate.ogm.type.spi.GridType;

public class TableDefinition {

	private final String tableName;
	private final List<ProtofieldWriter> keyFields = new ArrayList<ProtofieldWriter>();
	private final List<ProtofieldWriter> valueFields = new ArrayList<ProtofieldWriter>();
	private int uniqueTagAssigningCounter = 0;

	public TableDefinition(String name) {
		this.tableName = name;
	}

	public void addPrimaryKeyColumn(Column pkColumn, GridType gridType) {
		uniqueTagAssigningCounter++;
		String name = pkColumn.getName();
		addMapping( keyFields, name, uniqueTagAssigningCounter, gridType );
	}

	public void addValueColumn(Column column, GridType gridType) {
		uniqueTagAssigningCounter++;
		String name = column.getName();
		addMapping( valueFields, name, uniqueTagAssigningCounter, gridType );
	}

	private void addMapping(List<ProtofieldWriter> fieldset, String name, int labelCounter2, GridType gridType) {
		if ( gridType instanceof StringType ) {
			fieldset.add( new StringProtofieldWriter( uniqueTagAssigningCounter, name ) );
		}
		else if ( gridType instanceof IntegerType ) {
			fieldset.add( new IntegerProtofieldWriter( uniqueTagAssigningCounter, name ) );
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
		keyFields.forEach( (v) -> v.exportProtobufFieldDefinition( sb ) );
		valueFields.forEach( (v) -> v.exportProtobufFieldDefinition( sb ) );
		sb.append( "\n}\n" );
	}

}
