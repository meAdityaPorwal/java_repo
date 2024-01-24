package com.thinking.machines.dmframework.query.aggregator;
import com.thinking.machines.dmframework.exceptions.*;
import java.util.*;
public interface QueryImplementor<T>
{
public Double query() throws DMFrameworkException;
public void addExpression(Expression<T> expression);
}
