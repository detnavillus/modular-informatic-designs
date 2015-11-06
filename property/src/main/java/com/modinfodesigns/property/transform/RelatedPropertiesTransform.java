package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringCountProperty;

import java.util.Iterator;
import java.util.HashSet;
/**
 * Finds related properties by co-occurrence of DataObject delegates.
 */

public class RelatedPropertiesTransform implements IPropertyHolderTransform
{
  private String relatedPropertyName = "relatedProperties";
    
  private boolean dataObjectsOnly = true;
    
  private HashSet<String> excludedProperties;
    
  public void setRelatedPropertyName( String relatedPropertyName ) {
    this.relatedPropertyName = relatedPropertyName;
  }
    
  public void addExcludedProperty( String excludedProperty )
  {
    System.out.println( "addExcludedProperty " + excludedProperty );
    if (excludedProperties == null) excludedProperties = new HashSet<String>( );
    excludedProperties.add( excludedProperty );
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
    System.out.println( "RelatedPropertiesTransform " + input.getName( ) );
    
    Iterator<IProperty> propIt = input.getProperties( );
    while (propIt != null && propIt.hasNext() ) {
      IProperty prop = propIt.next( );
      // System.out.println( "Checking related property " + prop + ": " + prop.getName( ) );
      if (prop instanceof DataObject) {
        addRelatedProperties( (DataObject)prop, input );
      }
      else if (prop instanceof PropertyList)
      {
        Iterator<IProperty> pit = ((PropertyList)prop).getProperties( );
        while( pit != null && pit.hasNext() )
        {
          IProperty pr = pit.next( );
          if (pr instanceof DataObject)
          {
            addRelatedProperties( (DataObject)pr, input );
          }
        }
      }
    }
    // System.out.println( "RelatedPropertiesTransform " + input.getName( ) + " Done" );
    return input;
  }
    
  private void addRelatedProperties( DataObject childOb, IPropertyHolder input ) {
    if (!(input instanceof DataObject))
    {
        return;
    }

    if (childOb.getProxyObject( ) != null)
    {
      childOb = childOb.getProxyObject( );
    }
    else
    {
      return;
    }
      
    DataObject inputOb = (DataObject)input;
    if (childOb.getID().equals( inputOb.getID() ) )
    {
      return;
    }
      
    String child_id = childOb.getID( );
    System.out.println( "addRelatedProperties to " + child_id );
      
    StringCountProperty scp = null;
    if (childOb.getProperty( relatedPropertyName ) == null)
    {
      scp = new StringCountProperty( relatedPropertyName );
      childOb.setProperty( scp );
    }
    else
    {
      IProperty prop = childOb.getProperty( relatedPropertyName );
      if (prop instanceof StringCountProperty )
      {
        scp = (StringCountProperty)prop;
      }
      else
      {
        scp = new StringCountProperty( relatedPropertyName );
        scp.addString( prop.getValue( ) );
        childOb.setProperty( scp );
      }
    }
      
    Iterator<IProperty> propIt = input.getProperties( );
    while ( propIt != null && propIt.hasNext() ) {
      IProperty prop = propIt.next( );
      // System.out.println( "checking related property " + prop.getName( ) );
      if (prop instanceof IntrinsicPropertyDelegate || prop.getName().equals( relatedPropertyName )
        || (excludedProperties != null && excludedProperties.contains( prop.getName( ) )))
      {
        // System.out.println( "excluding " + prop.getName( ) );
        continue;
      }

      if ( prop instanceof DataObject )
      {
        DataObject propOb = (DataObject)prop;
        addRelatedProperty( propOb, prop.getName( ), child_id, childOb.getName(), scp );
      }
      else if (prop instanceof PropertyList)
      {
        Iterator<IProperty> pit = ((PropertyList)prop).getProperties( );
        while( pit != null && pit.hasNext() )
        {
          IProperty pr = pit.next( );
          if (pr instanceof DataObject)
          {
            DataObject prOb = (DataObject)pr;
            addRelatedProperty( prOb, prop.getName( ), child_id, childOb.getName(), scp );
          }
        }
      }
      else if (!dataObjectsOnly)
      {
        String relatedPropVal = prop.getValue( );
        System.out.println( "Adding related property to " + childOb.getName() + ": " + prop.getName( ) + " = '" + relatedPropVal + "'" );
        scp.addString( relatedPropVal );
      }
    
    }
      
    // System.out.println( "addRelatedProperties DONE" );
  }
      
  private void addRelatedProperty( DataObject propOb, String propName, String child_id, String child_name, StringCountProperty scp )
  {
    if (propOb.getProxyObject( ) != null)
    {
      propOb = propOb.getProxyObject( );
    }

    // System.out.println( "checking related property object " + propOb.getID( ) );
    if ( propOb.getID() != null && !child_id.equals( propOb.getID( ) ))
    {
      String relatedPropVal = propOb.getName( );
      System.out.println( "Adding related property to " + child_name  + ": " + propName + " = '" + relatedPropVal + "'" );
      scp.addString( relatedPropVal );
    }
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
              throws PropertyTransformException
  {
        
  }
    
}