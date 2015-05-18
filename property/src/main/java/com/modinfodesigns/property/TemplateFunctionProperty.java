package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.PropertyTemplateTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a PropertyTemplateTransform to create a dynamic function property by using properties
 * contained in the associated IPropertyHolder.  When any of the properties listed in the
 * template change, the value retrieved by getValue( ) will also change.
 * 
 * @author Ted Sullivan
 */

public class TemplateFunctionProperty implements IFunctionProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TemplateFunctionProperty.class );

  private IPropertyHolder propHolder;
  private static final String TEMP_PROP_THIS = "tempProp";
    
  private String template;
    
  private String name;
    
  @Override
  public String getName( )
  {
    return this.name;
  }

  @Override
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public String getType( )
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue( )
  {
    IProperty outputProp = getOutputProperty( );
    return (outputProp != null) ? outputProp.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    IProperty outputProp = getOutputProperty( );
    return (outputProp != null) ? outputProp.getValue( format ) : null;
  }
	
  private IProperty getOutputProperty(  )
  {
    PropertyTemplateTransform propTempTransform = new PropertyTemplateTransform( );
    propTempTransform.setTransformTemplate( this.template );
    propTempTransform.setOutputProperty( TEMP_PROP_THIS );  // make sure not to respond to this when added
    try
    {
      propTempTransform.transformPropertyHolder( this.propHolder );
        
      IProperty tempProp = propHolder.getProperty( TEMP_PROP_THIS );
			
      propHolder.removeProperty( TEMP_PROP_THIS );
      return tempProp;
    }
    catch (PropertyTransformException pte )
    {
      LOG.error( "Got PropertyTransformException: " + pte.getMessage( ) );
    }
		
    return null;
  }

  @Override
  public void setValue( String value, String format )
  {
    this.template = value;
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    TemplateFunctionProperty copy = new TemplateFunctionProperty( );
    copy.setFunction( this.template );
    copy.setPropertyHolder( this.propHolder );
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return template;
  }

  @Override
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction( String function )
  {
    this.template = function;
  }

  @Override
  public String getFunction()
  {
    return this.template;
  }

  @Override
  public IProperty execute()
  {
    return getOutputProperty( );
  }

  @Override
  public boolean isMultiValue()
  {
    IProperty outputProp = execute( );
    return (outputProp != null) ? outputProp.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty outputProp = execute( );
    return (outputProp != null) ? outputProp.getValues( format ) : null;
  }

}
