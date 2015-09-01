package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies one or more transforms to a nested property. 
 * 
 * @author Ted Sullivan
 */
public class NestedPropertyTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NestedPropertyTransform.class );
    
  private String nestedProperty;  // nested IPropertyHolder
	
  private ArrayList<IPropertyTransform> propTransforms;
	
  public void setNestedProperty( String nestedPropertyName )
  {
    this.nestedProperty = nestedPropertyName;
  }
	
  public void addPropertyTransform( IPropertyTransform propTransform )
  {
    if (propTransforms == null) propTransforms = new ArrayList<IPropertyTransform>( );
    propTransforms.add( propTransform );
  }

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    LOG.debug( "transform " + input.getValue( IProperty.JSON_FORMAT ) );
      
    if (propTransforms == null) return input;
		
    if (input instanceof IPropertyHolder)
    {
      return transformPropertyHolder( (IPropertyHolder) input );
    }
		
    return input;
  }

  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
			throws PropertyTransformException
  {

  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder " + input.getValue( IProperty.JSON_FORMAT ) );
      
    IProperty nestedProp = input.getProperty( nestedProperty );
    if ( nestedProp != null )
    {
      IProperty output = nestedProp;
      for (int i = 0; i < propTransforms.size(); i++)
      {
        IPropertyTransform xForm = propTransforms.get( i );
        output = xForm.transform( output );
      }
			
      input.setProperty( output );
    }
		
    return input;
  }

}
