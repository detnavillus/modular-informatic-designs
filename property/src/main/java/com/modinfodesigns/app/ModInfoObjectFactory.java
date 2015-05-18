package com.modinfodesigns.app;

import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.utils.FileMethods;

import java.io.StringReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Object Factory Implementation.
 * 
 * XML Schema:
 * 
 * @author Ted Sullivan
 */

public class ModInfoObjectFactory extends BaseObjectFactory implements ServletContextListener
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ModInfoObjectFactory.class );
    
  private String modInfoObjectFactoryName = "ModInfoDesignsObjectFactory";
    
  private String namePrefix = "object_";
    
  private HashMap<String,String> interfaceClassMap = new HashMap<String,String>( );
  private HashMap<String,String> classNameMap = new HashMap<String,String>();
    
  private static final String[] INTERFACE_NAME_MAP = { "DataSource",              "com.modinfodesigns.pipeline.source.IDataObjectSource",
                                                       "DataProcessor",           "com.modinfodesigns.pipeline.process.IDataObjectProcessor",
                                                       "DataTransform",           "com.modinfodesigns.property.transform.IPropertyHolderTransform",
                                                       "DataSourceFactory",       "com.modinfodesigns.app.persistence.database.IDataSourceFactory",
                                                       "DataListRenderer",        "com.modinfodesigns.property.transform.string.IDataListRenderer",
                                                       "ControllerAction",        "com.modinfodesigns.app.property.controller.IControllerAction",
                                                       "ContentStreamFilter",     "com.modinfodesigns.network.io.IContentStreamFilter",
                                                       "EntityExtractor",         "com.modinfodesigns.entity.IEntityExtractor",
                                                       "Finder",                  "com.modinfodesigns.search.IFinder",
                                                       "FinderIndexFeeder",       "com.modinfodesigns.search.IFinderIndexFeeder",
                                                       "PropertyTransform",       "com.modinfodesigns.property.transform.IPropertyTransform",
                                                       "StringTransform",         "com.modinfodesigns.property.transform.string.IStringTransform",
                                                       "SearchDataTransform",     "com.modinfodesigns.app.search.model.transform.ISearchPageDataTransform",
                                                       "SearchDataRenderer",      "com.modinfodesigns.app.search.userInterface.ISearchPageDataRenderer",
                                                       "Property",                "com.modinfodesigns.property.IProperty",
                                                       "PropertyMatcher",         "com.modinfodesigns.property.compare.IPropertyMatcher",
                                                       "PropertyRenderer",        "com.modinfodesigns.app.property.userInterface.IPropertyRenderer",
                                                       "DataMatcher",             "com.modinfodesigns.property.compare.IPropertyHolderMatcher",
                                                       "IndexMatcher",            "com.modinfodesigns.classify.IIndexMatcher",
                                                       "IndexMatcherFactory",     "com.modinfodesigns.classify.IIndexMatcherFactory",
                                                       "QueryTreeSetBuilder",     "com.modinfodesigns.search.IQueryTreeSetBuilder",
                                                       "Query",                   "com.modinfodesigns.search.IQuery",
                                                       "QueryFilter",             "com.modinfodesigns.search.IQueryFilter",
                                                       "QueryTransform",          "com.modinfodesigns.app.search.controller.IQueryTransform",
                                                       "ResultListFilter",        "com.modinfodesigns.search.IResultListFilter",
                                                       "ResultTransform",         "com.modinfodesigns.app.search.model.transform.IResultTransform",
                                                       "AjaxHandler",             "com.modinfodesigns.app.search.controller.IAjaxHandler",
                                                       "RequestHandler",          "com.modinfodesigns.network.http.IHttpRequestHandler",
                                                       "TaxonomyBuilder",         "com.modinfodesigns.ontology.ITaxonomyBuilder",
                                                       "TaxonomyRenderer",        "com.modinfodesigns.app.ontology.userInterface.ITaxonomyRenderer"
 };
    
  private static final String[] PROPERTY_TYPE_MAP = { "String",     "com.modinfodesigns.property.string.StringProperty",
                                                      "StringList", "com.modinfodesigns.property.StringListProperty",
                                                      "Date",       "com.modinfodesigns.property.time.DateProperty",
                                                      "Integer",    "com.modinfodesigns.property.quantity.IntegerProperty" };
    
  private HashMap<String,String> propertyTypeMap = new HashMap<String,String>();
    
  public void setNamePrefix( String namePrefix )
  {
    this.namePrefix = namePrefix;
  }
    
  @Override
  public void initialize( String configXML )
  {
    LOG.debug( "initialize " + configXML );
        
    Document doc = DOMMethods.getDocument( new StringReader( configXML ) );
        
    if (doc == null)
    {
      LOG.error( "Could not create Document from " + configXML );
      return;
    }
        
    Element docElem = doc.getDocumentElement( );
        
    int elem_num = 0;
        
    initializeClassNameMappings( docElem );
        
    NodeList childNodes = docElem.getChildNodes( );
    for (int i = 0, nsz = childNodes.getLength( ); i < nsz; i++)
    {
      Node n = childNodes.item( i );
      if (n instanceof Element )
      {
        Element el = (Element)n;
        Object obj = createObject( el );
                
        if (obj != null)
        {
          String type = el.getAttribute( "type" );
          if (type == null || type.trim().length() == 0)
          {
            type = el.getTagName( );
          }
                    
          String name = el.getAttribute( "name" );
          if (name == null || name.trim().length() == 0)
          {
            name = namePrefix + Integer.toString( elem_num++ );
          }
                
          addObject( name, type, obj );
        }
      }
    }
  }
    
  protected void initializeClassNameMappings( Element configElem )
  {
    LOG.debug( "initializeClassNameMappings..." );
    	
    for (int i = 0, isz = INTERFACE_NAME_MAP.length; i < isz; i += 2)
    {
      interfaceClassMap.put( INTERFACE_NAME_MAP[i], INTERFACE_NAME_MAP[i+1] );
    }
        
    for (int i = 0, isz = PROPERTY_TYPE_MAP.length; i < isz; i += 2)
    {
      propertyTypeMap.put( PROPERTY_TYPE_MAP[i], PROPERTY_TYPE_MAP[i+1] );
    }
        
    NodeList classMapList = configElem.getElementsByTagName( "ClassNameMapping" );
    if (classMapList != null && classMapList.getLength() > 0)
    {
      for (int i = 0, isz = classMapList.getLength(); i < isz; i++)
      {
        Element classNameMapEl = (Element)classMapList.item( i );
        String tagName = classNameMapEl.getAttribute( "elementName" );
        String className = classNameMapEl.getAttribute( "className" );
                
        if (className != null && className.trim().length() > 0)
        {
          LOG.debug( "Adding mapping: " + tagName + " = " + className );
          classNameMap.put( tagName, className );
        }
                
        String interfaceName = classNameMapEl.getAttribute( "interfaceName" );
        if (interfaceName != null && interfaceName.trim().length() > 0)
        {
          LOG.debug( "Adding Interface Mapping " + tagName + " -> " + interfaceName );
          interfaceClassMap.put( tagName, interfaceName );
        }
      }
    }
  }
    
  protected Object createObject( Element elem )
  {
    if (elem.getTagName().equals( "ClassNameMapping" ))
    {
      return null; // Not an error ...
    }
        
    LOG.debug( "createObject" );
    String className = elem.getAttribute( "class" );
    if (className == null || className.trim().length() == 0)
    {
      className = Constants.getClassName( elem.getTagName( ) );
    }
        
    if ((className == null || className.trim().length() == 0)
        && classNameMap.keySet().contains( elem.getTagName( ) ))
    {
      className = classNameMap.get( elem.getTagName( ) );
    }
        
    if ( className == null || className.trim().length() == 0)
    {
      // if the tag has text in it.  try this
      // return new String(  )...
      String tagData = DOMMethods.getText( elem );
      if (tagData != null && tagData.trim().length() > 0)
      {
        return tagData;
      }
        
      LOG.error( "Could not create object - no classname for " + elem.getTagName( ) );
      return null;
    }

    // if it is a DataList object ---
    // get the type (used for the method)
    // get the nested objects as DataObjects
    if (className.equals("com.modinfodesigns.property.DataList" ))
    {
      LOG.debug( "Creating DataList object" );
      return createDataList( elem );
    }
    else if (className.equals( "com.modinfodesigns.property.IProperty" ))
    {
      return createProperty( elem );
    }
        
    LOG.debug( "Creating object of class " + className );
        
    Object obj = null;
    try
    {
      obj = Class.forName( className ).newInstance( );
    }
    catch ( Exception e )
    {
      LOG.error( "Could not instantiate " + className );
    }
        
    // ------------------------------------------------------------------------------
    // Get all of the Attributes in the Element
    // Find Bean Set methods for these attributes if they exist
    // ------------------------------------------------------------------------------
    NamedNodeMap attrs = elem.getAttributes( );
    if (attrs != null)
    {
      for (int i = 0, isz = attrs.getLength(); i < isz; i++)
      {
        Attr node = (Attr)attrs.item(i);
        String attrName = node.getName( );
        if (attrName.equals( "class" ) == false)
        {
          String attrValue = node.getValue( );
            
          String method = getMethodName( attrName, false );
          LOG.debug( "executing method: " + method );
          executeMethod( obj, attrValue, method, null );
        }
      }
    }
        
    // ------------------------------------------------------------------------------
    // Get the child nodes, for each Element get the TagName see if there is a set
    // or an add method for this child Object
    // ------------------------------------------------------------------------------
    NodeList childNodes = elem.getChildNodes( );
    if (childNodes != null && childNodes.getLength() > 0)
    {
      for (int i = 0, csz = childNodes.getLength( ); i < csz; i++)
      {
        Node n = childNodes.item( i );
        if (n instanceof Element)
        {
          Element chEl = (Element)n;
                    
          LOG.debug( "Creating Child Object ... " );
          Object chObj = createObject( chEl );
          if (chObj != null)
          {
            // Type is the parameter type
            String chType = chEl.getAttribute( "type" );
            if (chType == null || chType.trim().length() == 0 || chEl.getTagName().equals( "Property" ))
            {
              chType = chEl.getTagName( );
            }
                    
            //String addSt = chEl.getAttribute( "add" );
            // boolean isAdd = (addSt != null && addSt.equalsIgnoreCase( "true" ) );
                    
            String method = getMethodName( chType, true );
            LOG.debug( "Got method name = " + method );
            if (executeMethod( obj, chObj, method, chType ) == false)
            {
              method = getMethodName( chType, false );
              if (executeMethod( obj, chObj, method, chType ) == false)
              {
                LOG.error( "Could not execute method for type: " + chType );
              }
            }
          }
          else
          {
            LOG.error( "Could not create Child Object!" );
          }
        }
      }
    }
        
    return obj;
  }
    
  // --------------------------------------------------------------------
  // Change type name to [set|add]TypeName (upper case first character)
  // --------------------------------------------------------------------
  private String getMethodName( String typeName, boolean isAdd )
  {
    String method = (isAdd) ? "add" : "set";
    method = method + StringMethods.initialCaps( typeName );
    return method;
  }
    
  protected DataList createDataList( Element elem )
  {
    LOG.debug( "createDataList" );
        
    DataList dl = new DataList( );
    NodeList chNodes = elem.getChildNodes( );
    if (chNodes != null && chNodes.getLength() > 0)
    {
      for (int i = 0, csz = chNodes.getLength(); i < csz; i++)
      {
        Node n = chNodes.item( i );
        if (n instanceof Element )
        {
          Element el = (Element)n;
          DataObject dObj = new DataObject( );
          dl.addDataObject( dObj );
            
          NamedNodeMap attrs = el.getAttributes( );
          if (attrs != null)
          {
            for (int a = 0, asz = attrs.getLength(); a < asz; a++)
            {
              Attr node = (Attr)attrs.item( a );
              String attrName = node.getName( );
              String attrValue = node.getValue( );
                
              dObj.addProperty( new StringProperty( attrName, attrValue ) );
            }
          }
                    
          // ====================================================
          // Add any generic Objects that the object might need: It
          // will "know" what to ask for ...
          // ====================================================
          NodeList dObjLst = el.getChildNodes( );
          if (dObjLst != null && dObjLst.getLength() > 0)
          {
            for (int d = 0, dsz = dObjLst.getLength(); d < dsz; d++)
            {
              Node dn = dObjLst.item( d );
              if (dn instanceof Element)
              {
                Element del = (Element)dn;
                String tagName = del.getTagName( );
                Object obj = createObject( del );
                if (obj != null)
                {
                  dObj.addObject( tagName, obj );
                }
              }
            }
          }
        }
      }
    }
        
    return dl;
  }
    
  protected IProperty createProperty( Element elem )
  {
    String propName = elem.getAttribute( "name" );
    String propValue = elem.getAttribute( "value" );
    String propFormat = elem.getAttribute( "format" );
        
    if (propFormat != null && propFormat.trim().length() == 0)
    {
      propFormat = null;
    }
        
    String propClass = elem.getAttribute( "class" );
    if (propClass == null || propClass.trim().length() == 0)
    {
      String propType = elem.getAttribute( "type" );
      if (propType != null)
      {
        propClass = propertyTypeMap.get( propType );
      }
    }
        
    if (propClass != null)
    {
      try
      {
        IProperty prop = (IProperty)Class.forName( propClass ).newInstance();
        prop.setName( propName );
        prop.setValue( propValue, propFormat );
          
        return prop;
      }
      catch ( Exception e )
      {
            
      }
    }
    
    return null;
  }
    
    
  private boolean executeMethod( Object targetOb, Object paramOb, String methodName, String paramType )
  {
    LOG.debug( "executeMethod\r\n    " + targetOb
                                       + "\r\n    " + paramOb
                                       + "\r\n    " + methodName
                                       + "\r\n    " + paramType );
    if (targetOb == null || paramOb == null || methodName == null )
    {
      LOG.debug( "Cannot execute method!" );
      return false;
    }
        
    try
    {
      Object[] params = new Object[1];
      params[0] = paramOb;
            
      @SuppressWarnings( "rawtypes" )
      Class[] paramArray = new Class[1];
      if (paramType != null && interfaceClassMap.keySet().contains( paramType ))
      {
        String interfaceClass = interfaceClassMap.get( paramType );
        LOG.debug( "Using Interface Type: " + interfaceClass );
        paramArray[0] = Class.forName( interfaceClass );
      }
      else if (paramType != null && classNameMap.keySet().contains( paramType ))
      {
        String className = classNameMap.get( paramType );
        LOG.debug( "using ClassName = " + className );
        paramArray[0] = Class.forName( className );
      }
      else
      {
        paramArray[0] = paramOb.getClass( );
      }
            
      Method m = targetOb.getClass().getMethod( methodName, paramArray );
      if (m != null)
      {
        m.invoke( targetOb, params );
        return true;
      }
      else
      {
        LOG.debug( "No such method ..." );
        return false;
      }
    }
    catch (NoSuchMethodException e )
    {
      LOG.error( "execute " + methodName + " threw NoSuchMethodException " + e.getMessage( ) );
    }
    catch ( InvocationTargetException e )
    {
      LOG.error( "execute " + methodName + " threw InvocationTargetException " + e.getMessage( ) );
    }
    catch ( IllegalAccessException e )
    {
      LOG.error( "execute " + methodName + " threw IllegalAccessException " + e.getMessage( ) );
    }
    catch (ClassNotFoundException e )
    {
      LOG.error( "execute " + methodName + " target = " + targetOb + " threw ClassNotFoundException " + e.getMessage( ) );
    }
        
    return false;
  }

  @Override
  public void contextInitialized( ServletContextEvent scEvent )
  {
    ServletContext sc = scEvent.getServletContext( );
        
    String fileName = sc.getInitParameter( "ModInfoConfigFile" );
    // Read the file and initialize
    String configXML = FileMethods.readFile( fileName );
    initialize( configXML );
    
    ApplicationManager appManager = ApplicationManager.getInstance( );
    appManager.addObjectFactory( modInfoObjectFactoryName, this );
  }
    
  @Override
  public void contextDestroyed( ServletContextEvent scEvent )
  {
        
  }


}
