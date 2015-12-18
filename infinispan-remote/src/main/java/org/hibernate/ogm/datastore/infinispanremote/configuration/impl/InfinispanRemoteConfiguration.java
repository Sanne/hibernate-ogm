/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.configuration.impl;

import java.net.URL;
import java.util.Map;

import org.hibernate.ogm.datastore.infinispanremote.InfinispanRemoteProperties;
import org.hibernate.ogm.util.configurationreader.spi.ConfigurationPropertyReader;
import org.hibernate.ogm.util.impl.Log;
import org.hibernate.ogm.util.impl.LoggerFactory;

/**
 * Configuration for {@link InfinispanRemoteProperties}.
 */
public class InfinispanRemoteConfiguration {

	private static final Log log = LoggerFactory.make();

	private URL configurationResource;

	/**
	 * The location of the configuration file.
	 *
	 * @see InfinispanRemoteProperties#CONFIGURATION_RESOURCE_NAME
	 * @return might be the name of the file (too look it up in the class path) or an URL to a file.
	 */
	public URL getConfigurationResourceUrl() {
		return configurationResource;
	}

	/**
	 * Initialize the internal values form the given {@link Map}.
	 *
	 * @param configurationMap
	 *            The values to use as configuration
	 */
	public void initConfiguration(Map<?, ?> configurationMap) {
		ConfigurationPropertyReader propertyReader = new ConfigurationPropertyReader( configurationMap );

		this.configurationResource = propertyReader
				.property( InfinispanRemoteProperties.CONFIGURATION_RESOURCE_NAME, URL.class )
				.getValue();

		log.tracef( "Initializing Infinispan Hot Rod client from configuration file at '%1$s'", configurationResource );
	}
}
