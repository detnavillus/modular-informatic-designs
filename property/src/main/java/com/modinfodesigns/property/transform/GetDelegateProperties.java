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
    
  private boolean multiValue = true;
    
  private boolean removeOriginal = true;
    
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
    LOG.debug( "setTargetFieldSuffix: " + targetFieldSuffix );
    this.targetFieldSuffix = targetFieldSuffix;
  }
    
  public void setMultiValue( boolean multiValue )
  {
    this.multiValue = multiValue;
  }
    
  public void addExcludedValue( String excludedValue )
  {
    LOG.debug( "addExcludedValue " + excludedValue );
    if (excludedValues == null) excludedValues = new HashSet<String>( );
    excludedValues.add( excludedValue );
  }
    
  public void setExcludedValue( String excludedValue )
  {
    addExcludedValue( excludedValue );
  }
    
  public void setRemoveOriginal( boolean removeOriginal )
  {
    this.removeOriginal = removeOriginal;
  }

  public void setRemoveOriginal( String removeOriginal )
  {
    this.removeOriginal = removeOriginal.equalsIgnoreCase( "true" );
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
    LOG.debug( "GetDelegateProperties.transformPropertyHolder: " + input );
    if (delegateField.equals( "*" ))
    {
      Iterator<String> propNames = input.getPropertyNames( );
      while( propNames.hasNext( ) )
      {
        String propName = propNames.next( );
        LOG.debug( " Getting delegate for " + propName );
        if (excludedValues == null || !excludedValues.contains( propName ))
        {
          IProperty prop = input.getProperty( propName );
          if (prop instanceof DataObject)
          {
            DataObject del = (DataObject)prop;
            LOG.debug( "setOutputProperty " + input + " " + del + " " + propName );
            setOutputProperty( input, del, propName );
          }
          else if (prop instanceof PropertyList)
          {
            PropertyList pl = (PropertyList)prop;
            addOutputProperty( input, pl, propName );
          }
        }
      }
    }
    else
    {
      LOG.debug( "Getting delegate for " + delegateField );
      IProperty delProp = input.getProperty( delegateField );
      if (delProp != null && delProp instanceof DataObject )
      {
        DataObject del = (DataObject)delProp;
        setOutputProperty( input, del, delegateField );
      }
      else if (delProp != null && delProp instanceof PropertyList)
      {
        PropertyList pl = (PropertyList)delProp;
        addOutputProperty( input, pl, delegateField );
      }
      else
      {
        LOG.debug( "Not a DataObjectDelegate: " + delProp );
      }
        
      if (removeOriginal)
      {
        input.removeProperty( delegateField );
      }
    }
    
    LOG.debug( "transformPropertyHolder " + delegateField + " DONE." );

    return input;
  }
      
  private void setOutputProperty( IPropertyHolder input, DataObject delegate, String propName  )
  {
    DataObject del = (delegate.getProxyObject( ) != null) ? delegate.getProxyObject( ) : delegate;
      
    System.out.println( "setOutputProperty: " + propName + "  " + delegate.getValue( ) );
    IProperty sourceProp = del.getProperty( sourceField );
    if (sourceProp == null && sourceField.equals( "name" ))
    {
        sourceProp = new StringProperty( propName, del.getName( ) );
    }
          
    setOutputProperty( input, sourceProp );
  }
    
  private void addOutputProperty( IPropertyHolder input, PropertyList delegate, String propName )
  {
    LOG.debug( "PropertyList: addOutputProperty: " + propName );
    Iterator<IProperty> props = delegate.getProperties( );
    while ( props.hasNext( ) )
    {
      IProperty del = props.next( );
      LOG.debug( "  Got delegate property: " + del.getValue( ) + "  " + del );
      if ( del instanceof DataObject )
      {
        DataObject delob = (DataObject)del;
          
        DataObject proxy = delob.getProxyObject( );
        if (proxy != null)
        {
            delob = proxy;
        }

        LOG.debug( "    DataObject: " + delob.getName( ) + " sourceField = " + sourceField );
          
        IProperty sourceProp = delob.getProperty( sourceField );
        LOG.debug( "        Got prop " + ((sourceProp != null) ? sourceProp.getValue( ) : "NULL!" ));
        if (sourceProp == null && sourceField.equals( "name" ))
        {
          LOG.debug( "      Using DataObject name = " + delob.getName( ) );
          sourceProp = new StringProperty( propName, delob.getName( ) );
        }
        
        if (sourceProp != null  && !(sourceProp.getValue( ).equals( propName )))
        {
          if (sourceProp instanceof PropertyList)
          {
            PropertyList sourceLst = (PropertyList)sourceProp;
            Iterator<IProperty> sourceProps = sourceLst.getProperties( );
            while( sourceProps.hasNext( ) )
            {
              IProperty sp = sourceProps.next( );
              addOutputProperty( input, sp );
            }
          }
          else
          {
            addOutputProperty( input, sourceProp );
          }
        }
      }
      else
      {
        addOutputProperty( input, del );
      }
    }
  }
    
  private void setOutputProperty( IPropertyHolder input, IProperty sourceProp )
  {
    LOG.debug( "setOutputProperty " + sourceProp );
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
    if (sourceProp == null) return;
      
    if (excludedValues != null )
    {
      String sourceVal = sourceProp.getValue( );
      if ( (excludedValues.contains( "$self" ) && input.getName().equals( sourceVal ))
         || excludedValues.contains( sourceVal ))
      {
        System.out.println( "excluding! " + sourceVal );
        return;
      }
    }
      
    LOG.debug( "addOutputProperty " + sourceProp.getValue( ) );
      
    IProperty outputProp = sourceProp.copy( );
    if (targetField != null)
    {
      outputProp.setName( targetField );
    }
    else if (targetFieldSuffix != null)
    {
      outputProp.setName( sourceField + targetFieldSuffix );
    }
    else
    {
        System.out.println( "No target Field - using source field " );
    }
        
    if (multiValue)
    {
      System.out.println( "GetDelegateProperties adding property " + outputProp.getName( ) + " = " + outputProp.getValue( )  + " to " + input );
      input.addProperty( outputProp );
    }
    else
    {
      LOG.debug( "setting property " + outputProp.getName( ) + " = " + outputProp.getValue( ) + " on " + input );
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