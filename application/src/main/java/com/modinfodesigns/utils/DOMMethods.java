package com.modinfodesigns.utils;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.Writer;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DOMMethods
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DOMMethods.class );
    
  public static Document getDocument( InputStream inStream )
  {
    return getDocument( inStream, false );
  }
    
  public static Document getDocument( InputStream inStream, boolean useNamespace )
  {
    try
    {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      if (useNamespace )
      {
        docBuilderFactory.setNamespaceAware(true);
      }
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      return docBuilder.parse( inStream );
    }
    catch (Exception e)
    {
      LOG.error( "DOMUtils.createDocument( InputStream ) got Exception: " + e );
    }

    return null;
  }
    
  public static Document getDocument( String filePath )
  {
    return getDocument( filePath, "UTF-8" );
  }

  public static Document getDocument( String filePath, String charSet )
  {
    return getDocument( filePath, charSet, false );
  }
    
  public static Document getDocument( String filePath, String charSet, boolean useNamespace )
  {
    try
    {
      InputStream fis = (filePath.startsWith( "/" )) ? new FileInputStream( filePath ) : FileResourceLoader.getResourceAsStream( filePath );
      InputStreamReader xmlSource = new InputStreamReader( fis, charSet );
      return getDocument( xmlSource, charSet, useNamespace );
    }
    catch ( IOException ioe )
    {
      LOG.error( "DOMUtils.createDocument( String ) got IOException: " + ioe );
    }
        
    return null;
  }

  public static Document getDocument( Reader xmlDataReader )
  {
    return getDocument( xmlDataReader, "UTF-8" );
  }

  public static Document getDocument( Reader xmlDataReader, String charSet )
  {
    return getDocument( xmlDataReader, charSet, false );
  }
    
  public static Document getDocument( Reader xmlDataReader, String charSet, boolean useNamespace )
  {
    try
    {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      if (useNamespace)
      {
        docBuilderFactory.setNamespaceAware(true);
      }
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
      InputSource inputSource = new InputSource( xmlDataReader );
      inputSource.setEncoding( charSet );
        
      return docBuilder.parse( inputSource );
    }
    catch (Exception e)
    {
      LOG.error( "DOMUtils.createDocument( Reader ) got Exception: " + e );
    }

    return null;
  }
    
    
  /**
   * returns the text within the DOM Node either as a Text or CData sections.
   */
  public static String getText( Node n )
  {
    return getText( n, false );
  }

  public static String getText( Node n, boolean addLineFeed )
  {
    return getText( n, false, false );
  }
    
  public static String getAttribute( Node n, String attrName )
  {
    NamedNodeMap attrMap = n.getAttributes( );
    if (attrMap == null) return null;
      
    Node attr = attrMap.getNamedItem( attrName );
    if (attr != null && attr instanceof Attr)
    {
      return ((Attr)attr).getValue( );
    }
    return null;
  }
    
  public static String getText( Node n, boolean addLineFeed, boolean addChildText )
  {
    NodeList chillun = n.getChildNodes( );
    StringBuffer sbuf = new StringBuffer( );
    for (int i = 0; i < chillun.getLength(); i++)
    {
      Node cNode = chillun.item(i);
      if (cNode.getNodeType() == Node.CDATA_SECTION_NODE ||
          cNode.getNodeType() == Node.TEXT_NODE )
      {
        sbuf.append( cNode.getNodeValue() );
        if ( addLineFeed ) sbuf.append( "\r\n" );
      }
      else if (cNode.getNodeType() == Node.ELEMENT_NODE && addChildText )
      {
        sbuf.append( getText( cNode, addLineFeed, true ));
      }
    }
        
    return sbuf.toString( );
  }
    
  public static String getXML( Element el, boolean removeHeader )
  {
    StringWriter strWriter = new StringWriter();
    writeXML( el, strWriter );
    String output = strWriter.toString();
    if (removeHeader && output.indexOf( "?>" ) > 0)
    {
      output = new String( output.substring( output.indexOf( "?>" ) + 2 ) ).trim( );
    }
        
    return output;
  }
    
  public static void  writeXML( Element elem, Writer writer )
  {
    try
    {
      Transformer transformer = TransformerFactory.newInstance().newTransformer( );
      DOMSource source = new DOMSource( elem );
      StreamResult result = new StreamResult( writer );
      transformer.transform( source, result );
    }
    catch ( Exception tfe )
    {
      LOG.debug( "Transform exception: " + tfe );
    }
  }
}
