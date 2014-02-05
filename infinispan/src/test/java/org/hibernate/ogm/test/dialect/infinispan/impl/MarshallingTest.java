/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.ogm.test.dialect.infinispan.impl;

import static org.fest.assertions.Assertions.assertThat;

import java.io.Serializable;

import org.infinispan.Cache;
import org.infinispan.atomic.AtomicMapLookup;
import org.infinispan.atomic.FineGrainedAtomicMap;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Test;

/**
 * @author Gunnar Morling
 */
public class MarshallingTest {

	@Test
	public void testMarshalling() throws Exception {
		EmbeddedCacheManager manager = new DefaultCacheManager( "infinispan-local.xml" );

		//The test passes when using this manager instead

//		Configuration configuration = new ConfigurationBuilder()
//		.storeAsBinary()
//		.transaction()
//			.transactionMode(TransactionMode.TRANSACTIONAL )
//			.transactionManagerLookup(new JBossStandaloneJTAManagerLookup() )
//		.build();
//
//		EmbeddedCacheManager manager = new DefaultCacheManager();
//		manager.defineConfiguration( "testCache", configuration );

		Cache<CacheKey, Object> cache = manager.getCache( "testCache", true );
		CacheKey rowKey = new CacheKey( "Some Key" );

		// Bonus question: Why is an CCE raised when using the cache below?

		// Cache<String, Object> cache = manager.getCache( "testCache", true );
		// String rowKey = "cacheKey";

		Object key = "the key";
		Object value = "the value";

		FineGrainedAtomicMap<Object, Object> map = AtomicMapLookup.getFineGrainedAtomicMap( cache, rowKey, true );
		map.put( key, value );

		assertThat( map.get( key ) ).isNotNull();
	}

	public static class CacheKey implements Serializable {

		String name;

		public CacheKey(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj )
				return true;
			if ( obj == null )
				return false;
			if ( getClass() != obj.getClass() )
				return false;
			CacheKey other = (CacheKey) obj;
			if ( name == null ) {
				if ( other.name != null )
					return false;
			}
			else if ( !name.equals( other.name ) )
				return false;
			return true;
		}
	}
}
