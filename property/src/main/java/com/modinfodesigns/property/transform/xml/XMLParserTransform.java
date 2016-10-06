package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataObjectBuilder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.transform.BasePropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.PropertyValidationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

import java.io.StringReader;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms a StringProperty with an XML String into a DataObject with the same structure as the XML.
 * XML Attributes are set to properties, nested Elements are set to nested DataObjects. Text elements 
 * are set to a special Property called 'text'.
 
 * Example:
 * <pre>
 *  &lt;record firstName="John" lastName="Smith" age="25" >
 *    &lt;address streetAddress="21 2nd Street" city="New York" state="NY" postalCode="10021" />
 *    &lt;phoneNumber type="home" number="212 555-1234" />
 *    &lt;phoneNumber type="fax" number="646 555-4567" />
 *  &lt;/record>
 * </pre>
 *
 * @author Ted Sullivan
 */

public class XMLParserTransform extends BasePropertyTransform implements IDataObjectBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( XMLParserTransform.class );

  // Optional DataObject subclass - (e.g. a DataObjectBean class )
  private String dataObjectClass;
	
	
  public void setDataObjectClass( String dataObjectClass )
  {
    this.dataObjectClass = dataObjectClass;
  }
	
	
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    String xmlString = input.getValue( );
    DataObject xmlObject = createDataObject( xmlString );
    if (xmlObject == null)
    {
      throw new PropertyTransformException( "Could not parse xmlString " );
    }
		
    return xmlObject;
  }

  @Override
  public DataObject createDataObject( String xmlString )
  {
    LOG.debug( "createDataObject" );
    	
    DataObjectCreator doc = new DataObjectCreator( );
    formatDataObjectCreator( doc, new StringReader( xmlString ) );
    return doc.getDataObject( );
  }
    
  public DataObject createDataObject( Reader xmlReader )
  {
    DataObjectCreator doc = new DataObjectCreator( );
    formatDataObjectCreator( doc, xmlReader );
    LOG.info( "returning DataObject" );
    return doc.getDataObject( );
  }
    
  public void formatDataObject( DataObject dobj, String xmlString )
  {
    DataObjectCreator doc = new DataObjectCreator( dobj );
    formatDataObjectCreator( doc, new StringReader( xmlString ) );
  }
    
  public DataObject createDataObject( InputStream xmlStream )
  {
    return createDataObject( new InputStreamReader( xmlStream ) );
  }
    
  private void formatDataObjectCreator( DataObjectCreator doc, Reader xmlReader )
  {
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
        
      InputSource is = new InputSource( xmlReader );
    		
      saxParser.parse( is, doc );
    }
    catch ( Exception e )
    {
      LOG.error( "Got Exception: " + e );
      e.printStackTrace( );
    }
  }
    
    
  class DataObjectCreator extends DefaultHandler
  {
    private DataObject rootObj;
    private DataObject currObject;
    private String currParentName;
    	
    private DataObject initRoot;
    	
    DataObjectCreator(  ) {  }
    
    DataObjectCreator( DataObject rootObj )
    {
      this.initRoot = rootObj;
    }

    private Stack<DataObject> parentStack = new Stack<DataObject>( );
    	
    DataObject getDataObject( )
    {
      return this.rootObj;
    }
    	
    @Override
    public void startElement( String namespaceURI, String localName, String qName, Attributes atts )
    {
      LOG.debug( "startElement: " + qName );
    		
      if (this.rootObj == null)
      {
        if (initRoot != null)
        {
          rootObj = initRoot;
          rootObj.setName( qName );
        }
        else
        {
          this.rootObj = createDataObject( );
          this.rootObj.setName( qName );
        }
        currObject = this.rootObj;
      }
      else
      {
        currObject = new DataObject( );
        currObject.setName( qName );
      }

      LOG.debug( "Pushing '" + qName + "' " + currObject );
      parentStack.push( currObject );
      currParentName = qName;
    		
      LOG.debug( "parentStack has " + parentStack.size( ) );
    		
      // add the attributes as StringProperty to current object
      if (atts != null)
      {
        for (int i = 0, isz = atts.getLength(); i < isz; i++ )
        {
          StringProperty stringProp = new StringProperty( );
          stringProp.setName( atts.getQName( i ) );
          
          try
          {
            stringProp.setValue( atts.getValue( i ), null );
            currObject.addProperty( stringProp );
            LOG.debug( "Adding property: " + stringProp.getName( ) + " : " + stringProp.getValue( ) );
          }
          catch ( PropertyValidationException pve )
          {

          }
        }
      }
    		
      LOG.debug( "currObject: " + currObject.getValue( ) );
    }
    	
    	
    @Override
    public void characters(char[] ch, int start, int length)
    {
      String charSt = new String( ch, start, length );
      if (charSt.trim().length() == 0) return;
        
      LOG.debug( "characters: " + charSt );
      if (charSt.indexOf( "&" ) >= 0) {
        LOG.debug( "got ampersand st: '" + charSt + "'" );
      }

      StringListProperty slp = (StringListProperty)currObject.getProperty( "text" );
      if (slp == null)
      {
        slp = new StringListProperty( );
        slp.setName( "text" );
        currObject.addProperty( slp );
      }
    		
      slp.addString( charSt );
    }
    	
    	
    @Override
    public  void endElement(String namespaceURI, String localName, String qName )
    {
      LOG.debug( "endElement localName '" + localName + "' qName '" + qName + "'" );
      LOG.debug( "parentStack has " + parentStack.size( ) );
    		
      DataObject lastParent = (parentStack != null && parentStack.size() > 0)
                            ? parentStack.peek( )
                            : null;
        
      if (lastParent != null && currParentName != null && currParentName.equals( qName ))
      {
        if (parentStack != null && parentStack.size() > 0)
        {
          LOG.debug( "Popping '" + currParentName + "'" );
          parentStack.pop( );
          LOG.debug( "parentStack has " + parentStack.size( ) );
          lastParent = (parentStack.size() > 0) ? parentStack.peek( ) : null;
          currParentName = (lastParent != null) ? lastParent.getName( ) : null;
          LOG.debug( currParentName );
        }
      }

      LOG.debug( "lastParent " + lastParent );
      if (lastParent != null) LOG.debug( lastParent.getName( ) );
      LOG.debug( "currObject " + currObject );
        
      if (currObject != null && lastParent != null)
      {
        LOG.debug( lastParent.getName() + " adding child " + currObject.getValue() );
        lastParent.addProperty( currObject );
      }
    		
      currObject = lastParent;
    }
    	
    private DataObject createDataObject(  )
    {
      DataObject dobj = null;
    		
      if (dataObjectClass != null)
      {
        try
        {
          dobj = (DataObject)Class.forName( dataObjectClass ).newInstance( );
        }
        catch (Exception e )
        {
            LOG.error( "Got EXCEPTION " + e );
        }
      }
    		
      if (dobj == null) dobj = new DataObject( );
        
      return dobj;
    }
  }
}
