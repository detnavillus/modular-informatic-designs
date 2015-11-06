package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;
import java.util.HashSet;

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
    
  private HashSet<String> valueList;
    
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
	
  public void setValueList( String valueList )
  {
    String[] valueArray = valueList.split( "," );
    this.valueList = new HashSet<String>( );
      
    for (int i = 0; i < valueArray.length; i++ ) {
      this.valueList.add( valueArray[i] );
    }
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
        if (nameProp instanceof PropertyList )
        {
          if (valueList != null)
          {
            Iterator<IProperty> nameIt = ((PropertyList)nameProp).getProperties( );
            while (nameIt.hasNext() )
            {
              IProperty nameP = nameIt.next( );
              if (valueList.contains( nameP.getValue( ) ) )
              {
                IProperty outputProp = valueProp.copy( );
                outputProp.setName( nameP.getValue( ) );
                return outputProp;
              }
            }
          }
        }
        else
        {
          IProperty outputProp = valueProp.copy( );
          outputProp.setName( nameProp.getValue( ) );
          return outputProp;
        }
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
