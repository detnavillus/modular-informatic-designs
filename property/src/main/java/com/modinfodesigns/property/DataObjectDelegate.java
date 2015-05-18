package com.modinfodesigns.property;

public class DataObjectDelegate extends DataObject
{
  public static final String DELEGATE_NAME = "DataDelegate";
	
  private DataObject delegate;
    
  public void setDelegate( DataObject delegate )
  {
    this.delegate = delegate;
  }
    
  public DataObject getDelegate( )
  {
    return this.delegate;
  }
}
