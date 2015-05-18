package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.security.IUserCredentials;

public class PropertyNameMatcher implements IPropertyMatcher
{
  private String name;
	
  private String mode = "EQUALS"; // EQUALS | CONTAINS | STARTS_WITH | ENDS_WITH
	
  private boolean caseSensitive = false;
	 
  public PropertyNameMatcher( ) {  }
	
  public PropertyNameMatcher( String name, String mode, boolean caseSensitive )
  {
    this.name = name;
    this.mode = mode;
    this.caseSensitive = caseSensitive;
  }
	
  public void setName( String name )
  {
    this.name = name;
  }
	
  public void setMode( String mode )
  {
    this.mode = mode;
  }
	
  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
  }
	
  public void setCaseSensitive( String caseSensitive )
  {
    this.caseSensitive = (caseSensitive != null && caseSensitive.equalsIgnoreCase( "true" ));
  }


  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (property == null) return false;

    String propName = property.getName( );
    if (propName == null) return false;
		
    return equals( propName, this.name, this.mode, this.caseSensitive );
  }
	
  public static boolean equals( String propName, String name, String mode, boolean caseSensitive )
  {
    if (mode.equals( "EQUALS" ))
    {
      return (caseSensitive) ? propName.equals( name ) : propName.equalsIgnoreCase( name );
    }
    else if (mode.equals( "CONTAINS" ))
    {
      if (caseSensitive)
      {
        return (propName.indexOf( name ) >= 0);
      }
      else
      {
        return (propName.toLowerCase().indexOf( name.toLowerCase() ) >= 0);
      }
    }
    else if (mode.equals( "STARTS_WITH" ))
    {
      if (caseSensitive)
      {
        return propName.startsWith( name );
      }
      else
      {
        return propName.toLowerCase().startsWith( name.toLowerCase() );
      }
    }
    else if (mode.equals( "ENDS_WITH" ))
    {
      if (caseSensitive)
      {
        return propName.endsWith( name );
      }
      else
      {
        return propName.toLowerCase().endsWith( name.toLowerCase( ) );
      }
    }
   		
    return false;
  }

}
