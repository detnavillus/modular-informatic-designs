package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches a Property value with a compare String.
 * 
 * @author Ted Sullivan
 */

// To Do: Add Regular Expression Support

public class StringPropertyMatcher implements IPropertyMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( StringPropertyMatcher.class );

  private String compareValue;
    
  private String mode;  // CONTAINS | EQUALS | STARTS_WITH | ENDS_WITH
    
  private boolean caseSensitive = false;
    
    
  public void setValue( String compareValue )
  {
    this.compareValue = compareValue;
  }
    
  public void setMode( String compareMode )
  {
    this.mode = compareMode;
  }
    
  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
  }
    
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    LOG.debug( "equals " + property );
    	
    if (property == null) return false;
    	
    if (property instanceof StringProperty)
    {
      String propValue = ((StringProperty)property).getValue();
      LOG.debug( "Prop value = '" + propValue + "'" );
      LOG.debug( "compareValue = '" + compareValue + "'" );
    		
      if (mode.equals( "EQUALS" ))
      {
        return (caseSensitive) ? propValue.equals( compareValue ) : propValue.equalsIgnoreCase( compareValue );
      }
      else if (mode.equals( "CONTAINS" ))
      {
        if (caseSensitive)
        {
          return (propValue.indexOf( compareValue ) >= 0);
        }
        else
        {
          return (propValue.toLowerCase().indexOf( compareValue.toLowerCase() ) >= 0);
        }
      }
      else if (mode.equals( "STARTS_WITH" ))
      {
        if (caseSensitive)
        {
          return propValue.startsWith( compareValue );
        }
        else
        {
          return propValue.toLowerCase().startsWith( compareValue.toLowerCase() );
        }
      }
      else if (mode.equals( "ENDS_WITH" ))
      {
        if (caseSensitive)
        {
          return propValue.endsWith( compareValue );
        }
        else
        {
          return propValue.toLowerCase().endsWith( compareValue.toLowerCase( ) );
        }
      }
    }
    	
    return false;
  }
	
}
