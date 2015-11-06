package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;

import java.util.Iterator;

/**
 * Finds related properties by co-occurrence of DataObject delegates.
 */

public class ReciprocalRelationTransform implements IPropertyHolderTransform
{
  private String sourcePropertyName;
  private String reciprocalPropertyName;
    
  public void setSourcePropertyName( String sourcePropertyName ) {
    this.sourcePropertyName = sourcePropertyName;
  }
    
  public void setReciprocalPropertyName( String reciprocalPropertyName ) {
    this.reciprocalPropertyName = reciprocalPropertyName;
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
    if ( !(input instanceof DataObject)) {
      return input;
    }
      
    DataObject inputDobj = (DataObject)input;
      
    // get all instances of Source Property - if these are DataObjectDelegate
    // get the proxy DataObject and set a reciprocal DataObjectDelegate on it
    // to the input object
    IProperty sourceProp = input.getProperty( sourcePropertyName );
    if (sourceProp != null) {
      if (sourceProp instanceof PropertyList)
      {
        Iterator<IProperty> propIt = ((PropertyList)sourceProp).getProperties( );
        while (propIt != null && propIt.hasNext() )
        {
          IProperty sp = propIt.next();
          if (sp instanceof DataObject )
          {
            DataObject sourceOb = (DataObject)sp;
            addReciprocalLink( inputDobj, sourceOb );
          }
        }
      }
      else if (sourceProp instanceof DataObject)
      {
        DataObject sourceDobj = (DataObject)sourceProp;
        addReciprocalLink( inputDobj, sourceDobj );
      }
    }
      
    return input;
  }
    
  private void addReciprocalLink( DataObject inputDobj, DataObject sourceDobj  )
  {
    if (sourceDobj.getProxyObject() != null)
    {
      sourceDobj = sourceDobj.getProxyObject( );
      System.out.println( "addReciprocalLink " + sourceDobj.getName( ) + " " + reciprocalPropertyName + " " + inputDobj.getName( ) );
      DataObjectDelegate recipDelegate = new DataObjectDelegate( inputDobj );
      recipDelegate.setName( reciprocalPropertyName );
      sourceDobj.addProperty( recipDelegate );
    }
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
              throws PropertyTransformException
  {
        
  }

}