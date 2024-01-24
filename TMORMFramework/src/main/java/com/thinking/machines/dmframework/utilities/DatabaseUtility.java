package com.thinking.machines.dmframework.utilities;
import com.thinking.machines.dmframework.pojo.*;
import com.thinking.machines.dmframework.exceptions.*;
import java.util.*;
import java.sql.*;
import org.apache.commons.dbcp2.*;
import org.apache.log4j.*;
import java.math.*;
import java.text.*;
public class DatabaseUtility
{
private static BasicDataSource basicDataSource=null;
private static int CONNECTION_POOL_SIZE=5;
private static final Logger logger=Logger.getLogger(DatabaseUtility.class);
private static Database database=null;
private DatabaseUtility(){}
public static Connection getConnection() throws DMFrameworkException
{
try
{
return basicDataSource.getConnection();
}catch(SQLException sqlException)
{
logger.fatal("Fatal : "+sqlException.getMessage());
throw new DMFrameworkException("Unable to connect to database");
}
}
public static Database getDatabase(HashMap<String,String> databaseConfiguration,HashSet<String> tablesToIgnore,HashSet<String> viewsToIgnore) throws DMFrameworkException
{
//if(database!=null) return database;
String connectionString=databaseConfiguration.get("connectionString");
if(connectionString==null) throw new DMFrameworkException("Configuration error, missing [connectionString]");
String driver=databaseConfiguration.get("driver");
if(driver==null) throw new DMFrameworkException("Configuration error, missing [driver] name");
String architecture=databaseConfiguration.get("architecture");
if(architecture==null) throw new DMFrameworkException("Configuration error, missing database [architecture]");
String username=databaseConfiguration.get("username");
String password=databaseConfiguration.get("password");
boolean autoCommit=false;
if(username!=null && password==null)
{
password="";
}
// if condition basicDataSource==null;
basicDataSource=new BasicDataSource();
basicDataSource.setDriverClassName(driver);
basicDataSource.setUrl(connectionString);
basicDataSource.setDefaultAutoCommit(autoCommit);
if(username!=null)
{
basicDataSource.setUsername(username);
basicDataSource.setPassword(password);
basicDataSource.setInitialSize(CONNECTION_POOL_SIZE);
}
Connection connection=getConnection();
DatabaseMetaData databaseMetaData=null;
try
{
databaseMetaData=connection.getMetaData();
}catch(SQLException sqlException)
{
try { connection.close(); }catch(SQLException se){}
logger.fatal("Fatal : "+sqlException);
throw new DMFrameworkException("Unable to extract information about the database");
}
String schema=null;
String catalog=null;
if(architecture.equalsIgnoreCase("oracle 11g"))
{
schema=username.toUpperCase();
}
try
{
catalog=connection.getCatalog();
}catch(SQLException sqlException)
{
logger.fatal("Fatal : "+sqlException);
throw new DMFrameworkException("Unable to extract information about the database catalog");
}
System.out.println("Connection to database : successful");
List<Table> tables=getTables(databaseMetaData,catalog,schema,tablesToIgnore,databaseConfiguration);
List<View> views=getViews(databaseMetaData,catalog,schema,viewsToIgnore,databaseConfiguration);
List<String> sequences=null;
if(architecture.equalsIgnoreCase("oracle 11g"))
{
sequences=getSequences(connection);
}
try { connection.close(); }catch(SQLException sqlException){}
database=new Database();
database.setTables(tables);
database.setViews(views);
if(architecture.equalsIgnoreCase("oracle 11g")) database.setName(schema);
else database.setName(catalog);
database.setSequences(sequences);
database.setArchitecture(architecture);
System.out.println("TMORM Framework initialized : successfully");
return database;
}
public static Database getPreparedDatabase()
{
return database;
}
static private List<Table> getTables(DatabaseMetaData databaseMetaData,String catalog,String schema,HashSet<String> tablesToIgnore,HashMap<String,String> databaseConfiguration) throws DMFrameworkException
{
String architecture=databaseConfiguration.get("architecture");
String []tableTypes={"TABLE"};
Property property;
ResultSet tablesResultSet;
ResultSet columnsResultSet;
int columnIndex;
int numberOfColumns;
String tableName;
String primaryKey;
int keySequence;
Table table;
Column column;
String columnName;
List<Column> columns;
String defaultValue;
HashMap<String,Table> tablesHashMap=new HashMap<String,Table>();
List<Table> tables=new ArrayList<Table>();
HashMap<Integer,Column> columnsHashMap;
HashMap<Integer,String> primaryKeysHashMap;
String indexName;
String nameOfTable;
try
{
tablesResultSet=databaseMetaData.getTables(catalog,schema,"%",tableTypes);
while(tablesResultSet.next()) 
{
nameOfTable=tablesResultSet.getString("TABLE_NAME").trim();
if(tablesToIgnore.contains(nameOfTable.toUpperCase())) continue;
table=new Table();
table.setName(nameOfTable);
columnsHashMap=new HashMap<Integer,Column>();
columnsResultSet=databaseMetaData.getColumns(catalog,schema,table.getName(),null);
while(columnsResultSet.next())
{
column=new Column();
column.setTable(table);
column.setName(columnsResultSet.getString("COLUMN_NAME").trim());
property=new Property();
property.setName(Utilities.getJavaPropertyName(column.getName()));
column.setType(columnsResultSet.getInt("DATA_TYPE"));
if(architecture.equals("oracle 11g") && column.getType()==java.sql.Types.CHAR)
{
column.applyPadding(true);
}
property.setType(Types.getJavaType(column.getType()));
column.setProperty(property);
column.setWidth(columnsResultSet.getInt("COLUMN_SIZE"));
column.setDecimalPrecision(columnsResultSet.getInt("DECIMAL_DIGITS"));
column.setIsNullable(columnsResultSet.getInt("NULLABLE")==0?false:true);
defaultValue=columnsResultSet.getString("COLUMN_DEF");
if(defaultValue!=null) 
{
column.setDefaultValue(parseDefaultColumnValue(defaultValue.trim(),property.getType()));
}
columnIndex=columnsResultSet.getInt("ORDINAL_POSITION");
column.setIsAutoIncrementEnabled(columnsResultSet.getString("IS_AUTOINCREMENT").trim().equals("YES"));
columnsHashMap.put(columnIndex,column);
}
columnsResultSet.close();
columnsResultSet=databaseMetaData.getIndexInfo(catalog,schema,table.getName(),true,false);
numberOfColumns=columnsHashMap.size();
while(columnsResultSet.next())
{
indexName=columnsResultSet.getString("INDEX_NAME");
if(indexName==null) continue;
columnName=columnsResultSet.getString("COLUMN_NAME").trim();
for(int i=1;i<=numberOfColumns;i++)
{
column=columnsHashMap.get(i);
if(column.getName().equalsIgnoreCase(columnName))
{
column.setIsUnique(true);
column.setUniqueIndexName(indexName);
break;
}
}
}
columnsResultSet.close();
for(int i=1;i<=numberOfColumns;i++)
{
table.addColumn(columnsHashMap.get(i));
}

columnsResultSet=databaseMetaData.getPrimaryKeys(catalog,schema,table.getName());
primaryKeysHashMap=new HashMap<Integer,String>();
while(columnsResultSet.next())
{
primaryKey=columnsResultSet.getString("COLUMN_NAME").trim();
keySequence=columnsResultSet.getInt("KEY_SEQ");
primaryKeysHashMap.put(keySequence,primaryKey);
}
columnsResultSet.close();
for(int i=1;i<=primaryKeysHashMap.size();i++)
{
table.addPrimaryKey(primaryKeysHashMap.get(i));
}
table.setupUniqueKeys();
table.buildSQL(true,true,true);
tables.add(table);
tablesHashMap.put(table.getName(),table);
}
tablesResultSet.close();
String tableWithForeignKey;
String tableWithPrimaryKey;
String primaryKeyColumnName;
String foreignKeyColumnName;
String first,second;
Table ft;
Pair<Column,Column> columnMapping;
ResultSet foreignKeysResultSet;
for(Table t:tables)
{
foreignKeysResultSet=databaseMetaData.getExportedKeys(catalog, schema, t.getName());
if(foreignKeysResultSet.next())
{
tableWithPrimaryKey=t.getName();
do 
{
primaryKeyColumnName=foreignKeysResultSet.getString("PKCOLUMN_NAME");
tableWithForeignKey=foreignKeysResultSet.getString("FKTABLE_NAME").trim();
ft=tablesHashMap.get(tableWithForeignKey);
foreignKeyColumnName=foreignKeysResultSet.getString("FKCOLUMN_NAME").trim();
columnMapping=new Pair<Column,Column>(ft.getColumn(foreignKeyColumnName),t.getColumn(primaryKeyColumnName));
keySequence=foreignKeysResultSet.getInt("KEY_SEQ");
tablesHashMap.get(tableWithForeignKey).addForeignKey(tableWithPrimaryKey,keySequence,columnMapping);
columnMapping=new Pair<Column,Column>(t.getColumn(primaryKeyColumnName),ft.getColumn(foreignKeyColumnName));
t.addExportedKey(tableWithForeignKey,keySequence,columnMapping);
}while(foreignKeysResultSet.next());
foreignKeysResultSet.close();
}
}
for(Table t:tables)
{
t.buildForeignKeysSQL();
t.buildExportedKeysSQL();
}
}catch(SQLException sqlException)
{
logger.fatal("Fatal : "+sqlException);
throw new DMFrameworkException("Unable to extract information about the tables from database.");
}
return tables;
}
static private List<View> getViews(DatabaseMetaData databaseMetaData,String catalog,String schema,HashSet<String> viewsToIgnore,HashMap<String,String> databaseConfiguration) throws DMFrameworkException
{
String architecture=databaseConfiguration.get("architecture");
String []tableTypes={"VIEW"};
Property property;
ResultSet tablesResultSet;
ResultSet columnsResultSet;
int columnIndex;
int numberOfColumns;
String tableName;
View view;
Column column;
String columnName;
List<Column> columns;
List<View> views=new ArrayList<View>();
HashMap<Integer,Column> columnsHashMap;
String nameOfView;
try
{
tablesResultSet=databaseMetaData.getTables(catalog,schema,"%",tableTypes);
while(tablesResultSet.next()) 
{
nameOfView=tablesResultSet.getString("TABLE_NAME").trim();
if(viewsToIgnore.contains(nameOfView)) continue;
view=new View();
view.setName(nameOfView);
columnsHashMap=new HashMap<Integer,Column>();
columnsResultSet=databaseMetaData.getColumns(catalog,schema,view.getName(),null);
while(columnsResultSet.next())
{
column=new Column();
column.setView(view);
column.setName(columnsResultSet.getString("COLUMN_NAME").trim());
property=new Property();
property.setName(Utilities.getJavaPropertyName(column.getName()));
column.setType(columnsResultSet.getInt("DATA_TYPE"));
if(architecture.equals("oracle 11g") && column.getType()==java.sql.Types.CHAR)
{
column.applyPadding(true);
}
property.setType(Types.getJavaType(column.getType()));
column.setProperty(property);
column.setWidth(columnsResultSet.getInt("COLUMN_SIZE"));
column.setDecimalPrecision(columnsResultSet.getInt("DECIMAL_DIGITS"));
columnIndex=columnsResultSet.getInt("ORDINAL_POSITION");
columnsHashMap.put(columnIndex,column);
}
columnsResultSet.close();
numberOfColumns=columnsHashMap.size();
for(int i=1;i<=numberOfColumns;i++)
{
view.addColumn(columnsHashMap.get(i));
}
view.buildSQL();
views.add(view);
}
tablesResultSet.close();
}catch(SQLException sqlException)
{
logger.fatal("Fatal : "+sqlException);
throw new DMFrameworkException("Unable to extract information about the views from database");
}
return views;
}
public static Object parseDefaultColumnValue(String string,Class c)
{
Object object=null;
if(c.equals(Boolean.class))
{
object=Boolean.parseBoolean(string);
}
if(c.equals(Integer.class))
{
try
{
object=Integer.parseInt(string); 
}catch(NumberFormatException numberFormatException)
{
}
}
if(c.equals(Long.class))
{
try
{
object=Long.parseLong(string); 
}catch(NumberFormatException numberFormatException)
{
}
}
if(c.equals(BigDecimal.class))
{
try
{
object=new BigDecimal(string);
}catch(Exception exception)
{

}
}
if(c.equals(String.class))
{
object=string;
}
if(c.equals(java.sql.Date.class))
{
if(string.equals("0000-00-00")==false)
{
try
{
object=new java.sql.Date(new SimpleDateFormat("dd-MM-yyyy").parse(string).getTime());
}catch(ParseException parseException)
{
}
}
}
if(c.equals(java.sql.Time.class))
{
try
{
object=new java.sql.Time(new SimpleDateFormat("hh:mm:ss").parse(string).getTime());
}catch(ParseException parseException)
{
}
}
return object;
}
static public List<String> getSequences(Connection connection)
{
List<String> sequences=new ArrayList<String>();
ResultSet resultSet=null;
Statement statement=null;
try
{
statement=connection.createStatement();
resultSet=statement.executeQuery("select sequence_name from user_sequences order by sequence_name");
while(resultSet.next())
{
sequences.add(resultSet.getString("sequence_name").trim());
}
resultSet.close();
statement.close();
}catch(SQLException sqlException)
{
try { if(resultSet!=null && resultSet.isClosed()==false) resultSet.close(); } catch(SQLException sqlException2){}
try { if(statement!=null && statement.isClosed()==false) statement.close(); } catch(SQLException sqlException2) {}
}
return sequences;
}
}