package com.thinking.machines.dmframework.query;
import com.thinking.machines.dmframework.exceptions.*;
import java.util.*;
public interface QueryImplementor<T>
{
public List<T> query() throws DMFrameworkException;
public void addExpression(Expression<T> expression);
public QueryImplementor<T> between(int x,int y) throws DMFrameworkException;
public QueryImplementor<T> orderByAscending(String... propertyNames) throws DMFrameworkException;
public QueryImplementor<T> orderByDescending(String... propertyNames) throws DMFrameworkException;
}
