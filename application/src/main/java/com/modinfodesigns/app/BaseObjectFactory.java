package com.modinfodesigns.app;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseObjectFactory implements IObjectFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BaseObjectFactory.class );
    
  private HashMap<String,LinkedHashMap<String,Object>> objMap = new HashMap<String,LinkedHashMap<String,Object>>( );

  @Override
  public abstract void initialize( String config );

  @Override
  public Object getApplicationObject( String name, String type )
  {
    LinkedHashMap<String,Object> typeObjs = objMap.get( type );
    if (typeObjs != null)
    {
      return typeObjs.get( name );
    }
        
    return null;
  }
    
  @Override
  public List<Object> getApplicationObjects( String type )
  {
    LinkedHashMap<String,Object> typeObjs = objMap.get( type );
    if (typeObjs != null)
    {
      ArrayList<Object> objs = new ArrayList<Object>( );
      for (Iterator<Object> it = typeObjs.values().iterator(); it.hasNext( ); )
      {
        objs.add( it.next( ) );
      }
            
      return objs;
    }
        
    return null;
  }

  protected void addObject( String name, String type, Object object )
  {
    LOG.debug( "addObject( " + name + ", " + type + ", " + object + " )" );
        
    LinkedHashMap<String,Object> typeObjs = objMap.get( type );
    if (typeObjs == null)
    {
      typeObjs = new LinkedHashMap<String,Object>( );
      objMap.put( type, typeObjs );
    }
        
    typeObjs.put( name, object );
  }
    
  @Override
  public List<String> getApplicationObjectNames(String type)
  {
    LinkedHashMap<String,Object> typeObjs = objMap.get( type );
    if (typeObjs != null)
    {
      ArrayList<String> objNames = new ArrayList<String>( );
      for (Iterator<String> it = typeObjs.keySet().iterator(); it.hasNext( ); )
      {
        objNames.add( it.next( ) );
      }
            
      return objNames;
    }
        
    return null;
  }
}
