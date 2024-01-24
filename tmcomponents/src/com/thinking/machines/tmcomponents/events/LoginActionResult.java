package com.thinking.machines.tmcomponents.events;
public class LoginActionResult
{
private boolean disposeLoginDialog;
private String error;
public LoginActionResult(boolean disposeLoginDialog)
{
this.disposeLoginDialog=disposeLoginDialog;
this.error="";
}
public LoginActionResult(boolean disposeLoginDialog,String error)
{
this.error=error;
this.disposeLoginDialog=disposeLoginDialog;
}
public String getError()
{
return this.error;
}
public boolean disposeLoginDialog()
{
return this.disposeLoginDialog;
} 
}