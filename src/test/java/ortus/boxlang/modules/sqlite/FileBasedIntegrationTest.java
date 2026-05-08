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

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Query;
import ortus.boxlang.runtime.types.Struct;

/**
 * Integration tests for file-based SQLite databases.
 */
public class FileBasedIntegrationTest extends BaseIntegrationTest {

	private Path tempDbPath;

	@BeforeEach
	public void setupFileTest() throws Exception {
		super.setupEach();
		tempDbPath = Path.of( freshFileDatabase( "fileTest" ) );
	}

	@AfterEach
	public void teardownFileTest() throws Exception {
		// Close the datasource pool FIRST (Windows requires files to be closed before deletion)
		super.teardownEach();

		// Give the pool a moment to fully release the file handle on Windows
		try {
			Thread.sleep( 100 );
		} catch ( InterruptedException e ) {
			Thread.currentThread().interrupt();
		}

		if ( tempDbPath != null ) {
			Files.deleteIfExists( tempDbPath );
			Path parent = tempDbPath.getParent();
			if ( parent != null && Files.isDirectory( parent ) ) {
				Files.deleteIfExists( parent );
			}
		}
	}

	@DisplayName( "Test file-based database CREATE and SELECT" )
	@Test
	public void testFileBasedCreateAndSelect() throws Exception {
		String dbPath = toSqlitePath( tempDbPath );

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", dbPath
		        )
		    )
		);
		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE employees ( id INTEGER PRIMARY KEY, name VARCHAR(155), department VARCHAR(155) )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO employees ( id, name, department ) VALUES ( 1, 'Alice Johnson', 'Engineering' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO employees ( id, name, department ) VALUES ( 2, 'Bob Smith', 'Marketing' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO employees ( id, name, department ) VALUES ( 3, 'Carol White', 'Engineering' )", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT * FROM employees WHERE department = 'Engineering' ORDER BY id", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.size() ).isEqualTo( 2 );
		assertThat( result.getRowAsStruct( 0 ).get( Key.of( "name" ) ) ).isEqualTo( "Alice Johnson" );
		assertThat( result.getRowAsStruct( 1 ).get( Key.of( "name" ) ) ).isEqualTo( "Carol White" );

		assertThat( Files.exists( tempDbPath ) ).isTrue();
	}

	@DisplayName( "Test file-based database UPDATE and verify persistence" )
	@Test
	public void testFileBasedUpdateAndPersistence() throws Exception {
		String dbPath = toSqlitePath( tempDbPath );

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", dbPath
		        )
		    )
		);
		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE inventory ( id INTEGER PRIMARY KEY, item VARCHAR(155), quantity INTEGER )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO inventory ( id, item, quantity ) VALUES ( 1, 'Widgets', 100 )", [], { "datasource": "sqlite" } );
				queryExecute( "UPDATE inventory SET quantity = 75 WHERE id = 1", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT quantity FROM inventory WHERE id = 1", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.getRowAsStruct( 0 ).get( Key.of( "quantity" ) ) ).isEqualTo( 75L );
	}

	@DisplayName( "Test file-based database DELETE operations" )
	@Test
	public void testFileBasedDelete() throws Exception {
		String dbPath = toSqlitePath( tempDbPath );

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", dbPath
		        )
		    )
		);
		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE tasks ( id INTEGER PRIMARY KEY, description VARCHAR(155), completed BOOLEAN )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO tasks ( id, description, completed ) VALUES ( 1, 'Task 1', 0 )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO tasks ( id, description, completed ) VALUES ( 2, 'Task 2', 1 )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO tasks ( id, description, completed ) VALUES ( 3, 'Task 3', 0 )", [], { "datasource": "sqlite" } );
				queryExecute( "DELETE FROM tasks WHERE completed = 1", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT * FROM tasks ORDER BY id", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.size() ).isEqualTo( 2 );
		assertThat( result.getRowAsStruct( 0 ).get( Key.of( "description" ) ) ).isEqualTo( "Task 1" );
		assertThat( result.getRowAsStruct( 1 ).get( Key.of( "description" ) ) ).isEqualTo( "Task 3" );
	}

	@DisplayName( "Test file-based database with AUTOINCREMENT" )
	@Test
	public void testFileBasedAutoincrement() throws Exception {
		String dbPath = toSqlitePath( tempDbPath );

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", dbPath
		        )
		    )
		);
		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE logs ( id INTEGER PRIMARY KEY AUTOINCREMENT, message VARCHAR(255), created_at DATETIME DEFAULT CURRENT_TIMESTAMP )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO logs ( message ) VALUES ( 'First log entry' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO logs ( message ) VALUES ( 'Second log entry' )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO logs ( message ) VALUES ( 'Third log entry' )", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT id, message FROM logs ORDER BY id", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.size() ).isEqualTo( 3 );
		assertThat( result.getRowAsStruct( 0 ).get( Key.of( "id" ) ) ).isEqualTo( 1L );
		assertThat( result.getRowAsStruct( 1 ).get( Key.of( "id" ) ) ).isEqualTo( 2L );
		assertThat( result.getRowAsStruct( 2 ).get( Key.of( "id" ) ) ).isEqualTo( 3L );
	}

	@DisplayName( "Test file-based database with transactions" )
	@Test
	public void testFileBasedTransactions() throws Exception {
		String dbPath = toSqlitePath( tempDbPath );

		runtime.getConfiguration().datasources.put(
		    moduleName,
		    new DatasourceConfig( moduleName ).process(
		        Struct.of(
		            "driver", "sqlite",
		            "database", dbPath
		        )
		    )
		);
		context.clearConfigCache();

		// @formatter:off
		runtime.executeSource(
		    """
				queryExecute( "CREATE TABLE accounts ( id INTEGER PRIMARY KEY, name VARCHAR(155), balance DECIMAL(10,2) )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO accounts ( id, name, balance ) VALUES ( 1, 'Checking', 1000.00 )", [], { "datasource": "sqlite" } );
				queryExecute( "INSERT INTO accounts ( id, name, balance ) VALUES ( 2, 'Savings', 500.00 )", [], { "datasource": "sqlite" } );
				queryExecute( "BEGIN TRANSACTION", [], { "datasource": "sqlite" } );
				queryExecute( "UPDATE accounts SET balance = balance - 200 WHERE id = 1", [], { "datasource": "sqlite" } );
				queryExecute( "UPDATE accounts SET balance = balance + 200 WHERE id = 2", [], { "datasource": "sqlite" } );
				queryExecute( "COMMIT", [], { "datasource": "sqlite" } );
				result = queryExecute( "SELECT * FROM accounts ORDER BY id", [], { "datasource": "sqlite" } );
			""",
		    context
		);
		// @formatter:on

		Query result = ( Query ) variables.get( Key.result );
		assertThat( result.size() ).isEqualTo( 2 );
		assertThat( result.getRowAsStruct( 0 ).get( Key.of( "balance" ) ) ).isEqualTo( 800L );
		assertThat( result.getRowAsStruct( 1 ).get( Key.of( "balance" ) ) ).isEqualTo( 700L );
	}
}
