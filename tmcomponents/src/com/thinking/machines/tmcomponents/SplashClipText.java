package com.thinking.machines.tmcomponents;
import java.awt.*;
public class SplashClipText
{
private String Text;
private Font font;
private Color color;
private int x;
private int y;
private int width;
private int height;
public SplashClipText(String text,Font font,Color color,int x,int y,int width,int height)
{
this.text=text;
this.font=font;
this.color=color;
this.x=x;
this.y=y;
this.width=width;
this.height=height;
}
public String getText()
{
return this.text;
}
public Font getFont()
{
return this.font;
}
public Color getColor()
{
return this.color;
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
