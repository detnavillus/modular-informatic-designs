package com.modinfodesigns.network.http;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a hyperlink from a HttpRequestData object and two Property holder objects: one that contains
 * a set of IProperty name=value pairs to remove and another with a set of parameters to add.
 * 
 * Parameter removal can be of a single parameter, a single parameter value or all but a set of parameters.
 * Remove a single parameter
 *     prop name = name of parameter
 *     prop value = name of specific parameter value to remove or 'ALL' to remove all instances
 *                  of this parameter.
 *                  
 * Remove all parameters
 *     prop name = 'ALL'
 *     
 * Remove all but a list of parameters to keep (Include Fields)
 *     prop name = 'NOT'
 *     prop value - list of parameters to retain (StringListProperty)
 *     
 * Property names will be prepared for URL by replacing spaces with '_'  Values will be URL Encoded.
 * 
 * @author Ted Sullivan
 */

public class HyperlinkRenderer
{
  private transient static final Logger LOG = LoggerFactory.getLogger( HyperlinkRenderer.class );

  public static final String ALL = "ALL";
  public static final String NOT = "NOT";
    
  private static String charSet = "UTF-8";
    
  public static String renderHyperlink( HttpRequestData httpRequestData, IProperty addProperty, IProperty removeProperty )
  {
    if (httpRequestData == null) return "";
        
    DataObject requestParams = httpRequestData.getRequestParameters( );
    if (requestParams != null)
    {
      DataObject changedParams = (DataObject)requestParams.copy( );
            
      if (removeProperty != null)
      {
        removeParams( changedParams, removeProperty );
      }
            
      if (addProperty != null)
      {
        changedParams.removeProperty( addProperty.getName( ) );
        addParams( changedParams, addProperty );
      }
            
      LOG.debug( "rendering " + changedParams.getValue( ) );
      String link = render( httpRequestData, changedParams );
      LOG.debug( "Got link = " + link );
      return link;
    }
    else
    {
      LOG.error( "Request Data is NULL!" );
    }
        
    return httpRequestData.getRequestURL( );
  }
    
  private static void removeParams( DataObject params, IProperty removeProperty )
  {
    if (params == null || removeProperty == null) return;
    	
    LOG.debug( "removeParams: " + params.getValue( ) + " minus " + removeProperty.getName( ) + " = " + removeProperty.getValue( ) );
        
    if (removeProperty instanceof IPropertyHolder)
    {
      IPropertyHolder propHolder = (IPropertyHolder)removeProperty;
      Iterator<IProperty> propIt = propHolder.getProperties( );
      while( propIt != null && propIt.hasNext() )
      {
        IProperty prop = propIt.next( );
        removeParams( params, prop );
      }
    }
    else if (removeProperty instanceof PropertyList)
    {
      PropertyList removeList = (PropertyList)removeProperty;
      Iterator<IProperty> propIt = removeList.getProperties();
      while ( propIt != null && propIt.hasNext() )
      {
        removeParams( params, propIt.next() );
      }
    }
    else
    {
      String propName = removeProperty.getName( );
      if (propName == null) return;
            
      if (propName.equals( ALL ) || propName.equals( NOT ))
      {
        ArrayList<String> propsToRemove = new ArrayList<String>( );
        Iterator<String> propNameIt = params.getPropertyNames();
        HashSet<String> propVals = (propName.equals( NOT )) ? getPropertyValues( removeProperty ) : null;

        while( propNameIt != null && propNameIt.hasNext() )
        {
          String reqProp = propNameIt.next();
          if (propVals == null || !propVals.contains( reqProp ))
          {
            propsToRemove.add( reqProp );
          }
        }
                
        for (int i = 0; i < propsToRemove.size(); i++)
        {
          params.removeProperty( propsToRemove.get( i ) );
        }
      }
      else
      {
        String propVal = removeProperty.getValue( );
        if (propVal != null && propVal.equals( ALL ))
        {
          // remove this property from the request...
          params.removeProperty( propName );
        }
        else
        {
          // remove the property (from the StringListProperty)
          // that has this value ...
          HashSet<String> propVals = getPropertyValues( removeProperty );
            
          IProperty requestProp = params.getProperty( propName );
          if (requestProp != null)
          {
            if (requestProp instanceof StringListProperty)
            {
              // create new StringListProperty, add prop values that
              // are not in the propVals list ...
              StringListProperty requested = (StringListProperty)requestProp;
              StringListProperty edited = new StringListProperty( propName );
              String[] valList = requested.getStringList( );
              if (valList != null)
              {
                for (int i = 0; i < valList.length; i++)
                {
                  if (propVals == null || !propVals.contains( valList[i] ))
                  {
                    edited.addString( valList[i] );
                  }
                }
                                
                params.setProperty( edited );
              }
            }
            else if (propVals != null && propVals.contains( requestProp.getValue()) )
            {
              params.removeProperty( propName );
            }
          }
        }
      }
    }
        
    LOG.debug( "After remove: " + params.getValue( ) );
  }
    
