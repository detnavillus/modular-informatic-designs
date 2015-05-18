package com.modinfodesigns.app;

import java.util.List;

public interface IObjectFactory
{
  /**
   * Initialize the Object Factory with XML (or JSON) String
   *
   * @param configStr   Configuration String
   */
  public void initialize( String configStr );
	
  public Object getApplicationObject( String name, String type );
    
  public List<Object> getApplicationObjects( String type );
    
  public List<String> getApplicationObjectNames( String type );
}
