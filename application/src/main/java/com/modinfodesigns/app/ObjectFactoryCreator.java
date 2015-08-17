package com.modinfodesigns.app;

import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.FileMethods;

import java.io.StringReader;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of ObjectFactory implementations from a configuration file and loads them
 * into the ApplicationManager.
 * 
 * In J2EE Applications, the ObjectFactoryCreator is activated by setting up a context parameter
 * pointing to the configuration file for ObjectFactoryCreator and adding ObjectFactoryCreator as a listener
 * in the web.xml file. This ensures that the ObjectFactoryCreator will be initialized when the
 * Web Application is accessed.
 * 
 * <pre>
 *   &lt;context-param>
 *    &lt;param-name>ConfigurationPath&lt;/param-name>
 *    &lt;param-value>[path to ObjectFactoryCreator configuration file ]&lt;/param-value>
 *   &lt;/context-param>
 *
 *   &lt;listener>
 *    &lt;listener-class>com.modinfodesigns.app.ObjectFactoryCreator&lt;/listener-class>
 *   &lt;/listener>
 * </pre>
 * 
 * <u>XML Configuration:</u>
 * <pre>
 *   &lt;ObjectFactoryCreator>
 *     &lt;ObjectFactory factoryName="[ factory name ]"
 *                       factoryClass="[ fully qualified class name ]"
 *                       configFile="[ location of factory configuration file ]" />
 *
 *     &lt;!-- One or More debug classes -->
 *     &lt;DebugClass>[ name of debug class or 'none' to shut of debugging&lt;/DebugClass>
 *   &lt;/ObjectFactoryCreator>
 *   
 * @author Ted Sullivan
 */

public class ObjectFactoryCreator  implements ServletContextListener
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ObjectFactoryCreator.class );
    
  public static final String CONFIG_PATH = "ConfigurationPath";
    
  public static boolean initialized = false;
  public static Object LockObject = new String( "Lock" );
    
    
  /**
   * Initialize the Object Factories that are specified in the
   * Configuration XML.
   *
   * @param config
   */
  public static void initialize( String configFile )
  {
    LOG.debug( "initialize( )..." );
    	
    if (initialized) return;
        
    synchronized( LockObject )
    {
      if (initialized) return;
        
      String configPath = null;

      ApplicationManager appManager = ApplicationManager.getInstance( );
        
      String configXML = FileMethods.readFile( configFile );
      if (configXML == null)
      {
        LOG.error( "Could not find config file: " + configFile );
        return;
      }

      LOG.debug( "Got configFile = " + configFile );
            
      if (configFile.startsWith( "./webapps" ))
      {
        File basePathFile = new File( "." );
        String basePath = basePathFile.getAbsolutePath( );
        // replace the "." with the base path
        basePath = basePath.replace( "\\", "/" );
        basePath = basePath.substring( 0, basePath.lastIndexOf( "/" ));
            	
        LOG.debug( "Got basePath = '" + basePath + "'" );
            	
        configFile = configFile.substring( 1 );
        configFile = basePath + configFile;
            	
        LOG.debug( "Got configFile = '" + configFile + "'" );
            	
        configPath = configFile.substring( 0, configFile.lastIndexOf( "/" ) );
            	
        LOG.debug( "Got Config Path = '" + configPath + "'" );
      }
            
      Document doc = DOMMethods.getDocument( new StringReader( configXML ) );
          
      if (doc == null)
      {
        LOG.error( "Could not create Document from " + configFile );
        LOG.debug( "Could not create Document from " + configFile );
        return;
      }
            
      LOG.debug( "Got DOM Document " + doc );
        
      Element docElem = doc.getDocumentElement( );
        
      NodeList facList = docElem.getElementsByTagName( "ObjectFactory" );
      if (facList != null && facList.getLength() > 0)
      {
        for (int i = 0; i < facList.getLength(); i++)
        {
          Element objElem = (Element)facList.item( i );
          String facName   = objElem.getAttribute( "factoryName" );
          String facClass  = objElem.getAttribute( "factoryClass" );
          String objConfig = objElem.getAttribute( "configFile" );
            
          if (objConfig != null)
          {
            if (objConfig.startsWith( "./" ))
            {
              if (configPath != null)
              {
                objConfig = configPath + objConfig.substring( 1 );
                LOG.debug( "Reconfigured object config to " + objConfig );
              }
              else
              {
                // the the base path as before ...
              }
            }
                
            LOG.debug( "createObjectFactory( " + facName + "," + facClass + "," + objConfig );
            String factoryXML = FileMethods.readFile( objConfig );
            if (factoryXML != null)
            {
              appManager.createObjectFactory( facName, facClass, factoryXML );
            }
            else
            {
              LOG.error( "Could not read file: " + objConfig );
            }
          }
          else
          {
            String factoryXML = DOMMethods.getText( objElem );
            appManager.createObjectFactory( facName, facClass, factoryXML );
          }
        }
      }
    }
  }
    
  @Override
  public void contextInitialized( ServletContextEvent servletContext )
  {
    String configPath = servletContext.getServletContext().getInitParameter( CONFIG_PATH );
    if (configPath != null)
    {
      initialize( configPath );
    }
    else
    {
      LOG.error( "Cannot find CONFIG_PATH!" );
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0)
  {
        
  }

}
