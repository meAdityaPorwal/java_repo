package com.thinking.machines.inventory.servlets;
import java.io.*;
import com.thinking.machines.inventory.dl.interfaces.*;
import com.thinking.machines.inventory.dl.exception.*;
import com.thinking.machines.inventory.dl.*;
import javax.servlet.*;
import javax.servlet.http.*;
public class AddItem extends HttpServlet
{
public void doGet(HttpServletRequest request,HttpServletResponse response)
{
System.out.println("add s phle");
String name=request.getParameter("name");
String category=request.getParameter("category");
int price=Integer.parseInt(request.getParameter("price"));
ItemDAOInterface itemDAOInterface=new ItemDAO();
ItemDTOInterface itemDTOInterface=new ItemDTO();
itemDTOInterface.setName(name);
itemDTOInterface.setCategory(category);
itemDTOInterface.setPrice(price);
PrintWriter pw=null;
try
{
itemDAOInterface.add(itemDTOInterface);
try
{
pw=response.getWriter();
response.setContentType("application/json");
}catch(Exception e)
{
}
pw.print("{\"success\" : true, \"code\" : \""+itemDTOInterface.getCode()+"\"}");
System.out.println("add s phle");
}catch(DAOException daoException)
{
try{
pw=response.getWriter();
response.setContentType("application/json");
}catch(Exception e)
{
}
pw.print("{\"success\" : false, \" exception \" : \""+daoException.getMessage()+"\"}");
}
}
public void doPost(HttpServletRequest request,HttpServletResponse response)
{
this.doGet(request,response);
}
}