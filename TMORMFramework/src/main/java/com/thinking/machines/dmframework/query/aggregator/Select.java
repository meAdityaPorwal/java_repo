package com.thinking.machines.dmframework.query.aggregator;
import org.apache.commons.lang3.*;
import java.util.*;
import com.thinking.machines.dmframework.pojo.*;
import com.thinking.machines.dmframework.exceptions.*;
import com.thinking.machines.dmframework.query.Clause;
import com.thinking.machines.dmframework.query.Operators;
import com.thinking.machines.dmframework.query.SelectWrapper;
import com.thinking.machines.dmframework.*;
import java.lang.reflect.*;
import java.sql.*;
import org.apache.log4j.*;
public class Select<T> implements QueryImplementor<T>
{
public static final String COUNT="count";
public static final String MIN="min";
public static final String MAX="max";
public static final String SUM="sum";
public static final String AVG="avg";
public final static Logger logger=Logger.getLogger(Select.class);
private Entity entity;
String column="";
String propertyName="";
private boolean whereFlag=true;
LinkedList<Expression<T>> whereExpressions=new LinkedList<Expression<T>>();
private Connection connection;
private String aggregateFunction;
public Select(Connection connection,Entity entity,String aggregateFunction) throws DMFrameworkException
{
this.aggregateFunction=aggregateFunction;
this.entity=entity;
this.connection=connection;
this.column="*";
}
public Select(Connection connection,Entity entity,String aggregateFunction,String propertyName) throws DMFrameworkException
{
this.aggregateFunction=aggregateFunction;
this.entity=entity;
this.connection=connection;
this.propertyName=propertyName;
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
public Double query() throws DMFrameworkException
{
if(whereExpressions.size()==0) return statementQuery();
return preparedStatementQuery();
}
private Double preparedStatementQuery() throws DMFrameworkException
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
if(propertyName.length()!=0)
{
List<Column> columns=entity.getTable().getColumns();
Column col;
for(i=0;i<columns.size();i++)
{
col=columns.get(i);
if(col.getProperty().getName().equals(propertyName))
{
if(!aggregateFunction.equals(COUNT) && !isNumeric(col.getProperty().getType())) throw new DMFrameworkException("Data type of property : "+propertyName+" in case of entity : "+entity.getEntityClass().getName()+" must be numeric");
this.column=col.getName();
break;
}
}
if(i==columns.size())
{
throw new DMFrameworkException("Property name : "+propertyName+" is invalid in case of entity : "+entity.getEntityClass().getName());
}
}
//column found
String sqlStatement;
StringBuilder sb=new StringBuilder("select ");
sb.append(aggregateFunction);
sb.append("("+column+")");
sb.append(" as "+aggregateFunction);
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
PreparedStatement preparedStatement=null;
ResultSet resultSet=null;
sqlStatement=sb.toString();
try
{
System.out.println(sqlStatement);
preparedStatement=connection.prepareStatement(sqlStatement);
Method preparedStatementSetterMethod;
i=0;
while(i<preparedStatementSettersRequiredInExpressions.size())
{
expression=whereExpressions.get(i);
clause=expression.getClause();
rightOperand=clause.getRightOperand();
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
Double data;
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
//changes
resultSet.next();
data=resultSet.getDouble(aggregateFunction);
}
catch(SQLException sqlException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { preparedStatement.close(); }catch(SQLException sqlException3){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
try { preparedStatement.close(); }catch(SQLException sqlException2){}
return data;
}



private Double statementQuery() throws DMFrameworkException
{
Double data;
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
if(propertyName.length()!=0)
{
List<Column> columns=entity.getTable().getColumns();
Column col;
int i;
for(i=0;i<columns.size();i++)
{
col=columns.get(i);
if(col.getProperty().getName().equals(propertyName))
{
if(!aggregateFunction.equals(COUNT) && !isNumeric(col.getProperty().getType())) throw new DMFrameworkException("Data type of property : "+propertyName+" in case of entity : "+entity.getEntityClass().getName()+" must be numeric");
this.column=col.getName();
break;
}
}
if(i==columns.size())
{
throw new DMFrameworkException("Property name : "+propertyName+" is invalid in case of entity : "+entity.getEntityClass().getName());
}
}
//column found
String sqlStatement;
StringBuilder sb=new StringBuilder("select ");
sb.append(aggregateFunction);
sb.append("("+column+")");
sb.append(" as "+aggregateFunction);
sb.append(" from ");
sb.append(entity.getTable().getName());
if(defaultOrderBy!=null)
{
sb.append(defaultOrderBy);
}
sqlStatement=sb.toString();
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
resultSet.next();
data=resultSet.getDouble(aggregateFunction);
}
catch(SQLException sqlException)
{
try { resultSet.close(); }catch(SQLException sqlException2){}
try { statement.close(); }catch(SQLException sqlException3){}
logger.fatal(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
throw new DMFrameworkException(sqlException.getMessage()+" in case of select operation for "+entity.getEntityClass().getSimpleName());
}
try { statement.close(); }catch(SQLException sqlException2){}
return data;
}

private boolean isNumeric(Class c)
{
String className=c.getSimpleName();
if(className.equals("Integer")) return true;
if(className.equals("Long")) return true;
if(className.equals("BigDecimal")) return true;
return false;
}
}