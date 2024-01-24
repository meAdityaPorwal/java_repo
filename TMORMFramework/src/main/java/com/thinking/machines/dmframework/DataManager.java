package com.thinking.machines.dmframework;
import com.thinking.machines.dmframework.pojo.*;
import com.thinking.machines.dmframework.utilities.*;
import com.thinking.machines.dmframework.exceptions.*;
import com.thinking.machines.dmframework.query.*;
import com.thinking.machines.dmframework.dml.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import org.apache.log4j.*;
import java.sql.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import java.io.*;
public class DataManager
{
private static Database database=null;
private Connection connection;
final public static Logger logger=Logger.getLogger(DataManager.class);
static 
{
try
{
database=DatabaseUtility.getDatabase(ConfigurationUtility.getDatabaseConfiguration(),ConfigurationUtility.getTablesToIgnore(),ConfigurationUtility.getViewsToIgnore());
}catch(ClassNotFoundException classNotFoundException)
{
logger.fatal("Configuration file TMDMFramework.xml contains wrong class name : "+classNotFoundException.getMessage());
throw new RuntimeException("Configuration file TMDMFramework.xml contains wrong class name : "+classNotFoundException.getMessage());
}
catch(DMFrameworkException dmFrameworkException)
{
String alternatePath=DataManager.class.getResource("/").toString();
System.out.println("Alternate path : "+alternatePath);
alternatePath=alternatePath.toString().substring(5);
System.out.println("Alternate path : "+alternatePath);
ConfigurationUtility.configurationFilePath=alternatePath+"TMDMFramework.xml";
try
{
database=DatabaseUtility.getDatabase(ConfigurationUtility.getDatabaseConfiguration(),ConfigurationUtility.getTablesToIgnore(),ConfigurationUtility.getViewsToIgnore());
}catch(ClassNotFoundException classNotFoundException)
{
logger.fatal("Configuration file TMDMFramework.xml contains wrong class name : "+classNotFoundException.getMessage());
throw new RuntimeException("Configuration file TMDMFramework.xml contains wrong class name : "+classNotFoundException.getMessage());
}
catch(DMFrameworkException dmFrameworkException2)
{
logger.fatal(dmFrameworkException2.getMessage());
throw new RuntimeException(dmFrameworkException2.getMessage());
}
}
}
public DataManager()
{

}

public void createDatabase(String connectionString,String username,String password,String rootPassword,Boolean createNewUser,String sqlFileName) throws DMFrameworkException 
{
String driver="com.mysql.jdbc.Driver";
String architecture="MYSQL";
String databaseName=connectionString.substring(connectionString.lastIndexOf("/")+1);
String connectionStringForRoot=connectionString.substring(0,connectionString.lastIndexOf("/")+1);
Statement statement=null;
try
{
try
{
Class.forName(driver);
}catch(ClassNotFoundException classNotFoundException)
{
throw new DMFrameworkException("Driver class for mysql is missing");
}
connection=DriverManager.getConnection(connectionStringForRoot,"root",rootPassword);
statement=connection.createStatement();
if(createNewUser==false)
{
ResultSet resultSet=statement.executeQuery("select exists(select 1 from mysql.user where user = '"+username+"')");
resultSet.next();
if(resultSet.getBoolean(1)==false)
{
throw new DMFrameworkException("user "+username+" does not exist");
}
}
}catch(SQLException sqlException)
{
throw new DMFrameworkException(sqlException.getMessage());
}
String sql="create database "+databaseName;
try
{
statement.executeUpdate(sql);
}catch(SQLException sqlException)
{
throw new DMFrameworkException("Database :"+databaseName+" already exist");
}
System.out.println("database :"+databaseName+" created");
if(createNewUser==true)
{
sql="create user '"+username+"'@'%' identified by '"+password+"' ";
try
{
statement.executeUpdate(sql);
System.out.println("user :"+username+" created");
}catch(SQLException sqlException)
{
}
}
try
{
sql="grant all privileges on "+databaseName+".* to '"+username+"'@'%' with grant option";
statement.executeUpdate(sql);
System.out.println("rights of database :"+databaseName+" has been granted to user :"+username);
}catch(SQLException sqlException)
{
throw new DMFrameworkException("unable to grant rights on database: "+databaseName+" to user :"+username);
}
File file=new File(sqlFileName);
RandomAccessFile raf=null;
try
{
raf=new RandomAccessFile(file,"r");
}catch(IOException ioException)
{
throw new DMFrameworkException("file :"+sqlFileName+" does not exist");
}
StringBuffer sb=new StringBuffer();
String line;
try
{
while(raf.getFilePointer()<file.length())
{
line=raf.readLine();
sb.append(line);
}
}catch(IOException ioException)
{
throw new DMFrameworkException(ioException.getMessage());
}
String[] sqlStatements=sb.toString().split(";");
int i=0;
try
{
sql="use "+databaseName;
statement.executeUpdate(sql);
while(i<sqlStatements.length)
{
statement.executeUpdate(sqlStatements[i]+";");
i++;
}
statement.close();
}catch(SQLException sqlException)
{
throw new DMFrameworkException("you have error in your sql statements at "+(i+1)+" in "+sqlFileName);
}
System.out.println("Statements of "+sqlFileName+" executed");
this.connection=null;
HashMap<String,String> map=new HashMap<String,String>();
HashSet<String> tablesToIgnore=new HashSet<String>();
HashSet<String> viewsToIgnore=new HashSet<String>();
map.put("connectionString",connectionString);
map.put("architecture",architecture);
map.put("driver",driver);
map.put("username",username);
map.put("password",password);
database=DatabaseUtility.getDatabase(map,tablesToIgnore,viewsToIgnore);
connection=DatabaseUtility.getConnection();
}

public void setDatabaseConfiguration(String connectionString,String username,String password) throws DMFrameworkException
{
String driver="com.mysql.jdbc.Driver";
String architecture="MYSQL";
HashMap<String,String> map=new HashMap<String,String>();
HashSet<String> tablesToIgnore=new HashSet<String>();
HashSet<String> viewsToIgnore=new HashSet<String>();
map.put("connectionString",connectionString);
map.put("architecture",architecture);
map.put("driver",driver);
map.put("username",username);
map.put("password",password);
database=DatabaseUtility.getDatabase(map,tablesToIgnore,viewsToIgnore);
connection=DatabaseUtility.getConnection();
EntityManager.setDatabase(database);
}

public String getConfiguration(String entityName) throws DMFrameworkException
{
Class c=null;
try
{
c=Class.forName(entityName);
}catch(ClassNotFoundException classNotFoundException)
{
throw new DMFrameworkException("Entity :"+entityName+" does not exist");
}
Entity entity=EntityManager.get(c);
ObjectMapper objectMapper=new ObjectMapper();
ObjectNode entityObjectNode=objectMapper.createObjectNode();
Table entityTable=entity.getTable();
entityObjectNode.put("entityName",Utilities.getJavaClassName(entityTable.getName()));
ObjectNode columnObjectNode;
ArrayNode columnArray=objectMapper.createArrayNode();
List<Column> columns=entityTable.getColumns();
Column column;
int i=0;
while(i<columns.size())
{
column=columns.get(i);
columnObjectNode=objectMapper.createObjectNode();
columnObjectNode.put("name",Utilities.getJavaPropertyName(column.getName()));
columnObjectNode.put("isNullable",column.getIsNullable());
columnObjectNode.put("isAutoIncrementEnable",column.getIsAutoIncrementEnabled());
columnObjectNode.put("isUnique",column.getIsUnique());
columnObjectNode.put("width",column.getWidth());
columnObjectNode.put("decimalPrecision",column.getDecimalPrecision());
columnObjectNode.put("type",com.thinking.machines.dmframework.utilities.Types.getJavaType(column.getType()).getSimpleName());
columnArray.add(columnObjectNode);
i++;
}
entityObjectNode.put("columns",columnArray);
ArrayNode primaryKeyArray=objectMapper.createArrayNode();
List<String> primaryKeys=entityTable.getPrimaryKeys();
i=0;
while(i<primaryKeys.size())
{
primaryKeyArray.add(Utilities.getJavaPropertyName(primaryKeys.get(i)));
i++;
}
entityObjectNode.put("primaryKeys",primaryKeyArray);
HashMap<String,LinkedList<Column>> uniqueKeysMap=entityTable.getUniqueKeys();
ArrayNode uniqueKeyArray=objectMapper.createArrayNode();
ArrayNode uniqueKeyColumnArray=objectMapper.createArrayNode();
Iterator<Map.Entry<String,LinkedList<Column>>> iterator;
Map.Entry<String,LinkedList<Column>> pair;
iterator=uniqueKeysMap.entrySet().iterator();
while(iterator.hasNext())
{
pair=iterator.next();
LinkedList<Column> list=pair.getValue();
i=0;
while(i<list.size())
{
uniqueKeyColumnArray.add(Utilities.getJavaPropertyName(list.get(i).getName()));
i++;
}
uniqueKeyArray.add(uniqueKeyColumnArray);
}
entityObjectNode.put("uniqueKeys",uniqueKeyArray);
return entityObjectNode.toString();
}

public <T> Select<T> select(Class<T> entityClass) throws DMFrameworkException
{
if(connection==null) throw new DMFrameworkException("Call to begin of DataManager is missing");
try
{
Entity entity=EntityManager.get(entityClass);
return entity.<T>select(connection);
}catch(DMFrameworkException dmFrameworkException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
dmFrameworkException.setDatabaseOperationException("Unable to apply select on "+entityClass.getSimpleName());
throw dmFrameworkException;
}
}
public void insert(Object object) throws DMFrameworkException,ValidatorException
{
if(connection==null) throw new DMFrameworkException("Call to begin of DataManager is missing");
Class entityClass=object.getClass();
LinkedList<Pair<Object,MethodWrapper>> oldValues=null;
try
{
Entity entity=EntityManager.get(entityClass);
HashMap<Integer,SequenceWrapper> sequenceWrappers;
sequenceWrappers=entity.getSequenceWrappers();
if(sequenceWrappers!=null && sequenceWrappers.size()>0)
{
oldValues=new LinkedList<Pair<Object,MethodWrapper>>();
SequenceWrapper sequenceWrapper;
String nextValSQLStatement;
MethodWrapper nextValObjectPropertyGetter;
MethodWrapper nextValObjectPropertySetter;
Method nextValResultSetGetter;
Object oldValue;
Statement nextValStatement;
ResultSet nextValResultSet;
for(int i=0;i<entity.getNumberOfValuesInInsert();i++)
{
sequenceWrapper=sequenceWrappers.get(new Integer(i+1));
if(sequenceWrapper!=null)
{
nextValObjectPropertyGetter=sequenceWrapper.getNextValObjectPropertyGetter();
nextValObjectPropertySetter=sequenceWrapper.getNextValObjectPropertySetter();
nextValResultSetGetter=sequenceWrapper.getNextValResultSetGetter();
try
{
nextValStatement=connection.createStatement();
nextValResultSet=nextValStatement.executeQuery(sequenceWrapper.getNextValSQLStatement());
nextValResultSet.next();
}catch(SQLException sqlException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
try
{
for(Pair<Object,MethodWrapper> nextValResetPair:oldValues)
{
nextValResetPair.getSecond().invoke(object,nextValResetPair.getFirst());
}
}catch(Exception exception)
{
// ignore, cannot do anything about it
}
logger.fatal(sqlException.getMessage()+" in case of insert operation for "+entityClass.getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of insert operation for "+entityClass.getSimpleName());
}
try
{
oldValue=nextValObjectPropertyGetter.invoke(object);
oldValues.add(new Pair<Object,MethodWrapper>(oldValue,nextValObjectPropertySetter));
nextValObjectPropertySetter.invoke(object,nextValResultSetGetter.invoke(nextValResultSet,1));
}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException commonException)
{
logger.fatal(commonException.getMessage()+" in case of insert operation for "+entityClass.getSimpleName());
DMFrameworkException dmFrameworkException=new DMFrameworkException(commonException.getMessage()+" in case of insert operation for "+entityClass.getSimpleName());
dmFrameworkException.setDatabaseOperationException("Cannot add "+entityClass.getSimpleName());
throw dmFrameworkException;
}
try
{
nextValResultSet.close();
nextValStatement.close();
}catch(SQLException sqlException)
{ 
// ignore 
}
} // if sequenceWrapper!=null
} // loop for number of values in insert statement ends
}
entity.validateConstraintsBeforeInsertion(connection,object);
entity.insert(connection,object);
}catch(ValidatorException validatorException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
// lot of work to be done
validatorException.setDatabaseOperationException("Cannot add "+entityClass.getSimpleName());
throw validatorException;
}
catch(DMFrameworkException dmFrameworkException)
{
try { if(this.connection!=null) { this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
if(oldValues!=null)
{
try
{
for(Pair<Object,MethodWrapper> nextValResetPair:oldValues)
{
nextValResetPair.getSecond().invoke(object,nextValResetPair.getFirst());
}
}catch(Exception exception)
{
// ignore, cannot do anything about it
}
}
dmFrameworkException.setDatabaseOperationException("Cannot add "+entityClass.getSimpleName());
throw dmFrameworkException;
}
}
public void update(Object object) throws DMFrameworkException,ValidatorException
{
if(connection==null) throw new DMFrameworkException("Call to begin of DataManager is missing");
Class entityClass=object.getClass();
try
{
Entity entity=EntityManager.get(entityClass);
entity.validateConstraintsBeforeUpdation(connection,object);
entity.update(connection,object);
}catch(ValidatorException validatorException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
// lot of work to be done
validatorException.setDatabaseOperationException("Cannot update "+entityClass.getSimpleName());
throw validatorException;
}catch(DMFrameworkException dmFrameworkException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
dmFrameworkException.setDatabaseOperationException("Cannot update "+entityClass.getSimpleName());
throw dmFrameworkException;
}
}
public void delete(Object object) throws DMFrameworkException,ValidatorException
{
if(connection==null) throw new DMFrameworkException("Call to begin of DataManager is missing");
Class entityClass=object.getClass();
try
{
Entity entity=EntityManager.get(entityClass);
entity.validateConstraintsBeforeDeleting(connection,object);
entity.delete(connection,object);
}catch(ValidatorException validatorException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
// lot of work to be done
validatorException.setDatabaseOperationException("Cannot delete : "+entityClass.getSimpleName());
throw validatorException;
}catch(DMFrameworkException dmFrameworkException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
dmFrameworkException.setDatabaseOperationException("Cannot delete : "+entityClass.getSimpleName());
throw dmFrameworkException;
}
}
public void delete(Class entityClass,Object ...primaryKey) throws DMFrameworkException,ValidatorException
{
if(connection==null) throw new DMFrameworkException("Call to begin of DataManager is missing");
try
{
Entity entity=EntityManager.get(entityClass);
entity.validateConstraintsByPrimaryKeyBeforeDeleting(connection,primaryKey);
entity.deleteByPrimaryKey(connection,primaryKey);
}catch(ValidatorException validatorException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
// lot of work to be done
validatorException.setDatabaseOperationException("Cannot delete : "+entityClass.getSimpleName());
throw validatorException;
}catch(DMFrameworkException dmFrameworkException)
{
try { if(this.connection!=null) {this.connection.close(); this.connection=null; }}catch(SQLException sqlException1) {}
dmFrameworkException.setDatabaseOperationException("Cannot delete : "+entityClass.getSimpleName());
throw dmFrameworkException;
}
}
public Connection getConnection()
{
return this.connection;
}
public void begin() throws DMFrameworkException
{
connection=DatabaseUtility.getConnection();
try
{
connection.setAutoCommit(false);
}catch(SQLException sqlException)
{
logger.warn(sqlException.getMessage());
}
}
public void end()
{
if(connection!=null)
{
try
{
connection.commit();
}catch(Exception exception)
{
logger.warn(exception.getMessage());
}
try
{
if(connection!=null)
{
connection.close();
connection=null;
}
}catch(Exception exception)
{
logger.warn(exception.getMessage());
}
}
}
}