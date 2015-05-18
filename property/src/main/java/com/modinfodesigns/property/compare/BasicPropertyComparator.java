package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicPropertyComparator implements IPropertyComparator
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BasicPropertyComparator.class );
    
  public static String VALUE = "VALUE";
  public static String NAME  = "NAME";
	
  private String mode = VALUE;  // VALUE || NAME
	
  public BasicPropertyComparator( )
  {
		
  }
	
  public BasicPropertyComparator( String mode )
  {
    this.mode = mode;
  }

  @Override
  public int compare(IProperty prop1, IProperty prop2)
  {
    if (prop1 == null || prop2 == null)
    {
      return 0;
    }
    	
    return (mode.equals( "VALUE" )) ? prop1.getValue().compareTo( prop2.getValue( ))
                                    : prop1.getName().compareTo( prop2.getName( ));
  }
	
  public boolean equals( IProperty prop1, IProperty prop2 )
  {
    LOG.debug( prop1 + " " + prop2 );
    	
    if (prop1 == null || prop2 == null)
    {
      return false;
    }
    	
    return (mode.equals( "VALUE" )) ?  ( prop1.getValue().equals( prop2.getValue() ) )
                                    :  ( prop1.getName( ).equals( prop2.getName( )));
  }

}
