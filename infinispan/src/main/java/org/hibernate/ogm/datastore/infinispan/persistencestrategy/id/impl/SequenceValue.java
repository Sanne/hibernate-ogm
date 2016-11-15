/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispan.persistencestrategy.id.impl;

import java.util.UUID;

/**
 * Simply storing a Long object would not allow us to fully control
 * storage format and equality.
 * In particular we need to consider two instances of SequenceValue
 * which wrap the same value as different, unless they are the same
 * instance across the cluster, to avoid some pitfalls of the
 * Infinispan API:
 *  - https://issues.jboss.org/browse/ISPN-4286
 *  - https://issues.jboss.org/browse/ISPN-3918
 * In particular, a consequence of these open issues is that two concurrent
 * operations invoking Cache#replace will both return true if they
 * are writing the same value.
 *
 * @author Sanne Grinovero
 */
public final class SequenceValue {

	private final long wrappedValue;
	private final UUID unique;

	public SequenceValue(long value) {
		this.wrappedValue = value;
		this.unique = UUID.randomUUID();
	}

	@Override
	public int hashCode() {
		return (int) (wrappedValue ^ (wrappedValue >>> 32));
	}

	public long longValue() {
		return wrappedValue;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( SequenceValue.class != obj.getClass() )
			return false;
		SequenceValue other = (SequenceValue) obj;
		if ( wrappedValue != other.wrappedValue )
			return false;
		if ( !unique.equals( other.unique ) )
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SequenceValue [wrappedValue=" + wrappedValue + ", unique=" + unique + "]";
	}

}
