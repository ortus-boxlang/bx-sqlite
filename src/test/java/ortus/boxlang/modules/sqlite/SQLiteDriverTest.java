package ortus.boxlang.modules.sqlite;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.jdbc.drivers.DatabaseDriverType;
import ortus.boxlang.runtime.scopes.Key;

public class SQLiteDriverTest {

	@Test
	@DisplayName( "Test getName()" )
	public void testGetName() {
		SQLiteDriver	driver			= new SQLiteDriver();
		Key				expectedName	= new Key( "SQLite" );
		assertThat( driver.getName() ).isEqualTo( expectedName );
	}

	@Test
	@DisplayName( "Test getType()" )
	public void testGetType() {
		SQLiteDriver		driver			= new SQLiteDriver();
		DatabaseDriverType	expectedType	= DatabaseDriverType.SQLITE;
		assertThat( driver.getType() ).isEqualTo( expectedType );
	}

	@Test
	@DisplayName( "Test getClassName()" )
	public void testGetClassName() {
		SQLiteDriver	driver				= new SQLiteDriver();
		String			expectedClassName	= "org.sqlite.JDBC";
		assertThat( driver.getClassName() ).isEqualTo( expectedClassName );
	}

	@Test
	@DisplayName( "Test buildConnectionURL()" )
	public void testBuildConnectionURL() {
		SQLiteDriver		driver	= new SQLiteDriver();
		DatasourceConfig	config	= new DatasourceConfig();
		config.properties.put( "driver", "sqlite" );
		config.properties.put( "database", "mydb" );

		String expectedURL = "jdbc:sqlite:mydb";
		assertThat( driver.buildConnectionURL( config ) ).isEqualTo( expectedURL );
	}

	@DisplayName( "Throw an exception if the database is not found" )
	@Test
	public void testBuildConnectionURLNoDatabase() {
		SQLiteDriver		driver	= new SQLiteDriver();
		DatasourceConfig	config	= new DatasourceConfig();
		try {
			driver.buildConnectionURL( config );
		} catch ( IllegalArgumentException e ) {
			assertThat( e.getMessage() ).isEqualTo( "The database property is required for the SQLite JDBC Driver" );
		}
	}

}
