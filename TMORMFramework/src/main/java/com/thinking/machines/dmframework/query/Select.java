package com.thinking.machines.dmframework.query;
import org.apache.commons.lang3.*;
import java.util.*;
import com.thinking.machines.dmframework.pojo.*;
import com.thinking.machines.dmframework.exceptions.*;
import com.thinking.machines.dmframework.*;
import java.lang.reflect.*;
import java.sql.*;
import org.apache.log4j.*;
public class Select<T> implements QueryImplementor<T>
{
public final static Logger logger=Logger.getLogger(com.thinking.machines.dmframework.query.Select.class);
private Entity entity;
String columns="*";
private boolean whereFlag=true;
private boolean limitFlag;
private int startLimit;
private int endLimit;
private boolean orderByFlag;
private StringBuilder orderByString=new StringBuilder("");
private StringBuilder parameterString=new StringBuilder("");
LinkedList<Expression<T>> whereExpressions=new LinkedList<Expression<T>>();
private Connection connection;
public Select(Connection connection,Entity entity) throws DMFrameworkException
{
this.entity=entity;
this.connection=connection;
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> count() throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.COUNT);
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> count(String propertyName) throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.COUNT,propertyName);
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> sum(String propertyName) throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.SUM,propertyName);
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> avg(String propertyName) throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.AVG,propertyName);
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> min(String propertyName) throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.MIN,propertyName);
}

public com.thinking.machines.dmframework.query.aggregator.Select<T> max(String propertyName) throws DMFrameworkException
{
return new com.thinking.machines.dmframework.query.aggregator.Select<T>(connection,entity,com.thinking.machines.dmframework.query.aggregator.Select.MAX,propertyName);
}

public com.thinking.machines.dmframework.query.Select<T> between(int x,int y) throws DMFrameworkException
{
if(x<=0 || y<=0) throw new DMFrameworkException("Arguments of between() cannot be less than 1");
limitFlag=true;
startLimit=x-1;
endLimit=y-startLimit;
return this;
}

public com.thinking.machines.dmframework.query.Select<T> orderByAscending(String... propertyNames) throws DMFrameworkException
{
if(propertyNames.length==0) throw new DMFrameworkException("Atleast one argument must be provided with an order by clause");
if(orderByFlag==false) orderByString.append(" order by ");
orderByFlag=true;
String propertyName;
String columnName;
int e=0;
List<Column> columns=entity.getTable().getColumns();
while(e<propertyNames.length)
{
propertyName=propertyNames[e];
if(parameterString.indexOf(propertyName+",")!=-1) throw new DMFrameworkException(propertyName+" already used with an order by clause");
parameterString.append(propertyName+",");
Column col;
int i;
for(i=0;i<columns.size();i++)
{
col=columns.get(i);
if(col.getProperty().getName().equals(propertyName))
{
columnName=col.getName();
orderByString.append(columnName+" asc , ");
break;
}
}
if(i==columns.size())
{
String className="";
try
{
className=entity.getEntityClass().getName();
}catch(Exception ex){}
throw new DMFrameworkException("Property name : "+propertyName+" is invalid in case of entity : "+className);
}
e++;
}
return this;
}

public com.thinking.machines.dmframework.query.Select<T> orderByDescending(String... propertyNames) throws DMFrameworkException
{
if(propertyNames.length==0) throw new DMFrameworkException("Atleast one argument must be provided with an order by clause");
if(orderByFlag==false) orderByString.append(" order by ");
orderByFlag=true;
String propertyName;
String columnName;
int e=0;
List<Column> columns=entity.getTable().getColumns();
while(e<propertyNames.length)
{
propertyName=propertyNames[e];
if(parameterString.indexOf(propertyName+",")!=-1) throw new DMFrameworkException(propertyName+" already used with an order by clause");
parameterString.append(propertyName+",");
Column col;
int i;
for(i=0;i<columns.size();i++)
{
col=columns.get(i);
if(col.getProperty().getName().equals(propertyName))
{
columnName=col.getName();
orderByString.append(columnName+" desc ,");
break;
}
}
if(i==columns.size())
{
String className="";
try
{
className=entity.getEntityClass().getName();
}catch(Exception ex){}
throw new DMFrameworkException("Property name : "+propertyName+" is invalid in case of entity : "+className);
}
e++;
}
return this;
}


