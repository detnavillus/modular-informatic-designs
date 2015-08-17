package com.modinfodesigns.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton Class used to get objects from configuration layer.  Contains one or more
 * IObjectFactory implementations. Client code can obtain configurable objects by specifying
 * an object 'name' and an object 'type'. The name should be unique within the set of objects 
 * of a given type across all loaded Object Factories (unless the object factory is specified).
 * 
 * @author Ted Sullivan
 */

public class ApplicationManager
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ApplicationManager.class );

  private static ApplicationManager appManager;
  private HashMap<String,IObjectFactory> objFactoryMap = new HashMap<String,IObjectFactory>( );
    
  public static ApplicationManager getInstance( )
  {
    if (appManager != null) return appManager;
        
    appManager = new ApplicationManager( );
    return appManager;
  }
    
  private ApplicationManager(  ) {  }
    
  /**
   * Returns an ObjectFactory with the specified name, Class name and configuration file.
   * Puts the created IObjectFactory into the object factory map so that it can be used in
   * subsequent calls to getApplicationObject and getApplicationObjects methods.
   *
   * @param objFacName  The name of the object factory to create
   * @param className   The fully qualified class name of the object factory implementation
   * @param config      The path to the configuration file for the object factory
   * @return            The instantiated and loaded IObjectFactory instance
   */
  public IObjectFactory createObjectFactory( String objFacName, String className, String config )
  {
    LOG.debug( "createObjectFactory( " + objFacName + ", " + className + ", " + config + " ) - ???" );
        
    try
    {
      IObjectFactory objFac = (IObjectFactory)Class.forName( className ).newInstance( );
      objFac.initialize( config );
        
      addObjectFactory( objFacName, objFac );
      return objFac;
    }
    catch ( Exception e )
    {
      LOG.error( "createObjectFactory " + objFacName + " Failed with exception: " + e );
    }
        
    LOG.debug( "createObjectFactory " + objFacName + " FAILED!" );
    return null;
  }
    
  public void addObjectFactory( String objFacName, IObjectFactory objFactory )
  {
    LOG.debug( "addObjectFactory " + objFacName + " " + objFactory );
    objFactoryMap.put( objFacName, objFactory );
  }
    
    
  public IObjectFactory getObjectFactory( String name ) {
    return objFactoryMap.get( name );
  }
    
  /**
   * Returns an iteration of the names of the IObjectFactory implementations that have been loaded.
   *
   * @return  Iterator of loaded Object Factory names
   */
  public Iterator<String> getObjectFactoryNames(  )
  {
    return objFactoryMap.keySet().iterator();
  }
    
    
  /**
   * Returns a configurable object of a given name and type from all loaded IObjectFactory modules.
   * returns the first object returned by an IObjectFactory that is not null.
   *
   * @param objectName The name of the application object to obtain
   * @param objType    The type of application object requested
   * @return           The application object of the specified name and type or null
   *                   if the object cannot be found in any of the loaded Object Factories
   */
  public Object getApplicationObject( String objectName, String objType )
  {
    LOG.debug( "getApplicationObject( " + objectName + ", " + objType + " )" );

    // ============================================================================
    // Iterate through the set of loaded IObjectFactory implementations.
    // Return the first object that is not-null
    // ============================================================================
    for (Iterator<String> it = objFactoryMap.keySet().iterator(); it.hasNext(); )
    {
      Object obj = getApplicationObject( it.next( ), objectName, objType);
      if ( obj != null)
      {
        return obj;
      }
    }
        
    return null;
  }
    
  /**
   * Returns a list of configurable objects of a given type from all loaded IObjectFactory modules.
   *
   * @param objType  The type of application object requested
   * @return         List of application objects of the specified type
   */
  public List<Object> getApplicationObjects( String objType )
  {
    LOG.debug( "getApplicationObjects( "  + objType + " )" );
        
    ArrayList<Object> allObjects = new ArrayList<Object>();
    Iterator<IObjectFactory> it = objFactoryMap.values().iterator( );
        
    while (it != null && it.hasNext( ) )
    {
      IObjectFactory objFac = it.next( );
            
      List<Object> facObjects = objFac.getApplicationObjects( objType );
      if (facObjects != null)
      {
        allObjects.addAll( facObjects );
      }
    }
        
    return allObjects;
  }
    
  public List<String> getApplicationObjectNames( String objType )
  {
    LOG.debug( "getApplicationObjectNames( "  + objType + " )" );
        
    ArrayList<String> allObjectNames = new ArrayList<String>();
        
    for (Iterator<IObjectFactory> it = objFactoryMap.values().iterator(); it.hasNext( ); )
    {
      IObjectFactory objFac = it.next( );
      List<String> facObjectNames = objFac.getApplicationObjectNames( objType );
      if (facObjectNames != null)
      {
        allObjectNames.addAll( facObjectNames );
      }
    }
        
    return allObjectNames;
  }
    
  public List<String> getApplicationObjectNames( String objectFactoryName, String objType )
  {
    LOG.debug( "getApplicationObjectNames( "  + objectFactoryName + ", " + objType + " )" );
        
    IObjectFactory objFac = (IObjectFactory)objFactoryMap.get( objectFactoryName );
    if (objFac != null)
    {
      return objFac.getApplicationObjectNames( objType );
    }
        
    return null;
  }
    
  /**
   * Returns a list of configurable objects of a given type from the specified IObjectFactory.
   *
   * @param objFacName  The name of the IObjectFactory to use
   * @param objType     The type of application object requested
   * @return            List of application objects of the specified type contained
   *                    in the specified IObjectFactory
   */
  public List<Object> getApplicationObjects( String objFacName, String objType )
  {
    LOG.debug( "getApplicationObjects( " + objFacName + ", " + objType );
        
    IObjectFactory objFac = objFactoryMap.get( objFacName );
    if (objFac != null)
    {
      return objFac.getApplicationObjects( objType );
    }
        
    return null;
  }
    
  /**
   * Returns a configured object of a given name and type from the specified IObjectFactory.
   *
   * @param objFacName  The name of the IObjectFactory to use
   * @param objName     The name of the application object to obtain
   * @param objType     The type of application object requested
   * @return            The application object of the specified name and type or null
   *                    if the object cannot be found in the specified IObjectFactory
   */
  public Object getApplicationObject( String objFacName, String objName, String objType )
  {
    LOG.debug( "getApplicationObject( " + objFacName + ", " + objName + ", " + objType + " )" );
        
    IObjectFactory objFac = objFactoryMap.get( objFacName );
    if (objFac != null)
    {
      return objFac.getApplicationObject( objName, objType );
    }
        
    return null;
  }

}
