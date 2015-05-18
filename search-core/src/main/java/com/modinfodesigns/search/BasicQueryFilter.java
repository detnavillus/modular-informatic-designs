package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;

import com.modinfodesigns.property.transform.IPropertyTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Query object from an IProperty
 * 
 * @author Ted Sullivan
 *
 */
public class BasicQueryFilter implements IQueryFilter
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BasicQueryFilter.class );

  // ==============================================
  // Map of input field -> query field
  // ==============================================
  private HashMap<String,String> queryFieldMap;
	
  private HashMap<String,IPropertyTransform> transforms;
	
  private int pageSize = 10;
  private int startRec = 0;
	
  private SortProperty sortProp = null;
	
  public void setQueryFieldMap( DataList fieldMapData )
  {
    LOG.debug( "setQueryFieldMap " );
		
    queryFieldMap = new HashMap<String,String>( );
    for (Iterator<DataObject> dataIt = fieldMapData.getData(); dataIt.hasNext(); )
    {
      DataObject dobj = dataIt.next();
      String inputField = dobj.getProperty( "inputField" ).getValue();
      String queryField = dobj.getProperty( "queryField" ).getValue();
			
      LOG.debug( "Mapping " + inputField + " to " + queryField );
      queryFieldMap.put( inputField, queryField );
			
      Object transformObj = dobj.getObject( "Transform" );
      if (transformObj == null)
      {
        transformObj = dobj.getObject( "PropertyTransform" );
      }
      if (transformObj != null && transformObj instanceof IPropertyTransform)
      {
        IPropertyTransform propTransform = (IPropertyTransform)transformObj;
				
        LOG.debug( "Got Property Transform " + propTransform );
        if (transforms == null) transforms = new HashMap<String,IPropertyTransform>( );
        transforms.put( inputField, propTransform );
      }
    }
  }
	

  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    return createQuery( input );
  }



  @Override
  public IQuery filterQuery( IQuery input )
  {
    return createQuery( input );
  }

  @Override
  public IQuery createQuery(IProperty input)
  {
    Query query = new Query( );
    query.setPageSize( pageSize );
    query.setStartRecord( startRec );
		
    if (input instanceof IPropertyHolder)
    {
      IPropertyHolder propHolder = (IPropertyHolder)input;
    
      if (queryFieldMap != null)
      {
        for (Iterator<String> fieldIt = queryFieldMap.keySet().iterator(); fieldIt.hasNext(); )
        {
          String inputField = fieldIt.next();
          String queryField = queryFieldMap.get( inputField );
				
          IProperty inputProp = propHolder.getProperty( inputField );
          if (inputProp != null)
          {
            addQueryField( inputField, queryField, inputProp, query );
          }
        }
      }
    }
    else if (input instanceof PropertyList)
    {
      PropertyList pList = (PropertyList)input;
      for (Iterator<IProperty> propIt = pList.getProperties( ); propIt.hasNext();  )
      {
        IProperty inputProp = propIt.next();
        String queryField = queryFieldMap.get( inputProp.getName() );
        if (queryField != null)
        {
          addQueryField( inputProp.getName( ), queryField, inputProp, query );
        }
      }
    }
    else
    {
      IProperty copy = input.copy();
      query.addProperty( copy );
    }
		
    return query;
  }
	
  // This should create a QueryField
  private void addQueryField( String inputField, String queryField, IProperty inputProp, Query query )
  {
    IProperty copy = inputProp.copy( );
    copy.setName( queryField );
	    
    IPropertyTransform propTransform = (transforms != null) ? transforms.get( inputField ) : null;
    if (propTransform != null)
    {
      try
      {
        copy = propTransform.transform( copy );
      }
      catch( PropertyTransformException pte )
      {
          
      }
    }
	    
    query.addProperty( copy );
  }

  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    return (IPropertyHolder)transform( input );
  }

}
