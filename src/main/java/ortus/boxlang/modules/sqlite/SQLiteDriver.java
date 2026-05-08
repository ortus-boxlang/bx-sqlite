/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.modules.sqlite;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.jdbc.drivers.DatabaseDriverType;
import ortus.boxlang.runtime.jdbc.drivers.GenericJDBCDriver;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.Struct;

public class SQLiteDriver extends GenericJDBCDriver {

	/**
	 * Constructor
	 */
	public SQLiteDriver() {
		super();
		this.name					= new Key( "SQLite" );
		this.type					= DatabaseDriverType.SQLITE;
		this.driverClassName		= "org.sqlite.JDBC";
		this.defaultDelimiter		= ";";
		this.defaultURIDelimiter	= ";";
		this.defaultCustomParams	= Struct.of();
		this.defaultProperties		= Struct.of();
	}

	/**
	 * Build the connection URL for the SQLite Driver
	 * <p>
	 *
	 * <pre>
	 * jdbc:sqlite:[path_to_sqlite_file]*
	 * </pre>
	 *
	 * @param config The DatasourceConfig object
	 *
	 * @return The connection URL
	 */
	@Override
	public String buildConnectionURL( DatasourceConfig config ) {
		// Validate the database
		String database = ( String ) config.properties.getOrDefault( "database", "" );
		if ( database.isEmpty() ) {
			throw new IllegalArgumentException( "The database property is required for the SQLite JDBC Driver" );
		}

		database	= normalizeMemoryDatabasePath( database );

		// Normalize file paths for cross-platform compatibility
		database	= normalizeFilePath( database );

		// Build the Embedded URL
		return String.format(
		    "jdbc:sqlite:%s",
		    database
		);
	}

	/**
	 * Normalize the memory database path to the format expected by the SQLite JDBC driver
	 *
	 * @param database The database string from the configuration
	 *
	 * @return The normalized database string
	 */
	private String normalizeMemoryDatabasePath( String database ) {
		// Handle the special case of :memory: which is the default in-memory database for SQLite
		if ( database.equals( ":memory:" ) ) {
			return database;
		}

		// If it doesn't start with memory: it's a file path, so return as is
		if ( !database.startsWith( "memory:" ) ) {
			return database;
		}

		// For memory databases, we need to convert to the format expected by the SQLite JDBC driver for shared in-memory databases
		String	memoryName	= database.substring( "memory:".length() );
		int		paramsIndex	= memoryName.indexOf( ';' );
		if ( paramsIndex >= 0 ) {
			memoryName = memoryName.substring( 0, paramsIndex );
		}

		// If the memory name is blank, we can use the anonymous in-memory database
		if ( memoryName.isBlank() ) {
			return ":memory:";
		}

		// Return the formatted memory database URL for the SQLite JDBC driver
		return String.format( "file:%s?mode=memory&cache=shared", memoryName );
	}

	/**
	 * Normalize file paths for cross-platform compatibility with SQLite JDBC driver.
	 * Converts Windows backslashes to forward slashes and ensures proper path format.
	 *
	 * @param database The database path string
	 *
	 * @return The normalized database path
	 */
	private String normalizeFilePath( String database ) {
		// Skip normalization for memory databases and special URIs
		if ( database.startsWith( "memory:" ) || database.startsWith( "file:" ) || database.equals( ":memory:" ) ) {
			return database;
		}

		// Normalize path separators for cross-platform compatibility
		// SQLite JDBC expects forward slashes, even on Windows
		return database.replace( "\\", "/" );
	}

}
