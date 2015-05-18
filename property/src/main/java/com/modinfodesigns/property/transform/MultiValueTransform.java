package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.utils.StringMethods;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiValueTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( MultiValueTransform.class );

  private String delimiter = ",";
    
  private ArrayList<String> inputProperties;
    
    
  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    if (input == null) return null;
		
    LOG.debug( "transform " + input.getValue( ) );
		
    String value = input.getValue( );
    String[] values = StringMethods.getStringArray( value,  delimiter );
		
    return new StringListProperty( input.getName( ), values );
  }



  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder( ) " + inputProperties.get( 1 ) );

    if (inputProperties != null)
    {
      for (String inputPropName : inputProperties )
      {
        LOG.debug( "transforming " + inputPropName );
			
        IProperty inputProp = input.getProperty( inputPropName );
        if (inputProp != null)
        {
          IProperty outputProp = transform( inputProp );
          input.setProperty( outputProp );
        }
      }
    }

    LOG.debug( "transformPropertyHolder DONE." );
    return input;
  }

	
  public void addInputProperty( String inputProperty )
  {
    LOG.debug( "addInputProperty: " + inputProperty );
		
    if (inputProperties == null) inputProperties = new ArrayList<String>( );
    inputProperties.add( inputProperty );
  }
	
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
	
  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }
}
