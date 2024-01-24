package com.thinking.machines.tmcomponents;
import com.thinking.machines.tmcomponents.events.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
public class LoginDialog extends JDialog implements ActionListener,DocumentListener
{
private HashMap<LoginErrorHistory,LoginErrorHistory> history=new
HashMap<LoginErrorHistory,LoginErrorHistory>();
public static final int LOGIN_ACTION_SELECTED=1;
public static final int EXIT_ACTION_SELECTED=0;
private int selectedAction=EXIT_ACTION_SELECTED;
private ImageIcon statusBarErrorIcon;
private JLabel usernameCaption;
private JLabel passwordCaption;
private JTextField usernameTextField;
private JPasswordField passwordField;
private JButton loginButton;
private JLabel statusBarLabel;
private Container container;
private String titleText;
private String usernameCaptionText;
private String passwordCaptionText;
private String loginButtonCaptionText;
private ImageIcon dialogIcon;
private LoginActionListener loginActionListener;
private LoginDialog(String titleText,String usernameCaptionText,String passwordCaptionText,String loginButtonCaptionText,ImageIcon dialogIcon,LoginActionListener loginActionListener)
{
 this.loginActionListener=loginActionListener;
this.dialogIcon=dialogIcon;
this.titleText=titleText;
this.usernameCaptionText=usernameCaptionText;
this.passwordCaptionText=passwordCaptionText;
this.loginButtonCaptionText=loginButtonCaptionText;
setTitle(titleText);
initComponents();
addListeners();
setAppearance();
}
private void initComponents()
{
statusBarErrorIcon=new ImageIcon(getClass().getResource("/images/statusBarErrorIcon.png"));
container=getContentPane();
usernameCaption=new JLabel(usernameCaptionText);
passwordCaption=new JLabel(passwordCaptionText);
usernameTextField=new JTextField();
passwordField=new JPasswordField();
loginButton=new JButton(loginButtonCaptionText);
statusBarLabel=new JLabel("");
setDefaultCloseOperation(HIDE_ON_CLOSE);
setAlwaysOnTop(true);
}
private void setAppearance()
{
setModal(true);
loginButton.setEnabled(false);
setIconImage(dialogIcon.getImage());
setResizable(false);
Font dataFont=new Font("Verdana",Font.PLAIN,14);
Font statusBarFont=new Font("Ariel",Font.BOLD,12);
container.setLayout(null);
int lm,tm;
lm=13;
tm=13;
usernameCaption.setFont(dataFont);
usernameTextField.setFont(dataFont);
passwordCaption.setFont(dataFont);
passwordField.setFont(dataFont);
loginButton.setFont(dataFont);
statusBarLabel.setFont(statusBarFont);
usernameCaption.setBounds(lm+0,tm+0,70,30);
usernameTextField.setBounds(lm+0+70+5,tm+0,130,30);
passwordCaption.setBounds(lm+0,tm+0+30+10,70,30);
passwordField.setBounds(lm+0+70+5,tm+0+30+10,130,30);
loginButton.setBounds(125-40,tm+0+30+10+30+10,80,30);
statusBarLabel.setBounds(1,tm+0+30+10+30+10+30+10+2,242,30);
statusBarLabel.setBackground(new Color(212,212,212));
statusBarLabel.setOpaque(true);
container.add(usernameCaption);
container.add(usernameTextField);
container.add(passwordCaption);
container.add(passwordField);
container.add(loginButton);
container.add(statusBarLabel);
setSize(250,200);
Dimension dimension=Toolkit.getDefaultToolkit().getScreenSize();
setLocation(dimension.width/2-getWidth()/2,dimension.height/2-getHeight()/2);
}
private void addListeners()
{
 loginButton.addActionListener(this);
usernameTextField.getDocument().addDocumentListener(this);
passwordField.getDocument().addDocumentListener(this);
}
public void actionPerformed(ActionEvent ev)
{
 loginButton.setEnabled(false);
if(loginActionListener!=null)
{
LoginActionEvent loginActionEvent;
loginActionEvent=new LoginActionEvent(this,this.getUsername(),this.getPassword());
LoginActionResult loginActionResult;
loginActionResult=loginActionListener.loginActionPerformed(loginActionEvent);
if(loginActionResult.resetUsername())
{
usernameTextField.setText("");
passwordField.setText("");
}
 else
{
 if(loginActionResult.resetPassword())
{
passwordField.setText("");
}
}
 if(loginActionResult.disposeLoginDialog()==false)
{
statusBarLabel.setText(loginActionResult.getError());
statusBarLabel.setIcon(statusBarErrorIcon);
LoginErrorHistory loginErrorHistory;
loginErrorHistory=new LoginErrorHistory(this.getUsername(),this.getPassword(),loginActionResult.getError());
history.put(loginErrorHistory,loginErrorHistory);
}
 else
{
selectedAction=LOGIN_ACTION_SELECTED;
setVisible(false);
loginButton.setEnabled(true);
}
}
}
 public void insertUpdate(javax.swing.event.DocumentEvent ev)
{
 enableDisableLoginButton();
}
public void removeUpdate(javax.swing.event.DocumentEvent ev)
{
 enableDisableLoginButton();
}
public void changedUpdate(javax.swing.event.DocumentEvent ev)
{
 enableDisableLoginButton();
}
private void enableDisableLoginButton()
{
 if(usernameTextField.getText().trim().length()==0|| new String(passwordField.getPassword()).trim().length()==0)
{
 loginButton.setEnabled(false);
statusBarLabel.setText("");
statusBarLabel.setIcon(null);
}
 else
{
LoginErrorHistory loginErrorHistory;
loginErrorHistory=history.get(new LoginErrorHistory(this.getUsername(),this.getPassword(),""));
if(loginErrorHistory!=null)
{
statusBarLabel.setText(loginErrorHistory.errorMessage);
statusBarLabel.setIcon(statusBarErrorIcon);
loginButton.setEnabled(false);
}
 else
{
statusBarLabel.setText("");
statusBarLabel.setIcon(null);
loginButton.setEnabled(true);
}
}
}
public String getUsername()
{
return this.usernameTextField.getText().trim();
}
public String getPassword()
{
return new String(this.passwordField.getPassword()).trim();
}
public void resetHistory()
{
history.clear();
}
static public int showLoginDialog(String title,String usernameCaption,String passwordCaption,String loginButtonCaption,ImageIcon dialogIcon,LoginActionListener loginActionListener)
{
LoginDialog loginDialog;
loginDialog=new LoginDialog(title,usernameCaption,passwordCaption,loginButtonCaption,dialogIcon,loginActionListener);
loginDialog.setVisible(true);
System.out.println("Great");
return loginDialog.selectedAction;
} // inner class
class LoginErrorHistory implements Comparable<LoginErrorHistory>
{
public String username;
public String password;
public String errorMessage;
LoginErrorHistory(String username,String password,String errorMessage)
{
 this.username=username;
this.password=password;
this.errorMessage=errorMessage;
}
public boolean equals(Object object)
{
 if(!(object instanceof LoginErrorHistory)) return false;
LoginErrorHistory loginErrorHistory=(LoginErrorHistory)object;
return this.username.equals(loginErrorHistory.username) && this.password.equals(loginErrorHistory.password);
}
public int compareTo(LoginErrorHistory loginErrorHistory)
{
 int x=this.username.compareTo(loginErrorHistory.username);
if(x!=0) return x;
else return this.password.compareTo(loginErrorHistory.password);
}
public int hashCode()
{
return java.util.Objects.hash(this.username,this.password);
}
}
}
