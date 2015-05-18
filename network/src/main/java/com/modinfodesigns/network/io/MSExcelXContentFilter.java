package com.modinfodesigns.network.io;

import java.io.InputStream;

public class MSExcelXContentFilter implements IContentStreamFilter
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
    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  }

  @Override
  public InputStream filterInputStream( InputStream source )
  {
    return null;
  }

}
