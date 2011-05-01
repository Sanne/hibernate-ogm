/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.ogm.test.utils;

import java.util.Properties;
import java.util.Set;

import org.hibernate.ogm.datastore.infinispan.impl.CacheManagerServiceProvider;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

public class OGMInspect {
	
	public static void main(String[] args) throws InterruptedException {
		Properties cfg = new Properties();
		CacheManagerServiceProvider cacheManagerProvider = new CacheManagerServiceProvider();
		cacheManagerProvider.start( cfg );
		try {
			EmbeddedCacheManager cacheManager = cacheManagerProvider.getService();
			Thread.sleep( 1000l );
			Set<String> cacheNames = cacheManager.getCacheNames();
			System.out.println( "Cache Names: " + cacheNames );
			printCache( cacheManager.getCache(), "DEFAULT" );
			for ( String cacheName : cacheNames ) {
				printCache( cacheManager.getCache( cacheName ), cacheName );
				addTokenData( cacheManager.getCache( cacheName ), cacheName );
			}
//			Thread.sleep( 5000L );
		}
		finally {
			cacheManagerProvider.stop();
			System.exit( 0 );
		}
	}

	private static void addTokenData(Cache<Object, Object> cache, String cacheName) {
		//there's totally no need for this, I use it to verify that two instances of this same app are able to merge
		cache.put( "KeyFor" + cacheName, "Value for " + cacheName );
	}

	private static void printCache(Cache<Object, Object> cache, String cacheName) {
		System.out.println("\tCache: " + cacheName );
		Set<Object> keySet = cache.keySet();
		for ( Object k : keySet ) {
			System.out.println("\t\tkey: " + k);
			System.out.println("\t\t\tvalue: " + cache.get( k ));
		}
		System.out.println("\t");
	}

}