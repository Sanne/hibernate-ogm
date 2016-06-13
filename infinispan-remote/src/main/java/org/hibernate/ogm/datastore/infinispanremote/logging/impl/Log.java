/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.logging.impl;

import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.ERROR;

import java.io.IOException;

import org.hibernate.HibernateException;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Log messages and exceptions of the Infinispan Remote dialect.
 * The id range reserved for this dialect is 1701-1800.
 */
@MessageLogger(projectCode = "OGM")
public interface Log extends org.hibernate.ogm.util.impl.Log {

	@Message(id = 1701, value = "The Hot Rod client configuration was not defined")
	HibernateException hotrodClientConfigurationMissing();

	@Message(id = 1702, value = "Could not load the Hot Rod client configuration properties")
	HibernateException failedLoadingHotRodConfigurationProperties(@Cause IOException e);

	@Message(id = 1703, value = "Protobuf schema '%s' successfully deployed")
	@LogMessage(level = INFO)
	void successfullSchemaDeploy(String protobufName);

	@Message(id = 1704, value = "Protobuf schema '%s' was deployed but errors reported from server [%s]")
	@LogMessage(level = ERROR)
	void errorAtSchemaDeploy(String generatedProtobufName, String schemaDeployErrors);

}
