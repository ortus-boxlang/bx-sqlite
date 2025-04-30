# bx-sqlite

```
|:------------------------------------------------------:|
| ⚡︎ B o x L a n g ⚡︎
| Dynamic : Modular : Productive
|:------------------------------------------------------:|
```

<blockquote>
	Copyright Since 2023 by Ortus Solutions, Corp
	<br>
	<a href="https://www.boxlang.io">www.boxlang.io</a> |
	<a href="https://www.ortussolutions.com">www.ortussolutions.com</a>
</blockquote>

<p>&nbsp;</p>

This module provides a BoxLang JDBC driver for SQLite.

## Examples

See [BoxLang's Defining Datasources](https://boxlang.ortusbooks.com/boxlang-language/syntax/queries#defining-datasources) documentation for full examples on where and how to construct a datasource connection pool.

Here's a few examples of some SQLite datasources:

### Connecting to an In-Memory Database

You can specify an in-memory database using this connection string: `jdbc:sqlite:memory:{MyDBName};create=true` where `{MyDBName}` is replaced with your database name of choice:

```js
this.datasources[ "testDB" ] = {
	"driver"  : "sqlite",
	"protocol": "memory",
	"database": "testDB"
};
```

### Connecting to a Database Directory On Disk

You can also work with an on-disk database:

```js
this.datasources[ "AutoDB" ] = {
	"driver"  : "sqlite",
	"protocol": "directory",
	"database": "/home/michael/myApp/resources/AutoDB"
};
```

## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

### THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
