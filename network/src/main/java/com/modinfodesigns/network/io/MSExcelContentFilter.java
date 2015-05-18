package com.modinfodesigns.network.io;

import java.io.InputStream;

public class MSExcelContentFilter implements IContentStreamFilter
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
    return "application/vnd.ms-excel";
  }

  @Override
  public InputStream filterInputStream( InputStream source )
  {
    return null;
  }

}
