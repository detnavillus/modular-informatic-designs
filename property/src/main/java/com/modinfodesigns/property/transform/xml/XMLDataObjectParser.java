package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.utils.DOMMethods;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLDataObjectParser 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( XMLDataObjectParser.class );

  public static DataObject createDataObject( String xmlString )
  {
    if (xmlString == null) return null;
    	
    Document doc = DOMMethods.getDocument( new StringReader( xmlString ) );
    if (doc == null) return null;
    	
    Element documentElement = doc.getDocumentElement( );
    DataObject dObj = null;
    String className = documentElement.getAttribute( "class" );
    if (className != null && className.trim().length() > 0)
    {
      try
      {
        dObj = (DataObject)Class.forName( className ).newInstance( );
      }
      catch ( Exception e )
      {
        LOG.error( "could not create object of class '" + className + "'" );
        dObj = new DataObject( );
      }
    }
    else
    {
      dObj = new DataObject( );
    }
    NodeList propLst = documentElement.getChildNodes(  );
    	
    if (propLst != null && propLst.getLength() > 0)
    {
      for (int i = 0, isz = propLst.getLength(); i < isz; i++)
      {
        Node n = propLst.item( i );
        if (n instanceof Element && ((Element)n).getTagName().equals( "Property" ))
        {
          Element propEl = (Element)n;
          IProperty prop = createProperty( propEl );
          dObj.setProperty( prop );
        }
      }
    }
    return dObj;
  }
    
  public static IProperty createProperty( Element propertyEl )
  {
    String propType = propertyEl.getAttribute( "type" );
    try
    {
      IProperty prop = (IProperty)Class.forName( propType ).newInstance( );
      NodeList chLst = propertyEl.getChildNodes( );
      if (chLst != null)
      {
        for (int i = 0, isz = chLst.getLength(); i < isz; i++)
        {
          Node n = chLst.item( i );
          if (n instanceof Element && ((Element)n).getTagName( ).equals( "Name" ))
          {
            String name = DOMMethods.getText( n );
            prop.setName( name );
          }
          else if (n instanceof Element && ((Element)n).getTagName().equals( "Value" ))
          {
            String value = DOMMethods.getText( n );
            prop.setValue( value , "" );
          }
        }
      }
        
      return prop;
    }
    catch ( Exception e )
    {
      LOG.error( "Could not create Property of type: " + propType );
    }
    	
    return null;
  }

}
