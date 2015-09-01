package com.modinfodesigns.property;

import java.util.Iterator;

public class DataObjectDelegate extends DataObject
{
  public static final String DELEGATE_NAME = "DataDelegate";
	
  private DataObject delegate;
    
  public DataObjectDelegate( DataObject delegate )
  {
    setDelegate( delegate );
  }
    
  public void setDelegate( DataObject delegate )
  {
    this.delegate = delegate;
    setProxyObject( delegate );
  }
    
  public DataObject getDelegate( )
  {
    return this.delegate;
  }
    
    // get/set LocalProperty ...
    // synchronize
    // disable proxy
    // call super.getProperty
    // reset proxy
    
    

}
