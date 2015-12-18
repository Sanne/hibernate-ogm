/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.Column;

public class TableDefinition {

	private final String name;
	private final List<String> primaryKeys = new ArrayList<String>();

	public TableDefinition(String name) {
		this.name = name;
	}

	public void addPrimaryKeyColumn(Column pkColumn) {
		//FIXME implement me
	}

	public void addValueColumn(Column column) {
		//FIXME implement me
	}

}
