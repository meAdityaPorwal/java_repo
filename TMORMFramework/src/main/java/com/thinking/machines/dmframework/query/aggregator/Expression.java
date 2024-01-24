package com.thinking.machines.dmframework.query.aggregator;
import com.thinking.machines.dmframework.exceptions.*;
import com.thinking.machines.dmframework.query.Clause;
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
public Double query() throws DMFrameworkException
{
queryImplementor.addExpression(this);
return queryImplementor.query();
}
public Clause getClause()
{
return this.clause;
}
}
