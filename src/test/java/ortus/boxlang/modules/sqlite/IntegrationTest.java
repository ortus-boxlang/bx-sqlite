/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.modules.sqlite;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.Struct;

/**
 * This loads the module and runs an integration test on the module.
 */
public class IntegrationTest extends BaseIntegrationTest {

	// Add a before all to create the named datasource for the tests
	@BeforeEach
	public void setupEach() {
		super.setupEach();

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", freshMemoryDatabase( "integrationTest" )
		        )
		    )
		);

		context.clearConfigCache();
	}

	@DisplayName( "Test the module loads in BoxLang" )
	@Test
	public void testModuleLoads() {
		// Verify Registrations
		assertThat( moduleService.getRegistry().containsKey( moduleName ) ).isTrue();
		assertThat( runtime.getDataSourceService().hasDriver( moduleName ) ).isTrue();

		// @formatter:off
		runtime.executeSource(
		    """
				try{
					queryExecute( "DROP table developers", [], { "datasource": "sqlite" } );
				}catch( any e ){
					// Ignore
				}
				queryExecute( "CREATE TABLE developers ( id INTEGER, name VARCHAR(155), role VARCHAR(155) )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO developers ( id, name, role ) VALUES ( 77, 'Michael Born', 'Developer' )", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT * FROM developers ORDER BY id", [], { "datasource": "sqlite" } );
				println( result )
			""",
		    context
		);
		// @formatter:on

		// Assert it executes
		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.size() ).isEqualTo( 1 );
	}

	@DisplayName( "Test SELECT query" )
	@Test
	public void testSelectQuery() {
		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", freshMemoryDatabase( "selectTest" )
		        )
		    )
		);

		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE users ( id INTEGER PRIMARY KEY, name VARCHAR(155), email VARCHAR(155) )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO users ( id, name, email ) VALUES ( 1, 'John Doe', 'john@example.com' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO users ( id, name, email ) VALUES ( 2, 'Jane Smith', 'jane@example.com' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO users ( id, name, email ) VALUES ( 3, 'Bob Wilson', 'bob@example.com' )", [], { "datasource": "sqlite" } );
				selectResult = queryExecute( "SELECT * FROM users WHERE email LIKE '%example.com' ORDER BY id", [], { "datasource": "sqlite" } );
				singleResult = queryExecute( "SELECT name FROM users WHERE id = 2", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query	selectResult	= ( Query ) variables.get( Key.of( "selectResult" ) );
		Query	singleResult	= ( Query ) variables.get( Key.of( "singleResult" ) );

		assertThat( selectResult.size() ).isEqualTo( 3 );
		assertThat( selectResult.getRowAsStruct( 0 ).get( Key.of( "id" ) ) ).isEqualTo( 1L );
		assertThat( selectResult.getRowAsStruct( 0 ).get( Key.of( "name" ) ) ).isEqualTo( "John Doe" );
		assertThat( singleResult.size() ).isEqualTo( 1 );
		assertThat( singleResult.getRowAsStruct( 0 ).get( Key.of( "name" ) ) ).isEqualTo( "Jane Smith" );
	}

	@DisplayName( "Test UPDATE query" )
	@Test
	public void testUpdateQuery() {
		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", freshMemoryDatabase( "updateTest" )
		        )
		    )
		);

		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE products ( id INTEGER PRIMARY KEY, name VARCHAR(155), price DECIMAL(10,2) )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO products ( id, name, price ) VALUES ( 1, 'Widget', 9.99 )", [], { "datasource": "sqlite" } );
				queryExecute( "UPDATE products SET price = 14.99 WHERE id = 1", [], { "datasource": "sqlite" } );
				verifyResult = queryExecute( "SELECT price FROM products WHERE id = 1", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query verifyResult = ( Query ) variables.get( Key.of( "verifyResult" ) );
		assertThat( verifyResult.getRowAsStruct( 0 ).get( Key.of( "price" ) ) ).isEqualTo( 14.99 );
	}

	@DisplayName( "Test DELETE query" )
	@Test
	public void testDeleteQuery() {
		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", freshMemoryDatabase( "deleteTest" )
		        )
		    )
		);

		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE orders ( id INTEGER PRIMARY KEY, product VARCHAR(155), quantity INTEGER )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO orders ( id, product, quantity ) VALUES ( 1, 'Widget', 10 )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO orders ( id, product, quantity ) VALUES ( 2, 'Gadget', 5 )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO orders ( id, product, quantity ) VALUES ( 3, 'Doohickey', 3 )", [], { "datasource": "sqlite" } );
				queryExecute( "DELETE FROM orders WHERE quantity < 5", [], { "datasource": "sqlite" } );
				remainingResult = queryExecute( "SELECT * FROM orders ORDER BY id", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query remainingResult = ( Query ) variables.get( Key.of( "remainingResult" ) );
		assertThat( remainingResult.size() ).isEqualTo( 2 );
		assertThat( remainingResult.getRowAsStruct( 0 ).get( Key.of( "product" ) ) ).isEqualTo( "Widget" );
		assertThat( remainingResult.getRowAsStruct( 1 ).get( Key.of( "product" ) ) ).isEqualTo( "Gadget" );
	}
}
