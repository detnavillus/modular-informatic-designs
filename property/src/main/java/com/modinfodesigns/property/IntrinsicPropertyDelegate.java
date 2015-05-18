package com.modinfodesigns.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntrinsicPropertyDelegate implements IProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IntrinsicPropertyDelegate.class );

  private String name;
  private String intrinsicProperty;
    
  private IComputableProperties delegate;
    
  public IntrinsicPropertyDelegate( String name, String intrinsicProperty, IComputableProperties delegate )
  {
    LOG.debug( "IntrinsicPropertyDelegate( " + name + "," + intrinsicProperty + " )" );
    	
    this.name = name;
    this.intrinsicProperty = intrinsicProperty;
    this.delegate = delegate;
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getType( ) : null;
  }

  @Override
  public String getValue()
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getValue( ) : "";
  }

  @Override
  public String getValue(String format)
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getValue( format ) : "";
  }

  @Override
  public void setValue(String value, String format)
                       throws PropertyValidationException
  {
    // should throw here???
  }

  @Override
  public String getDefaultFormat()
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getDefaultFormat(  ) : null;
  }

  @Override
  public IProperty copy()
  {
    IntrinsicPropertyDelegate ipd = new IntrinsicPropertyDelegate( this.name, this.intrinsicProperty, delegate );
    return ipd;
  }

  @Override
  public Object getValueObject()
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getValueObject( ) : null;
  }

  private IProperty getDelegateProperty(  )
  {
    return delegate.getIntrinsicProperty( intrinsicProperty );
  }

  @Override
  public boolean isMultiValue()
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty delegateProp = getDelegateProperty( );
    return (delegateProp != null) ? delegateProp.getValues( format ) : null;
  }

}
