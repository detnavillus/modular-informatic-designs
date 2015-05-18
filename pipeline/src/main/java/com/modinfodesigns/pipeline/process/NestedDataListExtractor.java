package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces the DataList in the processing stream with nested data objects
 * or data lists.
 * 
 * @author Ted Sullivan
 */
public class NestedDataListExtractor implements IDataObjectProcessor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NestedDataListExtractor.class );

  private String nestedDataListName;
	
  private ArrayList<String> parentProperties;
	
  private boolean copyParentProperties = false;
	
  private boolean copyListProperties = true;
	
	
  public void setNestedDataProperty( String nestedDataListName )
  {
    this.nestedDataListName = nestedDataListName;
  }
	
  public void setNestedDataListName( String nestedDataListName )
  {
    this.nestedDataListName = nestedDataListName;
  }
	
  public void addParentProperty( String parentProperty )
  {
    if (parentProperties == null) parentProperties = new ArrayList<String>( );
    parentProperties.add( parentProperty );
  }
	
  public void setCopyParentProperties( String copyParentProps )
  {
    this.copyParentProperties = (copyParentProps != null && copyParentProps.equalsIgnoreCase( "true" ));
  }
	

  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList: " + ((data != null) ? data.size() : 0));
		
    if (nestedDataListName == null)
    {
      LOG.debug( "nestedDataListName is NULL - returning original data " );
      return data;
    }
		
    DataList newList = new DataList( );
    if (copyListProperties)
    {
      for (Iterator<String> propIt = data.getPropertyNames( ); propIt.hasNext(); )
      {
        String listProp = propIt.next();
                
        IProperty prop = data.getProperty( listProp );
        LOG.debug( "Copying property: " + listProp + " = " + prop.getValue( ) );
        newList.addProperty( prop.copy( ) );
      }
    }
		
    for (Iterator<DataObject> dit = data.getData( ); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
        
      IDataList nestedList = (IDataList)dobj.getProperty( nestedDataListName );
      if (nestedList != null)
      {
        for (Iterator<DataObject> nit = nestedList.getData(); nit.hasNext(); )
        {
          DataObject nested = nit.next();
				
          // if have properties to copy get them ...
          if (parentProperties != null)
          {
            for (int i = 0; i < parentProperties.size(); i++)
            {
              String propName = parentProperties.get( i );
              IProperty prop = dobj.getProperty( propName );
              if (prop != null)
              {
                nested.addProperty( prop.copy( ) );
              }
            }
          }
          else if (copyParentProperties )
          {
            for (Iterator<String> parentPropIt = dobj.getPropertyNames(); parentPropIt.hasNext(); )
            {
              String parentProp = parentPropIt.next();
              if (parentProp.equals( nestedDataListName ) == false )
              {
                IProperty prop = dobj.getProperty( parentProp );
                nested.addProperty( prop.copy( ) );
              }
            }
          }
				
          newList.addDataObject( nested );
        }
      }
      else
      {
        LOG.debug( "No nested data named " + nestedDataListName );
      }
    }
		
    LOG.debug( "Returning new list with " + newList.size() + " objects." );
    return newList;
  }

  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {

  }

}
