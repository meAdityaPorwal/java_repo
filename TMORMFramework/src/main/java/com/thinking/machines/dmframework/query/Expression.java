package com.thinking.machines.dmframework.query;
import com.thinking.machines.dmframework.exceptions.*;
import java.util.*;
public class Expression<T>
{
private QueryImplementor<T> queryImplementor;
private Clause clause;
private String logicalOperator;
public Expression(QueryImplementor<T> queryImplementor,Clause clause)
{
this.queryImplementor=queryImplementor;
this.clause=clause;
this.logicalOperator=null;
}
public Expression(QueryImplementor<T> queryImplementor,String logicalOperator,Clause clause)
{
this.queryImplementor=queryImplementor;
this.clause=clause;
this.logicalOperator=logicalOperator;
}
public LogicalOperator<T> and(String leftOperand)
{
queryImplementor.addExpression(this);
Clause clause=new Clause();
clause.setLeftOperand(leftOperand);
LogicalOperator<T> logicalOperator;
logicalOperator=new LogicalOperator<T>(queryImplementor,"and",clause);
return logicalOperator;
}
public LogicalOperator<T> or(String leftOperand)
{
queryImplementor.addExpression(this);
Clause clause=new Clause();
clause.setLeftOperand(leftOperand);
LogicalOperator<T> logicalOperator;
logicalOperator=new LogicalOperator<T>(queryImplementor,"or",clause);
return logicalOperator;
}
public String getLogicalOperator()
{
return this.logicalOperator;
}
public List<T> query() throws DMFrameworkException
{
queryImplementor.addExpression(this);
return queryImplementor.query();
}
public QueryImplementor<T> between(int x,int y) throws DMFrameworkException
{
queryImplementor.addExpression(this);
return queryImplementor.between(x,y);
}

public QueryImplementor<T> orderByAscending(String... propertyNames) throws DMFrameworkException
{
queryImplementor.addExpression(this);
return queryImplementor.orderByAscending(propertyNames);
}

public QueryImplementor<T> orderByDescending(String... propertyNames) throws DMFrameworkException
{
queryImplementor.addExpression(this);
return queryImplementor.orderByDescending(propertyNames);
} 
 

public Clause getClause()
{
return this.clause;
}
}
