package com.modinfodesigns.app;

import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.FileMethods;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements IObjectFactory using a set of Type directories and file names that
 * correspond to the name and type parameters for getApplicationObject.
 * 
 * Uses superclass ModInfoObjectFactory to create the Objects from XML files.
 * 
 * If caching is set (default == off), objects are instantiated when requested and then cached
 * in the super class (BaseObjectFactory) object map.
 * 
 * @author Ted Sullivan
 */

public class FileSystemObjectFactory extends ModInfoObjectFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FileSystemObjectFactory.class );
  private String baseDirectory;
    
  private boolean caching = false;
    
  @Override
  public void initialize( String configXML )
  {
    LOG.debug( "initialize with " + configXML );
        
    Document doc = DOMMethods.getDocument( new StringReader( configXML ) );
        
    if (doc == null)
    {
      LOG.error( "Could not create Document from " + configXML );
      return;
    }
        
    Element docElem = doc.getDocumentElement( );
        
    super.initializeClassNameMappings( docElem );
        
    this.baseDirectory = docElem.getAttribute( "baseDirectory" );
        
    LOG.debug( "Got baseDirectory = " + baseDirectory );
        
    if ( baseDirectory.startsWith( "./webapps" ) )
    {
      LOG.debug( "Got a relative directory" );
      File file = new File( "." );
      LOG.debug( "basePath = '" + file.getAbsolutePath( ) + "'"  );
        	
      String basePath = file.getAbsolutePath( );
      basePath = basePath.replace( "\\", "/" );
      basePath = basePath.substring( 0, basePath.lastIndexOf( "/" ));
        	
      baseDirectory = baseDirectory.substring( 1 );
      baseDirectory = basePath + baseDirectory;
        	
      LOG.debug( "baseDirectory is set to '" + baseDirectory + "'" );
    }
        
    String tmp = docElem.getAttribute( "caching" );
    if (tmp != null && tmp.equalsIgnoreCase( "true" ))
    {
      this.caching = true;
    }
  }
    
  @Override
  public Object getApplicationObject( String name, String type )
  {
    LOG.debug( "getApplicationObject: " + name + " | " + type );
    	
    if (caching && super.getApplicationObject( name, type ) != null)
    {
      return super.getApplicationObject( name, type );
    }
        
    if (baseDirectory == null)
    {
      LOG.error( "Not initialize correctly! basedirectory is NULL!" );
      return null;
    }
        
    // ===========================================================================
    // find the XML object referred to where type is the sub-directory and name is
    // the name of the XML file - create it using super class createObject method
    // if caching is on - call super.addObject( name, type, Object )
    // ===========================================================================
    String filename = baseDirectory + "/" + type + "/" + name + ".xml";
    LOG.debug( "Reading config file: " + filename );
        
    String config = FileMethods.readFile( filename );
        
    LOG.debug( "Got config = '" + config + "'" );
        
    if (config != null && config.length() > 0)
    {
      Document doc = DOMMethods.getDocument( new StringReader( config ) );
      if (doc != null)
      {
        Element docElem = doc.getDocumentElement( );
        Object obj = createObject( docElem );
        if (obj != null)
        {
          if (caching) addObject( name, type, obj );
          return obj;
        }
        else
        {
          LOG.error( "Could not construct object: type = " + type + " name = " + name );
        }
      }
      else
      {
        LOG.error( "Could not parse XML for object: type = " + type + " name = " + name );
      }
    }
    else
    {
      LOG.error( "Could not find file: " + filename );
    }
        
    return null;
  }
    
    
  /**
   * By convention, the object name is encoded in the XML file name of its configuration
   * file.
   */
  @Override
  public List<Object> getApplicationObjects( String type )
  {
    LOG.debug( "getApplicationObjects( " + type + ")" );
    	
    String typePath = baseDirectory + "/" + type;
    String[]  fileList = FileMethods.getFileList( typePath, false );
    if (fileList != null && fileList.length > 0)
    {
      ArrayList<Object> appObjects = new ArrayList<Object>( );
      for (int i = 0, isz = fileList.length; i < isz; i++)
      {
        String objName = new String( fileList[i].substring( 0, fileList[i].indexOf( ".xml" )));
        Object appObj = getApplicationObject( objName, type );
        if (appObj != null)
        {
          appObjects.add( appObj );
        }
      }
        	
      return appObjects;
    }
        
    return null;
  }

  @Override
  public List<String> getApplicationObjectNames( String type )
  {
    String typePath = baseDirectory + "/" + type;
    String[]  fileList = FileMethods.getFileList( typePath, false );
    if (fileList != null && fileList.length > 0)
    {
      ArrayList<String> appObjectNames = new ArrayList<String>( );
      for (int i = 0, isz = fileList.length; i < isz; i++)
      {
        String objName = new String( fileList[i].substring( 0, fileList[i].indexOf( ".xml" )));
        appObjectNames.add( objName );
      }
        	
      return appObjectNames;
    }
        
    return null;
  }
}
