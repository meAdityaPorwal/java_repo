package com.thinking.machines.tmcomponents;
import javax.swing.*;
public class SplashClipImage
{
private ImageIcon imageIcon;
private int x;
private int y;
private int width;
private int height;
public SplashClipImage(ImageIcon imageIcon,int x,int y,int width,int height)
{
this.imageIcon=imageIcon;
this.x=x;
this.y=y;
this.width=width;
this.height=height;
}
public ImageIcon getImageIcon()
{
return this.imageIcon;
}
public int getX()
{
return this.x;
}
public int getY()
{
return this.y;
}
public int getWidth()
{
return this.width;
}
public int getHeight()
{
return this.height;
}
}
