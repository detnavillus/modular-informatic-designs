package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchema;

import java.util.HashMap;
import java.util.Iterator;

// To Do: make this use a DataList

public class QueryTreeSet extends DataList implements ICreateDataObjectSchema
{
  HashMap<String,QueryTree> queryTreeMap;
	
  public void addQueryTree( QueryTree qTree )
  {
    if (queryTreeMap == null) queryTreeMap = new HashMap<String,QueryTree>( );
    queryTreeMap.put( qTree.getName(), qTree );
  }
	
  public QueryTree getQueryTree( String name )
  {
    return (queryTreeMap != null) ? queryTreeMap.get( name ) : null;
  }
	
  public Iterator<QueryTree> getQueryTrees( )
  {
    return (queryTreeMap != null) ? queryTreeMap.values().iterator() : null;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setChildObjectSchema( "QueryTree" );
    dos.setChildPlaceholderClass( "com.modinfodesigns.search.QueryTreePlaceholder" );
    
    return dos;
  }

}
