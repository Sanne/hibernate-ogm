/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.utils;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

/**
 * Helper class allowing you to run all or any specified subset of test available on the classpath.
 *
 * This method is for example useful to run all or parts of the <i>backendtck</i>.
 *
 * Before launching the Unit test, launch it as a regular application to start the Hot Rod server:
 * you need a server running for most of the unit tests of the Hot Rod backend.
 *
 * @author Hardy Ferentschik
 * @author Sanne Grinovero
 */
@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({ ".*CRUDTest" })
public class InfinispanRemoteBackendTckHelper {

	public static void main(String[] args) {
		RemoteHotRodServerRule hotrodServer = new RemoteHotRodServerRule();
		try {
			hotrodServer.before();
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
