package com.thinking.machines.dmframework.query;
import java.util.*;
public class Operators
{
private Operators(){};
private static HashMap<Integer,String> operators=new HashMap<Integer,String>();
static
{
operators.put(1,">");
operators.put(2,"<");
operators.put(3,">=");
operators.put(4,"<=");
operators.put(5,"=");
operators.put(6,"<>");
operators.put(7,"like");
}
public static final Integer gt=1;	// >
public static final Integer lt=2;	// <
public static final Integer ge=3;	// >=
public static final Integer le=4;	// <=
public static final Integer eq=5;	// =
public static final Integer ne=6;    // <>
public static final Integer like=7;
public static String getOperator(Integer operator)
{
return operators.get(operator);
}
}