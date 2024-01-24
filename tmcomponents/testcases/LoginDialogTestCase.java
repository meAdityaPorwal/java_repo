import com.thinking.machines.tmcomponents.*;
import javax.swing.*;
class LoginDialogTestCase
{
public static void main(String g[])
{
int x=LoginDialog.showLoginDialog("AAA","BBBB","CCCC","DDDD",new ImageIcon("..\\..\\GenericSocketServer\\resources\\images\\appIcon.png"));
if(x==LoginDialog.LOGIN_ACTION_SELECTED)
{
System.out.println("Process to login starts");
}
if(x==LoginDialog.EXIT_ON_CLOSE)
{
System.out.println("Existing");
System.exit(0);
}
System.out.println("Ujjain");
}
}