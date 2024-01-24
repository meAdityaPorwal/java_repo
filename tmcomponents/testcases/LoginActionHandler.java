import com.thinking.machines.tmcomponents.*;
import com.thinking.machines.tmcomponents.events.*;
import javax.swing.*;
class LoginActionHandler implements LoginActionListener
{
LoginActionHandler()
{
}
public LoginActionResult loginActionPerformed(LoginActionEvent ev)
{
LoginActionResult loginActionResult;
String username=ev.getUsername();
String password=ev.getPassword();
if(username.equals("UJJAIN") && password.equals("UJJAIN"))
{
 loginActionResult=new LoginActionResult(true);
}
 else
{
 loginActionResult=new LoginActionResult(false,"Invalid username/password");
//loginActionResult.resetUsername(true);
}
return loginActionResult;
}
}
 class LoginDialogTestCase
{
public static void main(String gg[])
{
 int x=LoginDialog.showLoginDialog("AAA","BBBB","CCCC","DDDD",new ImageIcon("..\\..\\GenericSocketServer\\resources\\images\\appIcon.png"),new LoginActionHandler());
if(x==LoginDialog.LOGIN_ACTION_SELECTED)
{
System.out.println("Process to login starts");
}
 if(x==LoginDialog.EXIT_ACTION_SELECTED)
{
System.out.println("Exiting ");
System.exit(0);
}
System.out.println("Ujjain");
}
}