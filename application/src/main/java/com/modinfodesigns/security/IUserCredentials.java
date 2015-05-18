package com.modinfodesigns.security;


public interface IUserCredentials
{
  public String getUsername( );
    
  public String getPassword( );
    
  public void setSearchApplication( String searchApplicationName );
    
  public String getSearchApplication( );
}
