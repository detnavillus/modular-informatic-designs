package com.modinfodesigns.property.compare;

import java.util.Iterator;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches an IPropertyHolder if it has a property with a given name or name pattern.
 * 
 * @author Ted Sullivan
 */

// To Do:  Add Regular Expression support

public class HasPropertyMatcher implements IPropertyHolderMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( HasPropertyMatcher.class );

  private String compareTo;
    
  private String mode = "EQUALS";  // EQUALS | CONTAINS | STARTS_WITH | ENDS_WITH
    
  private boolean caseSensitive = false;
    
  private IPropertyMatcher propMatcher;
    
  public void setPropertyName( String propertyName )
  {
    this.compareTo = propertyName;
  }
    
  public void setMode( String mode )
  {
    this.mode = mode;
  }
    
  public void setPropertyMatcher( IPropertyMatcher propMatcher )
  {
    this.propMatcher = propMatcher;
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (property instanceof IPropertyHolder)
    {
      return equals( user, (IPropertyHolder)property );
    }

    if (propMatcher == null)
    {
      return doesMatch( property.getName( ) );
    }
    else
    {
      return propMatcher.equals( user, property );
    }
  }

  @Override
  public boolean equals( IUserCredentials user, IPropertyHolder propHolder )
  {
    if (mode.equals( "EQUALS" ))
    {
      LOG.debug( "has property " + compareTo + " = " +
                                (propHolder != null && propHolder.getProperty( compareTo ) != null ));
      return (propHolder != null && propHolder.getProperty( compareTo ) != null);
    }
    else
    {
      for (Iterator<String> sit = propHolder.getPropertyNames(); sit.hasNext(); )
      {
        String propName = sit.next();
        if (propMatcher == null)
        {
          if (doesMatch( propName )) return true;
        }
        else
        {
          IProperty theProp = propHolder.getProperty( propName );
          if (propMatcher.equals( user, theProp )) return true;
        }
      }
    }
		
    return false;
  }
	
  private boolean doesMatch( String propName )
  {
    return PropertyNameMatcher.equals( propName, compareTo, mode, caseSensitive);
  }
}