  private static HashSet<String> getPropertyValues( IProperty property )
  {
    HashSet<String> valueSet = new HashSet<String>( );
    if (property == null) return valueSet;
        
    if (property instanceof StringListProperty)
    {
      StringListProperty slp = (StringListProperty)property;
      String[] values = slp.getStringList( );
      for (int i = 0; i < values.length; i++)
      {
        valueSet.add( values[i] );
      }
    }
    else
    {
      valueSet.add( property.getValue() );
    }
        
    return valueSet;
  }
    
  private static void addParams( DataObject params, IProperty addProperty )
  {
    if (addProperty instanceof IPropertyHolder)
    {
      IPropertyHolder propHolder = (IPropertyHolder)addProperty;
      Iterator<IProperty> propIt = propHolder.getProperties( );
      while( propIt != null && propIt.hasNext() )
      {
        IProperty prop = propIt.next( );
        addParams( params, prop );
      }
    }
    else
    {
      params.addProperty( addProperty );
    }
  }
    
  public static String render( HttpRequestData httpRequestData, DataObject params )
  {
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( httpRequestData.getRequestURL( ) );
    Iterator<IProperty> propIt = params.getProperties( );
    boolean isFirst = true;
    LOG.debug( "render( ) DataObject " + params  + " " + isFirst );
    while( propIt != null && propIt.hasNext() )
    {
      IProperty prop = propIt.next( );
      if ( renderProperty( prop, isFirst, strbuilder ) )
      {
        isFirst = false;
      }
    }
        
    LOG.debug( "render DONE." );
    return strbuilder.toString();
  }
    
  public static String renderHyperlink( HttpRequestData httpRequestData, List<IProperty> params )
  {
    LOG.debug( "renderHyperlink List<IProperty> " + params );
        
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( httpRequestData.getRequestURL( ) );
    boolean isFirst = true;
    for (int i = 0; i < params.size(); i++)
    {
      IProperty prop = params.get( i );
      if (renderProperty( prop, isFirst, strbuilder ))
      {
        isFirst = false;
      }
    }
        
    return strbuilder.toString();
  }
    
  private static boolean renderProperty( IProperty prop, boolean isFirst, StringBuilder strbuilder )
  {
    boolean iAmFirst = isFirst;
    LOG.debug( "renderProperty " + iAmFirst );
    	
    String propName = getParamName( prop );
    if (propName != null)
    {
      if ( prop instanceof PropertyList )
      {
        PropertyList propVals = (PropertyList)prop;
        Iterator<IProperty> propIt = propVals.getProperties( );
        while (propIt != null && propIt.hasNext() )
        {
          IProperty lstProp = propIt.next( );
          if ( renderProperty( lstProp, iAmFirst, strbuilder ) )
          {
            iAmFirst = false;
          }
        }
        	    
        return (!iAmFirst);
      }
      else if ( prop instanceof StringListProperty )
      {
        StringListProperty slp = (StringListProperty)prop;
        String[] values = slp.getStringList( );
        for (int i = 0; i < values.length; i++)
        {
          appendParam( propName, values[i], iAmFirst, strbuilder );
          iAmFirst = false;
        }
            	
        return values.length > 0;
      }
      else if (prop instanceof IntrinsicPropertyDelegate == false)
      {
        String propValue = getParamValue( prop );
        appendParam( propName, propValue, isFirst, strbuilder );
        return true;
      }
    }
        
    return false;
  }
    
  private static void appendParam( String propName, String propValue, boolean isFirst, StringBuilder strbuilder )
  {
    LOG.debug( "APPEND: propName: " + propName + " = " + propValue + " " + isFirst );
    strbuilder.append( (isFirst) ? "?" : "&" ).append( propName ).append( "=" ).append( propValue );
  }
    
  private static String getParamName( IProperty prop )
  {
    String propName = prop.getName( );
        
    if (propName != null && propName.indexOf( " " ) > 0)
    {
      propName = StringTransform.replaceSubstring( propName, " ", "" );
    }
        
    return propName;
  }
    
 
  private static String getParamValue( IProperty prop )
  {
    String propValue = prop.getValue( );
    if (propValue == null) return "";
        
    try
    {
      propValue = URLEncoder.encode( propValue, charSet );
    }
    catch ( Exception e )
    {
    }
        
    return propValue;
  }

}
