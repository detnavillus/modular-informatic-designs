package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.PropertyValidationException;

import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

/**
 * Creates a Property from a list of input properties. Concatenates their string representations using a delimiter.
 * For more complex formatting use the PropertyTemplateTransform
 * 
 * @author Ted Sullivan
 */

public class ConcatenateTransform extends SequentialPropertyTransform implements IPropertyHolderTransform
{
  private String delimiter = "";  // By default - just concatenate without spaces
  private String inputFormat = null;
    
  private Map<String,String> inputFormatMap;  // Map of property name to format (optional)
    
  private String outputClass = null;
  private String outputFormat = null;
    
  private ArrayList<String> inputProperties;
    
  private String outputProperty;
    
  private String valueDelimiter;
   
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    IProperty outputProp = getOutputProperty( );
		
    if (input instanceof PropertyList)
    {
      PropertyList pl = (PropertyList)input;
      StringBuilder sbr = new StringBuilder( );
			
      // concatenate the list here ...
      for (Iterator<IProperty> pit = pl.getProperties(); pit.hasNext(); )
      {
        IProperty pp = pit.next( );
        sbr.append( getValue( pp ) );
        if (pit.hasNext() ) sbr.append( delimiter );
      }
			
      try
      {
        outputProp.setValue( sbr.toString(), outputFormat );
      }
      catch (PropertyValidationException pve )
      {
        throw new PropertyTransformException( pve.getMessage( ) );
      }
    }
    else
    {
      try
      {
        outputProp.setValue( getValue( input ), outputFormat );
      }
      catch (PropertyValidationException pve )
      {
        throw new PropertyTransformException( pve.getMessage( ) );
      }
    }
		
    return outputProp;
  }
	
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    if (inputProperties != null)
    {
      IProperty outputProperty = getOutputProperty( );
      StringBuilder sbr = new StringBuilder( );
        
      for (int i = 0; i < inputProperties.size(); i++)
      {
        String propName = inputProperties.get( i );
        IProperty prop = input.getProperty( propName );
        if (prop != null)
        {
          if (sbr.length() > 0) sbr.append( delimiter );
          if (valueDelimiter != null)
          {
            sbr.append( propName ).append( valueDelimiter );
          }
          sbr.append( getValue( prop ) );
        }
      }
			
      try
      {
        outputProperty.setValue( sbr.toString(), outputFormat );
      }
      catch ( PropertyValidationException pve )
      {
        throw new PropertyTransformException( pve.getMessage( ) );
      }
			
      input.addProperty( outputProperty );
    }
    
    return input;
  }
	
  private String getValue( IProperty inputProp )
  {
    String inputForm = getInputFormat( inputProp );
    return (inputForm != null) ? inputProp.getValue( inputForm ) : inputProp.getValue( );
  }
	
  private String getInputFormat( IProperty input )
  {
    if (inputFormatMap != null)
    {
      String format = inputFormatMap.get( input.getName( ) );
      if (format != null) return format;
    }
		
    // Else return the default input format
    return inputFormat;
  }
	
  public IProperty getOutputProperty( ) throws PropertyTransformException
  {
    IProperty outputProp = null;
		
    if (outputClass != null)
    {
      try
      {
        outputProp = (IProperty)Class.forName( outputClass ).newInstance(  );
      }
      catch ( Exception e )
      {
        throw new PropertyTransformException( e.getMessage( ) );
      }
    }
		
    outputProp = new StringProperty( );
    if (outputProperty != null)
    {
      outputProp.setName( outputProperty );
    }
		
    return outputProp;
  }
	
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
	
  public void setValueDelimiter( String valueDelimiter )
  {
    this.valueDelimiter = valueDelimiter;
  }
	
  public void setInputFormat( String inputFormat )
  {
    this.inputFormat = inputFormat;
  }
	
  public void setInputFormatMap( Map<String,String> inputFormatMap )
  {
    this.inputFormatMap = inputFormatMap;
  }
	
  public void setOutputFormat( String outputFormat )
  {
    this.outputFormat = outputFormat;
  }
	
  public void setOutputClass( String outputClass )
  {
    this.outputClass = outputClass;
  }
	
  public void setOutputProperty( String outputProperty )
  {
    this.outputProperty = outputProperty;
  }
	
  public void addInputProperty( String inputProperty )
  {
    if (inputProperties == null) inputProperties = new ArrayList<String>( );
    inputProperties.add( inputProperty );
  }
}
