/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.test.integration.cassandra;

import org.hibernate.ogm.test.integration.testcase.MagiccardsDatabaseScenario;
import org.hibernate.ogm.test.integration.testcase.controller.MagicCardsCollectionBean;
import org.hibernate.ogm.test.integration.testcase.model.MagicCard;
import org.hibernate.ogm.test.integration.testcase.util.ModulesHelper;
import org.hibernate.ogm.test.integration.testcase.util.TestingPersistenceDescriptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.junit.runner.RunWith;

/**
 * Test for the Hibernate OGM module in WildFly using Cassandra.
 * The class name has to finish with "ITCassandra" for the test to be enabled in the right profile.
 * <p>
 * At time of writing, the Cassandra GridDialect implementation is not ready to handle associations
 * so we'll use an over simplified model.
 *
 * @author Sanne Grinovero
 */
@RunWith(Arquillian.class)
public class ModulesMagicDeckIT extends MagiccardsDatabaseScenario {

	@Deployment
	public static Archive<?> createTestArchive() {
		WebArchive webArchive = ShrinkWrap
				.create( WebArchive.class, "modules-magic-cassandra.war" )
				.addClasses( MagicCard.class, MagicCardsCollectionBean.class, ModulesMagicDeckIT.class, MagiccardsDatabaseScenario.class );
		String persistenceXml = persistenceXml().exportAsString();
		webArchive.addAsResource( new StringAsset( persistenceXml ), "META-INF/persistence.xml" );
		ModulesHelper.addModulesDependencyDeclaration( webArchive, "org.hibernate.ogm:${hibernate-ogm.module.slot} services, org.hibernate.ogm.cassandra:${hibernate-ogm.module.slot} services" );
		return webArchive;
	}

	private static PersistenceDescriptor persistenceXml() {
		return new TestingPersistenceDescriptor
				.Builder( MagicCard.class )
				.name( "primary" )
				.setProperty( "hibernate.ogm.datastore.provider", "cassandra_experimental" )
				.setProperty( "hibernate.ogm.datastore.database", "ogm_test_database" )
				.setCassandraHostName()
				.setCassandraPort()
				.persistenceXml();
	}

}
