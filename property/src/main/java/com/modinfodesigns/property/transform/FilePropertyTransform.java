package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.utils.FileMethods;

/**
 * Transforms a property containing a File name into a text property.
 * 
 * @author Ted Sullivan
 */
public class FilePropertyTransform implements IPropertyHolderTransform 
{
  // Input Property contains the file name
  private String inputProperty;
    
  private String outputProperty;
    
  public void setInputProperty( String inputProperty )
  {
    this.inputProperty = inputProperty;
  }
    
  public void setOutputProperty( String outputProperty )
  {
    this.outputProperty = outputProperty;
  }
    
    
  @Override
  public IProperty transform( IProperty input )
                              throws PropertyTransformException
  {
    String fileContents = FileMethods.readFile( input.getName( ) );
    return new StringProperty( outputProperty, fileContents );
  }



  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    if (inputProperty != null )
    {
      IProperty inputProp = input.getProperty( inputProperty );
      if (inputProp != null)
      {
        IProperty outputProp = transform( inputProp );
        if (outputProperty != null)
        {
          outputProp.setName( outputProperty );
        }
        else
        {
          outputProp.setName( inputProperty );
        }
				
        input.setProperty( outputProp );
      }
    }
		
    return input;
  }

	
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }
}
