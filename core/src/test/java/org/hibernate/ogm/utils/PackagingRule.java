/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.utils;

import java.io.IOException;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.rules.ExternalResource;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;

/**
 * test case useful when one want to write a test relying on an archive (like a JPA archive)
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard &lt;emmanuel@hibernate.org&gt;
 * @author Sanne Grinovero
 */
public class PackagingRule extends ExternalResource {

	protected static ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
	ShrinkWrapClassLoader sd;

	private static final ArchivePath persistencePath = ArchivePaths.create( "persistence.xml" );
	private final JavaArchive archive;
	private final ShrinkWrapClassLoader classLoader;

	public PackagingRule(String persistenceConfResource, Class<?>... entities) {
		archive = ShrinkWrap.create( JavaArchive.class, "jtastandalone.jar" );
		archive.addClasses( entities );
		archive.addAsManifestResource( persistenceConfResource, persistencePath );
		classLoader = new ShrinkWrapClassLoader( originalClassLoader, archive );
	}

	@Override
	public void before() throws Throwable {
		super.before();
		Thread.currentThread().setContextClassLoader( classLoader );
	}

	@Override
	public void after() {
		// reset the classloader
		Thread.currentThread().setContextClassLoader( originalClassLoader );
		try {
			// Release the open input streams of ShrinkWrapClassLoader
			classLoader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		super.after();
	}

}
