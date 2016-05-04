/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.test.integration.testcase.util;

import java.util.HashSet;

import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceUnit;

public class TestingPersistenceDescriptor {

	private TestingPersistenceDescriptor() {
		//Not to be constructed
	}

	public static class Builder {
	
		private PersistenceUnit<PersistenceDescriptor> persistenceUnit;

		public Builder(Class<?>... entities) {
			HashSet<String> entityNamesSet = new HashSet<>();
			for ( Class<?> c : entities ) {
				entityNamesSet.add( c.getName() );
			}
			persistenceUnit = Descriptors.create( PersistenceDescriptor.class )
					.version( "2.0" )
					.createPersistenceUnit()
					.provider( "org.hibernate.ogm.jpa.HibernateOgmPersistence" )
					.clazz( entityNamesSet.toArray(new String[0]) );
			setDefaultProperties( this );
		}

		public Builder name(String puName) {
			persistenceUnit.name( puName );
			return this;
		}

		public Builder setProperty(String key, String value) {
			persistenceUnit.getOrCreateProperties().createProperty().name( key ).value( value );
			return this;
		}
	
		public PersistenceDescriptor persistenceXml() {
			return persistenceUnit.up();
		}

		public Builder setCassandraHostName() {
			CassandraConfigurationHelper.setCassandraHostName( persistenceUnit.getOrCreateProperties() );
			return this;
		}

		public Builder setCassandraPort() {
			CassandraConfigurationHelper.setCassandraPort( persistenceUnit.getOrCreateProperties() );
			return this;
		}

		private static void setDefaultProperties(Builder builder) {
			builder.setProperty( "jboss.as.jpa.providerModule", "application" );
			builder.setProperty( "hibernate.search.default.directory_provider", "ram" );
			builder.setProperty( "hibernate.transaction.jta.platform", "JBossAS" );
			builder.setProperty( "hibernate.search.lucene_version", "LUCENE_CURRENT" );
		}

	}

}
