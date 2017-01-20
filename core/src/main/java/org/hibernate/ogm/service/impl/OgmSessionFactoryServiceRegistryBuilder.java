/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.Service;
import org.hibernate.service.internal.ProvidedService;
import org.hibernate.service.internal.StandardSessionFactoryServiceInitiators;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistryBuilder;

/**
 * @author Davide D'Alto
 */
public class OgmSessionFactoryServiceRegistryBuilder implements SessionFactoryServiceRegistryBuilder {


	private final ServiceRegistryImplementor parent;

	private final List<SessionFactoryServiceInitiator> initiators = standardInitiatorList();
	private final List<ProvidedService> providedServices = new ArrayList<>();

	public OgmSessionFactoryServiceRegistryBuilder(ServiceRegistryImplementor parent) {
		this.parent = parent;
	}

	/**
	 * Used from the {@link #initiators} variable initializer
	 *
	 * @return List of standard initiators
	 */
	private static List<SessionFactoryServiceInitiator> standardInitiatorList() {
		final List<SessionFactoryServiceInitiator> initiators = new ArrayList<>();
		initiators.addAll( StandardSessionFactoryServiceInitiators.LIST );
		return initiators;
	}

	/**
	 * Adds a service initiator.
	 *
	 * @param initiator The initiator to be added
	 *
	 * @return this, for method chaining
	 */
	@Override
	@SuppressWarnings( {"UnusedDeclaration"})
	public SessionFactoryServiceRegistryBuilder addInitiator(SessionFactoryServiceInitiator initiator) {
		initiators.add( initiator );
		return this;
	}

	/**
	 * Adds a user-provided service.
	 *
	 * @param serviceRole The role of the service being added
	 * @param service The service implementation
	 *
	 * @return this, for method chaining
	 */
	@Override
	@SuppressWarnings( {"unchecked"})
	public SessionFactoryServiceRegistryBuilder addService(final Class serviceRole, final Service service) {
		providedServices.add( new ProvidedService( serviceRole, service ) );
		return this;
	}

	public SessionFactoryServiceRegistry buildSessionFactoryServiceRegistry(
			SessionFactoryImplementor sessionFactory,
			SessionFactoryOptions options) {
		return new OgmSessionFactoryServiceRegistryImpl(
				parent,
				initiators,
				providedServices,
				sessionFactory,
				options
		);
	}
}
