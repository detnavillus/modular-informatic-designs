package com.modinfodesigns.property.persistence;

import com.modinfodesigns.property.DataObject;

public interface IDataObjectPlaceholder
{
  public void setName( String name );
  public void setID( String id );
  public void setSchemaName( String schemaName );
  public void setSQLTemplatePersistenceManager( SQLTemplatePersistenceManager templateMan );
    
  public DataObject getProxyObject( );
}
