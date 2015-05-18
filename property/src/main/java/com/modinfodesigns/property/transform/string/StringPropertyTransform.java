package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyValidationException;


import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.transform.SequentialPropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.app.ApplicationManager;

import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a set of <code>IStringTransforms</code> to transform a string property.
 * 
 * @author Ted Sullivan
 */

public class StringPropertyTransform extends SequentialPropertyTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ApplicationManager.class );

  private String stringPropertyName;
	
  private String stringTransformBean;
    
  private ArrayList<IStringTransform> stringTransforms;
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input == null)
    {
      throw new PropertyTransformException( "Input is NULL!" );
    }
        
    LOG.debug( "transforming " + input.getValue() );
        
    try
    {
      if (input instanceof StringProperty )
      {
        return transformStringProperty( (StringProperty)input );
      }
      else if (input instanceof StringListProperty)
      {
        return transformStringListProperty( (StringListProperty)input );
      }
      else if (input instanceof IPropertyHolder)
      {
        return transformPropertyHolder( (IPropertyHolder)input );
      }
      else if (input instanceof IDataList)
      {
        return transformDataList( (IDataList)input );
      }
    }
    catch ( StringTransformException sfe )
    {
      throw new PropertyTransformException( "Got StringTransformException: " + sfe.getMessage( ));
    }

    throw new PropertyTransformException( "Cannot transform " + input );
  }
	
  private IProperty transformStringProperty( StringProperty input ) throws StringTransformException
  {
    String inputStr = input.getValue( );
    try
    {
      input.setValue( transform( inputStr ), "" );
    }
    catch (PropertyValidationException pve )
    {
      throw new StringTransformException( pve.getMessage( ) );
    }
    return input;
  }
    
  private IProperty transformStringListProperty( StringListProperty input ) throws StringTransformException
  {
    String[] inputList = input.getStringList( );
    String[] outputList = new String[ inputList.length ];

    for (int i = 0, isz = inputList.length; i < isz; i++)
    {
      outputList[i] = transform( inputList[i] );
    }

    input.setStringList( outputList );

    return input;
  }

  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )  throws PropertyTransformException
  {
    if (stringPropertyName == null)
    {
      throw new PropertyTransformException( "String Property name is null!" );
    }
    	
    IProperty innerProp = input.getProperty( stringPropertyName );
    if (innerProp != null)
    {
      IProperty outputProp = transform( innerProp );
      input.setProperty( outputProp );
    }
        
    return input;
  }

  private IProperty transformDataList( IDataList input )  throws StringTransformException
  {
    try
    {
      // for each thing in the data list, transform the string property
      Iterator<DataObject> it = input.getData( );
      while( it.hasNext( ))
      {
        transformPropertyHolder( it.next() );
      }
    }
    catch (PropertyTransformException pte )
    {
      throw new StringTransformException( "Got PropertyTransformException " + pte );
    }
    	
    return input;
  }
    
    
  private String transform( String input ) throws StringTransformException
  {
    LOG.debug( "transform( " + input + " )" );
    	
    ArrayList<IStringTransform> strTransforms = getStringTransforms( );
    if (strTransforms == null)
    {
      throw new StringTransformException( "StringTransform is null!" );
    }
        
    String output = input;
    for (int i = 0, isz = strTransforms.size(); i < isz; i++)
    {
      IStringTransform strTransform = strTransforms.get( i );
    
      output = strTransform.transformString( output );
    }
        
    LOG.debug( "transformed = '" + output + "'" );
        
    return output;
  }
    
  private ArrayList<IStringTransform> getStringTransforms(  )
  {
    if (stringTransforms != null) return this.stringTransforms;
    	
    ApplicationManager appMan = ApplicationManager.getInstance( );
    	
    if (stringTransformBean != null)
    {
      IStringTransform stringTransform = (IStringTransform)appMan.getApplicationObject( stringTransformBean, "StringTransform" );
    
      if (stringTransform == null)
      {
        LOG.error( "Cannot get StringTranform: " + stringTransformBean );
      }
        
      stringTransforms = new ArrayList<IStringTransform>( );
      stringTransforms.add( stringTransform );
    }
    	
    return stringTransforms;
  }

	
  /**
   * Sets the name of the String property to be transformed.
   *
   * @param propertyName
   */
  public void setTransformProperty( String propertyName )
  {
    this.stringPropertyName = propertyName;
  }

  public void setStringTransformBean( String transformBeanName )
  {
    this.stringTransformBean = transformBeanName;
  }
	
  public void addStringTransform( IStringTransform stringTransform )
  {
    LOG.debug( "setStringTransform " + stringTransform );
    if (stringTransforms == null)
    {
      stringTransforms = new ArrayList<IStringTransform>( );
    }
		
    this.stringTransforms.add( stringTransform );
  }

}
