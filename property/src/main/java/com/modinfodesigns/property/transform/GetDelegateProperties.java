package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDelegateProperties implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( GetDelegateProperties.class );
    
  private String delegateField = "*";
  private String sourceField = "name";
  private String targetField;
  private String targetFieldSuffix = "";
    
  private boolean multiValue = false;
    
  private HashSet<String> excludedValues;
    
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

  public void setTargetFieldSuffix( String targetFieldSuffix )
  {
    this.targetFieldSuffix = targetFieldSuffix;
  }
    
  public void setMultiValue( boolean multiValue )
  {
    this.multiValue = multiValue;
  }
    
  public void addExcudedValue( String excludedValue )
  {
    if (excludedValues == null) excludedValues = new HashSet<String>( );
    excludedValues.add( excludedValue );
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return input;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                         throws PropertyTransformException
  {
    if (delegateField.equals( "*" ))
    {
      Iterator<String> propNames = input.getPropertyNames( );
      while( propNames.hasNext( ) )
      {
        String propName = propNames.next( );
        IProperty prop = input.getProperty( propName );
        if (prop instanceof DataObject)
        {
          DataObject del = (DataObject)prop;
          setOutputProperty( input, del, propName );
        }
      }
    }
    else
    {
      IProperty delProp = input.getProperty( delegateField );
      if (delProp instanceof DataObject )
      {
        DataObject del = (DataObject)delProp;
        setOutputProperty( input, del, delegateField );
      }
        else
        {
            System.out.println( "Not a DataObjectDelegate " );
        }
    }
        
    return input;
  }
      
  private void setOutputProperty( IPropertyHolder input, DataObject delegate, String propName  )
  {
    IProperty sourceProp = null;
    if (sourceField.equals( "name" ))
    {
        sourceProp = new StringProperty( propName, delegate.getName( ) );
    }
    else
    {
      sourceProp = delegate.getProperty( sourceField );
    }
          
    setOutputProperty( input, sourceProp );
  }
    
  private void setOutputProperty( IPropertyHolder input, IProperty sourceProp )
  {
    if (sourceProp instanceof PropertyList )
    {
      Iterator<IProperty> props = ((PropertyList)sourceProp).getProperties( );
      while (props.hasNext() )
      {
        IProperty prop = props.next( );
        addOutputProperty( input, prop );
      }
    }
    else
    {
      addOutputProperty( input, sourceProp );
    }
  }
    
  private void addOutputProperty( IPropertyHolder input, IProperty sourceProp )
  {
    if (excludedValues != null )
    {
      String sourceVal = sourceProp.getValue( );
      if ( (excludedValues.contains( "$self" ) && input.getName().equals( sourceVal ))
         || excludedValues.contains( sourceVal ))
      {
        return;
      }
    }
      
    IProperty outputProp = sourceProp.copy( );
    if (targetField != null)
    {
      outputProp.setName( targetField );
    }
    else if (targetFieldSuffix != null)
    {
      outputProp.setName( sourceField + targetFieldSuffix );
    }
        
    if (multiValue)
    {
      LOG.debug( "adding property " + outputProp.getName( ) + " = " + outputProp.getValue( ) );
      input.addProperty( outputProp );
    }
    else
    {
      LOG.debug( "adding property " + outputProp.getName( ) + " = " + outputProp.getValue( ) );
      input.setProperty( outputProp );
    }
  }

  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }

}