public Where<T> where(String propertyName)
{
Clause clause=new Clause();
clause.setLeftOperand(propertyName);
Where<T> where=new Where<T>(this,clause);
return where;
}
public void addExpression(Expression<T> expression)
{
if(whereFlag)
{
whereExpressions.add(expression);
}
}
public List<T> query() throws DMFrameworkException
{
if(whereExpressions.size()==0) return statementQuery();
return preparedStatementQuery();
}


private List<T> preparedStatementQuery() throws DMFrameworkException
{
SelectWrapper selectWrapper=entity.getSelectWrapper();
String defaultOrderBy=selectWrapper.getDefaultOrderBy();
ArrayList<MethodWrapper> setterMethods=selectWrapper.getSetterMethods();
int i;
i=0;
MethodWrapper methodWrapper;
Expression expression;
String leftOperand;
Object rightOperand;
Class rightOperandClass;
Class methodParameterSetterClass;
Clause clause;
ArrayList<MethodWrapper> methodWrappersInExpressions=new ArrayList<MethodWrapper>();
ArrayList<Method> preparedStatementSettersRequiredInExpressions=new ArrayList<Method>();
Method preparedStatementSetter;
boolean applyPadding;
while(i<whereExpressions.size())
{
expression=whereExpressions.get(i);
clause=expression.getClause();
leftOperand=clause.getLeftOperand();
rightOperand=clause.getRightOperand();
methodWrapper=selectWrapper.getMethodWrapperByProperty(leftOperand);
if(methodWrapper==null)
{
throw new DMFrameworkException("Property name : "+leftOperand+" is invalid in case of entity : "+entity.getEntityClass().getName());
}
if(rightOperand==null)
{
String op=Operators.getOperator(clause.getOperator());
if(!(op.equals("=") || op.equals("<>"))) throw new DMFrameworkException("null can only be used with methods eq() or ne()");
methodWrappersInExpressions.add(methodWrapper);
i++;
continue;
}
rightOperandClass=rightOperand.getClass();
applyPadding=methodWrapper.getColumn().applyPadding();
if(applyPadding)
{
clause.setRightOperand(StringUtils.rightPad((String)rightOperand,methodWrapper.getColumn().getWidth()));
}
methodParameterSetterClass=methodWrapper.getMethod().getParameterTypes()[0];
if(methodParameterSetterClass.equals(rightOperandClass)==false)
{
throw new DMFrameworkException("Value for property "+leftOperand+" is of invalid type : "+rightOperandClass.getName()+", whereas required type is : "+methodParameterSetterClass.getName());
}
methodWrappersInExpressions.add(methodWrapper);
preparedStatementSetter=selectWrapper.getPreparedStatementSetterMethod(leftOperand);
preparedStatementSettersRequiredInExpressions.add(preparedStatementSetter);
i++;
}

// verified that every leftOperand used in the expression exists as a property
// verified that the right operand is of appropriate type
String sqlStatement;
StringBuilder sb=new StringBuilder("select ");
sb.append(columns);
sb.append(" from ");
sb.append(entity.getTable().getName());
sb.append(" where ");
i=0;
String logicalOperator;
while(i<whereExpressions.size())
{
expression=whereExpressions.get(i);
clause=expression.getClause();
logicalOperator=expression.getLogicalOperator();
if(logicalOperator!=null)
{
sb.append(" ");
sb.append(logicalOperator);
sb.append(" ");
}
leftOperand=clause.getLeftOperand();
rightOperand=clause.getRightOperand();
sb.append(methodWrappersInExpressions.get(i).getColumn().getName());
if(rightOperand==null)
{
if(Operators.getOperator(clause.getOperator()).equals("=")) sb.append(" is null");
else sb.append(" is not null");
i++;
continue;
}
sb.append(" ");
sb.append(Operators.getOperator(clause.getOperator()));
sb.append(" ");
sb.append("?");
i++;
}
if(defaultOrderBy!=null)
{
sb.append(defaultOrderBy);
}
if(orderByFlag!=false)
{
sb.append(orderByString.substring(0,orderByString.length()-2));
}
if(limitFlag!=false)
{
sb.append(" limit "+startLimit+","+endLimit);
}
PreparedStatement preparedStatement=null;
ResultSet resultSet=null;
sqlStatement=sb.toString();
try
{
System.out.println(sqlStatement);
preparedStatement=connection.prepareStatement(sqlStatement);
Method preparedStatementSetterMethod;
i=0;
int expressionIndex=0;
int j;
while(i<preparedStatementSettersRequiredInExpressions.size())
{
for(j=expressionIndex;;j++)
{
expression=whereExpressions.get(j);
clause=expression.getClause();
rightOperand=clause.getRightOperand();
if(rightOperand==null) continue;
break;
}
expressionIndex=j+1;
preparedStatementSetterMethod=preparedStatementSettersRequiredInExpressions.get(i);
try
{
preparedStatementSetterMethod.invoke(preparedStatement,i+1,rightOperand);
}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException commonException)
{
logger.fatal(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
i++;
}
}catch(SQLException sqlException)
{
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
List<T> list=new LinkedList<T>();
try
{
resultSet=preparedStatement.executeQuery();
}catch(SQLException sqlException)
{
try{ preparedStatement.close(); }catch(SQLException sqlException2){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
try
{
ArrayList<Method> sqlGetterMethods=selectWrapper.getSQLGetterMethods();
Class entityClass=entity.getEntityClass();
T object;
MethodWrapper setterMethodWrapper;
Method getterMethod;
while(resultSet.next())
{
try
{
object=(T)entityClass.newInstance();
for(i=0;i<setterMethods.size();i++)
{
setterMethodWrapper=setterMethods.get(i);
getterMethod=sqlGetterMethods.get(i);
setterMethodWrapper.invoke(object,getterMethod.invoke(resultSet,setterMethodWrapper.getColumn().getName()));
}
list.add(object);
}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException commonException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { preparedStatement.close(); }catch(SQLException sqlException3){}
logger.fatal(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
}
}catch(SQLException sqlException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { preparedStatement.close(); }catch(SQLException sqlException3){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
try { preparedStatement.close(); }catch(SQLException sqlException2){}
return list;
}
private List<T> statementQuery() throws DMFrameworkException
{
List<T> list=new LinkedList<T>();
Statement statement=null;
ResultSet resultSet=null;
try
{
statement=connection.createStatement();
}catch(SQLException sqlException)
{
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
SelectWrapper selectWrapper=entity.getSelectWrapper();
String defaultOrderBy=selectWrapper.getDefaultOrderBy();
try
{
String sqlStatement;
sqlStatement=selectWrapper.getSQLStatement();
if(defaultOrderBy!=null)
{
sqlStatement=sqlStatement+defaultOrderBy;
}
if(orderByFlag!=false)
{
sqlStatement=sqlStatement+orderByString.substring(0,orderByString.length()-2);
}
if(limitFlag!=false)
{
sqlStatement+=" limit "+startLimit+","+endLimit;
}
System.out.println(sqlStatement);
resultSet=statement.executeQuery(sqlStatement);
}catch(SQLException sqlException)
{
try{ statement.close(); }catch(SQLException sqlException2){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
try
{
ArrayList<MethodWrapper> setterMethods=selectWrapper.getSetterMethods();
ArrayList<Method> sqlGetterMethods=selectWrapper.getSQLGetterMethods();
Class entityClass=entity.getEntityClass();
T object;
MethodWrapper setterMethodWrapper;
Method getterMethod;
while(resultSet.next())
{
try
{
object=(T)entityClass.newInstance();
for(int i=0;i<setterMethods.size();i++)
{
setterMethodWrapper=setterMethods.get(i);
getterMethod=sqlGetterMethods.get(i);
setterMethodWrapper.invoke(object,getterMethod.invoke(resultSet,setterMethodWrapper.getColumn().getName()));
}
list.add(object);
}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException commonException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { statement.close(); }catch(SQLException sqlException3){}
logger.fatal(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(commonException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
}
}catch(SQLException sqlException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { statement.close(); }catch(SQLException sqlException3){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of operation for "+entity.getEntityClass().getSimpleName());
}
try { statement.close(); }catch(SQLException sqlException2){}
return list;
}
}