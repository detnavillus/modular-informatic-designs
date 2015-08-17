package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;

/**
 * Translates a nested DataObject with a name and value field into a String Property
 *
 * Replaces a PropertyList of DataObjects with name and value fields into a DataObject with properties where
 * the name and value are unified.
 *
 * @author Ted Sullivan
 */
public class NameValueObjectTransform implements IPropertyTransform
{
  private String nameProperty;  // nested IPropertyHolder
  private String valueProperty;

  private boolean replacePropertyList = true;
    
  public NameValueObjectTransform( ) {  }
    
  public NameValueObjectTransform( String nameProperty, String valueProperty )
  {
    this.nameProperty = nameProperty;
    this.valueProperty = valueProperty;
  }
	
  public void setNameProperty( String nameProperty )
  {
    this.nameProperty = nameProperty;
  }
    
  public void setValueProperty( String valueProperty )
  {
    this.valueProperty = valueProperty;
  }
	

    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input instanceof PropertyList && replacePropertyList )
    {
      DataObject outputObj = new DataObject( );
      outputObj.setName( input.getName( ) );
      Iterator<IProperty> props = ((PropertyList)input).getProperties( );
      while( props.hasNext( ) )
      {
        IProperty prop = props.next( );
        IProperty output = transform( prop );
        outputObj.addProperty( output );
      }
        
      return outputObj;
    }
      
    if (input instanceof IPropertyHolder)
    {
      IProperty nameProp = ((IPropertyHolder)input).getProperty( nameProperty );
      IProperty valueProp = ((IPropertyHolder)input).getProperty( valueProperty );
      if ( nameProp != null  && valueProp != null)
      {
        return new StringProperty( nameProp.getValue( ), valueProp.getValue( ) );
      }
    }
		
    return input;
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener )
         throws PropertyTransformException
  {
        
  }

    
}
