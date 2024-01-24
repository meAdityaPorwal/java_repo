package com.thinking.machines.tmcomponents;
public class SplashClip
{
private SplashClipText splashClipText;
private SplashClipImage splashClipImage;
public SplashClip(SplashClipText splashClipText,SplashClipImage splashClipImage)
{
this.splishClipText=splashClipText;
ths.splashClipImage=splashClipImage;
}
public SplashClip(SplashClipText getSplashClipText)
{
this.splashClipText=splashClipText;
}
public SplashClip(SplashClipImage splashClipImage)
{
this.splashClipImage=splashClipImage;
}
public SplashClipText getSplashClipText()
{
return this.splashClipText;
}
public SplashClipImage getSplashClipImage()
{
return this.splashClipImage;
}
public boolean containsText()
{
return this.splashClipText!=null;
}
public boolean containsImage()
{
return this.splashClipImage!=null;
}
}

