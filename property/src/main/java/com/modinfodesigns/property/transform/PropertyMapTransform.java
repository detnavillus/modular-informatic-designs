package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.MappedProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Purpose property name mapper - if createAliases is false, changes property names from 
 * input to output.
 * 
 * If createAliases is set - creates a MappedProperty to do the mapping. Using aliases enables Many-To-One 
 * mappings to be created. Aliasing also does not break any existing associations (such as Function properties) 
 * that have already been established. Using aliases creates additional memory overhead so it should
 * be turned off in certain situations.
 * 
 * @author Ted Sullivan
 */

public class PropertyMapTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertyMapTransform.class );

  private HashMap<String,String> propertyMap = new HashMap<String,String>( );
  private ArrayList<String> propList = new ArrayList<String>( );
    
  private boolean createAliases = true;
    
  public void addPropertyMapping( String fromProp, String toProp )
  {
    propList.add( toProp );
    propertyMap.put( toProp, fromProp  );
  }
    
  public void addPropertyMap( DataList dList )
  {
    setPropertyMap( dList );
  }
    
  public void setPropertyMap( DataList dList )
  {
    for ( Iterator<DataObject> dit = dList.getData(); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      IProperty fromProp = dobj.getProperty( "input" );
      if (fromProp == null)
      {
        fromProp = dobj.getProperty( "inputProperty" );
      }
      if (fromProp == null)
      {
        fromProp = dobj.getProperty( "from" );
      }
      if (fromProp == null)
      {
        fromProp = dobj.getProperty( "name" );
      }
    		
      if (fromProp != null)
      {
        IProperty toProp = dobj.getProperty( "output" );
        if (toProp == null)
        {
          toProp = dobj.getProperty( "outputProperty" );
        }
        if (toProp == null)
        {
          toProp = dobj.getProperty( "to" );
        }
        if (toProp == null)
        {
          toProp = dobj.getProperty( "mapTo" );
        }
    			
        if (toProp != null)
        {
          LOG.debug( "mapping: " + fromProp.getValue() + " to " + toProp.getValue( ) );
          propList.add( toProp.getValue( ) );
          propertyMap.put( toProp.getValue(), fromProp.getValue() );
        }
      }
    }
  }
    
  public void setCreateAliases( String createAliases )
  {
    this.createAliases = (createAliases != null && createAliases.equalsIgnoreCase( "true" ));
  }
    
  public void setCreateAliases( boolean createAliases )
  {
    this.createAliases = createAliases;
  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                          throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder( ) ..." );

    for (int i = 0; i < propList.size(); i++ )
    {
      String propName = propList.get( i );
      IProperty prop = input.getProperty( propName );
      String newName = propertyMap.get( propName );
        
      if (prop != null )
      {
        if (createAliases)
        {
          if ( MappedProperty.canMap( newName, propName, input ) )
          {
            input.setProperty( new MappedProperty( newName, propName, input ));
          }
          else
          {
            LOG.error( "Illegal Mapping Attempted!" );
          }
        }
        else
        {
          input.removeProperty( propName );
          prop.setName( newName );
          input.setProperty( prop );
        }
      }
    }

    return input;
  }


  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    return input;
  }

  @Override
  public void startTransform(IProperty input, IPropertyTransformListener transformListener)
             throws PropertyTransformException
  {

  }

}
