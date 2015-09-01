package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;

public class PutDelegateProperties implements IPropertyHolderTransform
{
  private String delegateField;
  private String sourceField;
  private String targetField;
    
  private boolean multiValue = true;
    
  public void setDelegateField( String delegateField )
  {
    this.delegateField = delegateField;
  }
    
  public void setSourceField( String sourceField )
  {
    this.sourceField = sourceField;
  }
    
  public void setTargetField( String targetField )
  {
    this.targetField = targetField;
  }
    
  public void setMultiValue( boolean multiValue )
  {
    this.multiValue = multiValue;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input instanceof IPropertyHolder )
    {
      return transformPropertyHolder( (IPropertyHolder)input );
    }
    return input;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    System.out.println( "PutDelegateProperties:transformPropertyHolder" );
    IProperty targetProp = null;
      
    if (sourceField.equals( "name" ))
    {
      targetProp = new StringProperty( targetField, input.getName( ) );
    }
    else
    {
      IProperty inputProp = input.getProperty( sourceField );
      targetProp = inputProp.copy( );
      targetProp.setName( targetField );
    }
      
    IProperty delegateProp = input.getProperty( delegateField );
    System.out.println( "  delegateProp " + delegateProp );
    if (delegateProp instanceof DataObject )
    {
        addProperty( targetProp, (DataObject)delegateProp );
    }
    else if (delegateProp instanceof PropertyList )
    {
      PropertyList pl = (PropertyList)delegateProp;
      Iterator<IProperty> propIt = pl.getProperties( );
      while (propIt.hasNext( ) )
      {
        IProperty prp = propIt.next( );
        if (prp instanceof DataObject )
        {
          addProperty( targetProp, (DataObject)prp );
        }
      }
    }
    else
    {
      System.out.println( "Could not add " + targetProp );
    }
      
    return input;
  }
    
  private void addProperty( IProperty targetProp, DataObject delegate )
  {
    if (delegate.getProxyObject( ) != null)
    {
      delegate = delegate.getProxyObject( );
    }
        
    if (multiValue)
    {
      System.out.println( "PutDelegateProperties addProperty " + targetProp.getName( ) + " = " + targetProp.getValue( ) );
      delegate.addProperty( targetProp );
    }
    else
    {
      System.out.println( "PutDelegateProperties setProperty " + targetProp.getName( ) + " = " + targetProp.getValue( ) );
      delegate.setProperty( targetProp );
    }
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {
        
  }
    
}