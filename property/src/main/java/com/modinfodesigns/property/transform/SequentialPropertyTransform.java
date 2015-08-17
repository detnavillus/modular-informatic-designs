package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Enables one or more IPropertyTransforms to be applied to an IPropertyHolder property in sequence.
 * 
 * @author Ted Sullivan
 */

public class SequentialPropertyTransform extends BasePropertyTransform implements IPropertyHolderTransform
{
  private String[] inputPropertyList;
  private String outputPropertyName;
   
  private ArrayList<IPropertyTransform> propTransforms;
   
  public void addPropertyTransform( IPropertyTransform propTransform )
  {
    if (propTransforms == null) propTransforms = new ArrayList<IPropertyTransform>( );
    propTransforms.add( propTransform );
  }
   
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (propTransforms == null)
    {
      throw new PropertyTransformException( "No Property Transforms defined" );
    }
		
    IProperty currentProp = input;
    for (int i = 0; i < propTransforms.size(); i++)
    {
      currentProp = propTransforms.get(i).transform( currentProp );
    }
		
    return currentProp;
  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    IProperty inputProp = getInputProperty( input );

    IProperty outputProperty = transform( inputProp );
		
    if (outputProperty != null)
    {
      if (outputPropertyName != null) outputProperty.setName( outputPropertyName );
      input.setProperty( outputProperty );
    }
		
    return input;
  }
	
  private IProperty getInputProperty( IPropertyHolder inputHolder )
  {
    // get the input property - if inputPropertyList has multiple values, create a PropertyList
    if (inputPropertyList != null)
    {
      if (inputPropertyList.length > 1)
      {
        PropertyList pl = new PropertyList( );
        for (int i = 0; i < inputPropertyList.length; i++ )
        {
          pl.addProperty( inputHolder.getProperty( inputPropertyList[i] ));
        }
				
        return pl;
      }
      else if (inputPropertyList.length > 0)
      {
        return inputHolder.getProperty( inputPropertyList[0] );
      }
    }
		
    return null;
  }
	
  public void setOutputProperty( String outputProperty )
  {
    this.outputPropertyName = outputProperty;
  }
	
  public void setInputProperty( String inputProperty )
  {
    inputPropertyList = new String[1];
    inputPropertyList[0] = inputProperty;
  }
	
  public void setInputPropertyList( String[] inputPropertyList )
  {
    this.inputPropertyList = inputPropertyList;
  }

}
