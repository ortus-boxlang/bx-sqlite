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

		// Build the Embedded URL
		return String.format(
		    "jdbc:sqlite:%s",
		    database
		);
	}

}
