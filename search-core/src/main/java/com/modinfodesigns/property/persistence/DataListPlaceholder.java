package com.modinfodesigns.property.persistence;

import java.util.Iterator;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;

public class DataListPlaceholder extends DataObjectPlaceholder implements IDataList
{
  public DataListPlaceholder( ) {  }
	
  public DataListPlaceholder( String name, String id,
                              String dataObjectSchema, SQLTemplatePersistenceManager templateMan )
  {
    super( name, id, dataObjectSchema, templateMan );
  }
	
  protected IDataList getProxyList( )
  {
    DataObject proxy = super.getProxyObject( );
    return (proxy != null && proxy instanceof IDataList) ? (IDataList)super.getProxyObject( ) : null;
  }

  @Override
  public int size()
  {
    IDataList proxyList = getProxyList( );
    return (proxyList != null) ? proxyList.size() : 0;
  }

  @Override
  public Iterator<DataObject> getData()
  {
    IDataList proxyList = getProxyList( );
    return (proxyList != null) ? proxyList.getData() : null;
  }

  @Override
  public DataObject item( int index )
  {
    IDataList proxyList = getProxyList( );
    return (proxyList != null) ? proxyList.item( index ) : null;
  }

  @Override
  public void addDataObject(DataObject dObj)
  {
    IDataList proxyList = getProxyList( );
    if (proxyList != null)
    {
      proxyList.addDataObject( dObj );
      setModified( true );
    }
  }

  @Override
  public void removeDataObject( int index )
  {
    IDataList proxyList = getProxyList( );
    if (proxyList != null)
    {
      proxyList.removeDataObject( index );
      setModified( true );
    }
  }

  @Override
  public void clearDataList()
  {
    IDataList proxyList = getProxyList( );
    if (proxyList != null)
    {
      proxyList.clearDataList(  );
      setModified( true );
    }
  }

  @Override
  public void setChildProperty(IProperty childProperty)
  {
    IDataList proxyList = getProxyList( );
    if (proxyList != null)
    {
      proxyList.setChildProperty( childProperty );
      setModified( true );
    }

  }
}
