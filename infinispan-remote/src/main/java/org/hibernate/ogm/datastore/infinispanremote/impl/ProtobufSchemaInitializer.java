/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispanremote.impl;

import java.util.Iterator;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.ogm.datastore.infinispanremote.impl.protobuf.SchemaDefinitions;
import org.hibernate.ogm.datastore.infinispanremote.impl.protobuf.TableDefinition;
import org.hibernate.ogm.datastore.spi.BaseSchemaDefiner;
import org.hibernate.ogm.datastore.spi.DatastoreProvider;
import org.hibernate.ogm.options.spi.OptionsService;
import org.hibernate.ogm.type.spi.GridType;
import org.hibernate.ogm.type.spi.TypeTranslator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.type.Type;

/**
 * Create and/or validate the protobuf schema definitions on the Infinispan grid.
 */
public class ProtobufSchemaInitializer extends BaseSchemaDefiner {

	@Override
	public void initializeSchema(SchemaDefinitionContext context) {
		ServiceRegistryImplementor serviceRegistry = context.getSessionFactory().getServiceRegistry();

		OptionsService optionsService = serviceRegistry.getService( OptionsService.class );
		TypeTranslator typeTranslator = serviceRegistry.getService( TypeTranslator.class );
		InfinispanRemoteDatastoreProvider datastoreProvider = (InfinispanRemoteDatastoreProvider) serviceRegistry.getService( DatastoreProvider.class );
		datastoreProvider.provideEntityMetadata(
				context.getAllEntityKeyMetadata(),
				context.getAllAssociationKeyMetadata(),
				context.getAllIdSourceKeyMetadata()
		);
		String protobufPackageName = datastoreProvider.getProtobufPackageName();
		SchemaDefinitions sd = new SchemaDefinitions( protobufPackageName );
		for ( Namespace namespace : context.getDatabase().getNamespaces() ) {
			for ( Table table : namespace.getTables() ) {
				if ( table.isPhysicalTable() ) {
					createTableDefinition( context.getSessionFactory(), sd, table, typeTranslator );
				}
			}
		}
		datastoreProvider.registerSchemaDefinitions( sd );
	}

	private void createTableDefinition(SessionFactoryImplementor sessionFactory, SchemaDefinitions sd, Table table, TypeTranslator typeTranslator) {
		//FIXME implement me
//		org.infinispan.protostream.FileDescriptorSource.fromString(String, String)
		TableDefinition td = new TableDefinition( table.getName() );
		/*
		 * The primary key is also defined among "normal" columns.
		 * Do we need this to encode the K of the K/V store?
		 * It's not indexed..
		 *
		if ( table.hasPrimaryKey() ) {
//			td.addPrimaryKey( table.getPrimaryKey() );
			for ( Column pkColumn : table.getPrimaryKey().getColumns() ) {
				String name = pkColumn.getName();
				Type type = pkColumn.getValue().getType();
				GridType gridType = typeTranslator.getType( type );
				td.addPrimaryKeyColumn( pkColumn, gridType );
			}
		}
		*/
		Iterator<Column> columnIterator = table.getColumnIterator();
		while ( columnIterator.hasNext() ) {
			Column column = columnIterator.next();
			String columnName = column.getName();
			Value value = column.getValue();
			Type type = value.getType();
			if ( type.isAssociationType() ) {
				type = type.getSemiResolvedType( sessionFactory );
				if ( type.isComponentType() ) {
					int index = column.getTypeIndex();
					type = ((org.hibernate.type.ComponentType) type).getSubtypes()[index];
				}
			}
			else if ( type.isComponentType() ) {
				int index = column.getTypeIndex();
				type = ((org.hibernate.type.ComponentType) column.getValue().getType()).getSubtypes()[index];
			}
			GridType gridType = typeTranslator.getType( type );
			td.addValueColumn( column, gridType );
		}
		sd.registerTableDefinition( td );
	}

	@Override
	public void validateMapping(SchemaDefinitionContext context) {
		// TODO
	}
}
