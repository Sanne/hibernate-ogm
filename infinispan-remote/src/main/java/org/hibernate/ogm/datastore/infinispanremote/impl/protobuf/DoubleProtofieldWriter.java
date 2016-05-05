/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl.protobuf;

import com.google.protobuf.CodedOutputStream;


public class DoubleProtofieldWriter extends BaseProtofieldWriter<Double> implements ProtofieldWriter<Double> {

	public DoubleProtofieldWriter(int tag, String name, boolean nullable) {
		super(tag, name, nullable,
				(CodedOutputStream outProtobuf, Double value) -> outProtobuf.writeDouble( tag, value )
				);
	}

	@Override
	protected String getProtobufTypeName() {
		return "double";
	}

}
