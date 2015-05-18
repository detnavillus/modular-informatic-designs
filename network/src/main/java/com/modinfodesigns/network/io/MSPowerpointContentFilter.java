package com.modinfodesigns.network.io;

import java.io.InputStream;

public class MSPowerpointContentFilter implements IContentStreamFilter
{
  private String name;
    
  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public String getContentType()
  {
    return "application/vnd.ms-powerpoint";
  }

  @Override
  public InputStream filterInputStream(InputStream source)
  {
    return null;
  }

}
