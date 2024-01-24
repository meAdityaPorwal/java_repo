package com.thinking.machines.tmcomponents.events;
import com.thinking.machines.tmcomponents.*;
public class LoginActionEvent
{
private LoginDialog source;
private String username;
private String password;
public LoginActionEvent(LoginDialog source, String username,String password)
{
this.source=source;
this.username=username;
this.password=password;
}
public LoginDialog getSource()
{
return this.source;
}
public String getUsername()
{
return this.username;
}
public String getPassword()
{
return this.password;
}
}

