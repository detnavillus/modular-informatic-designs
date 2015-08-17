package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;

import com.modinfodesigns.utils.DOMMethods;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Lists of Data Objects.  A DataList is also a DataObject. This enables DataLists
 * to be nested and enables properties to be added at the list level.
 * 
 * @author Ted Sullivan
 */

public class DataList extends DataObject implements IDataList
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataList.class );
	
  public static final String LIST_NAME = "objects";
	
  protected ArrayList<DataObject> dataObjects = new ArrayList<DataObject>( );
    
  private boolean hasPlaceholders = false;  // Used for persistence lazy evaluation
    
  public DataList( ) {  }
    
  public DataList( String name )
  {
    setName( name );
  }
    
  protected String getListName( )
  {
    return LIST_NAME;
  }
    
  protected String getXMLTagName( )
  {
    return "DataList";
  }
    
  @Override
  public String getValue(  )
  {
    return getValue( IProperty.JSON_FORMAT );
  }
    
  @Override
  public String getValue( String format )
  {
    if (format == null || format.equals( IProperty.JSON_FORMAT ) || format.equals( IProperty.JSON_VALUE ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "{" );
			
      if (getName() != null && getName().trim().length() > 0 && jsonNameLabel != null)
      {
        sbr.append( "\"" ).append( jsonNameLabel ).append( "\":\"" ).append( getName( ) ).append( "\"" );
      }
            
      if (getID( ) != null && getID( ).trim().length() > 0)
      {
        if (sbr.length() > 1 ) sbr.append( "," );
        sbr.append( "\"" ).append( idLabel ).append( "\":\"" ).append( getID( ) ).append( "\"" );
      }
			
      for (Iterator<IProperty> propIt = getProperties(); propIt.hasNext( ); )
      {
        IProperty prop = propIt.next();
				
        if ((prop instanceof IntrinsicPropertyDelegate) == false &&
            (prop instanceof IFunctionProperty) == false)
        {
          if (sbr.length() > 1 ) sbr.append( "," );
          sbr.append( "\"" ).append( prop.getName() ).append( "\":" )
             .append( prop.getValue( IProperty.JSON_VALUE ));
        }
      }
			
      Iterator<DataObject> daIt = getData( );
      if (daIt.hasNext() )
      {
        if (sbr.length() > 1 ) sbr.append( "," );
        sbr.append( "\"" + getListName( ) + "\":[" );
        int itAt = 0;
        while( daIt.hasNext() )
        {

          DataObject nextDa = daIt.next( );
          String chLabel = nextDa.jsonNameLabel;
            
          if (itAt++ > 0) sbr.append( "," );
          sbr.append( nextDa.getValue( IProperty.JSON_FORMAT ) );
            
          nextDa.jsonNameLabel = chLabel;
        }
			    
        sbr.append( "]" );
      }
      sbr.append( "}" );
      return sbr.toString( );
    }
    else if (format.equals(IProperty.XML_FORMAT) || format.equals( IProperty.XML_FORMAT_CDATA ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<" ).append( getXMLTagName( ) );
      if (getName( ) != null && getName( ).trim().length() > 0)
      {
        sbr.append( " name=\"" ).append( StringTransform.escapeXML( getName( ) )).append( "\"" );
      }
      if (getID( ) != null && getID( ).trim().length() > 0)
      {
        sbr.append( " ID=\"" ).append( StringTransform.escapeXML( getID( ) )).append( "\"" );
      }
      sbr.append( ">" );
			
      for (Iterator<DataObject> daIt = getData( ); daIt.hasNext(); )
      {
        sbr.append( daIt.next().getValue( format ) );
      }
			
      sbr.append( "</" ).append( getXMLTagName() ).append( ">" );
      return sbr.toString( );
    }

    return super.getValue( format );
  }
	


  @Override
  public void setValue( String value, String format )
  {
    if (format.equals( IProperty.XML_FORMAT ))
    {
      // ==========================================================
      // Use DOM to get childNodes that are Elements
      // for each element, get the XML String and use XMLParserTransform
      // to create a DataObject
      // ==========================================================
      XMLParserTransform xmlParserXform = new XMLParserTransform( );
			
      Document doc = DOMMethods.getDocument( new StringReader( value ) );
      if (doc != null)
      {
        Element docElem = doc.getDocumentElement( );
        NodeList childNodes = docElem.getChildNodes( );
        if (childNodes != null && childNodes.getLength() > 0)
        {
          for (int i = 0, isz = childNodes.getLength(); i < isz; i++)
          {
            Node chNode = childNodes.item( i );
            if (chNode instanceof Element )
            {
              Element childEl = (Element)chNode;
              String childXML = DOMMethods.getXML( childEl,  true );
              DataObject childObj = xmlParserXform.createDataObject( childXML );
              if (childObj != null)
              {
                addDataObject( childObj );
              }
            }
          }
        }
      }
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      // ================================================
      // Use JSONParserTransform to build DataList
      // internals from JSON string.
      // ================================================
      JSONParserTransform jsonParserXform = new JSONParserTransform( );
      ArrayList<String> jsonStrings = jsonParserXform.getJsonArray( value );
      if (jsonStrings != null)
      {
        for (String jsonString : jsonStrings )
        {
          DataObject dobj = jsonParserXform.createDataObject( jsonString );
          if (dobj != null)
          {
            addDataObject( dobj );
          }
        }
      }
    }
  }

  @Override
  public int size()
  {
    if (hasPlaceholders)
    {
      restoreList( );
    }
		
    return dataObjects.size( );
  }

  @Override
  public Iterator<DataObject> getData()
  {
    if (hasPlaceholders)
    {
      restoreList( );
    }
    return dataObjects.iterator( );
  }


  protected List<DataObject> getDataList()
  {
    if (hasPlaceholders)
    {
      restoreList( );
    }
    return dataObjects;
  }

  @Override
  public DataObject item(int index)
  {
    return dataObjects.get( index );
  }
	
  public void addDataObject( DataObject dObj )
  {
    dataObjects.add( dObj );
  }
	
  public void removeDataObject( int index )
  {
    if (index >= 0 && index < dataObjects.size() )
    {
      dataObjects.remove( index );
    }
  }
	
  public void removeDataObjectByName( String name )
  {
    int index = -1, i = 0;
    for (DataObject chOb : dataObjects )
    {
      if (chOb.getName() != null && chOb.getName().equals( name ))
      {
        index = i;
        break;
      }
      ++i;
    }
		
    if (index >= 0) removeDataObject( index );
  }
	
  public void replaceDataObject( int index, DataObject dObj )
  {
    if (index >= 0 && index < dataObjects.size() )
    {
      dataObjects.set( index, dObj );
    }
  }

	
  @Override
  public IProperty copy( )
  {
    DataList dl = (DataList)super.copy( );
		
    LOG.debug( "copy: Adding DataObjects ..." );
		
    List<DataObject> myObjs = getDataList( );
    if ( myObjs != null )
    {
      for ( DataObject myObj : myObjs )
      {
        DataObject dataCop = (DataObject)myObj.copy( );
        dl.addDataObject( dataCop );
      }
    }
		
    return dl;
  }
	
  /**
   * Checks if the propName is of the form <objname>[0..n].<propname> or [0..n].<propname>
   * if so, gets the DataObject at the specified index and calls its getProperty( ) method
   * with the remaining path
   *
   * <objname> is the name of this DataList. <propname> is the name of a property
   */
  @Override
  public IProperty getProperty( String propName )
  {
    if (propName == null) return null;
		
    String pName = propName.trim();
    if ( dataObjects != null && ((pName.indexOf( "[" ) == 0 && pName.indexOf( "]" ) > 0)
                             ||  (name != null && (pName.indexOf( this.name + "[" ) == 0) && pName.indexOf( "]" ) > 0)))
    {
      int startNdx = (pName.indexOf( this.name + "[" ) == 0) ? (name.length() + 1) : 1;
      String ndxString = new String( pName.substring( startNdx, pName.indexOf( "]" )));
      try
      {
        int ndx = Integer.parseInt( ndxString );
        if (ndx >= 0 && ndx < dataObjects.size() )
        {
          DataObject childObj = dataObjects.get( ndx );
          String childProp = new String( pName.substring( pName.indexOf( "]" ) + 1 ));
            
          if (childProp == null || childProp.trim().length() == 0)
          {
            return childObj;
          }
            
          // trim off leading '.' or '/'
          if (childProp != null && (childProp.startsWith( "." ) || childProp.startsWith( "/" )))
          {
            childProp = new String( childProp.substring( 1 ) );
          }
				    
          return childObj.getProperty( childProp );
        }
        else
        {
          LOG.error( "Index " + ndx + " is out of bounds!" );
        }
      }
      catch( NumberFormatException nfe )
      {
        LOG.error( "Index NumberFormatException: '" + pName + "'" );
      }
    }
		
    LOG.debug( "Got to super " + pName );
		
    return super.getProperty( pName );
  }
	
  /**
   * Sets a Property on each of the DataList child objects ...
   * @param childProperty
   */
	
  @Override
  public void setChildProperty( IProperty childProperty )
  {
    Iterator<DataObject> objIt = getData();
    while( objIt != null && objIt.hasNext() )
    {
      DataObject chObj = objIt.next();
      chObj.setProperty( childProperty );
    }
  }
	
  private synchronized void restoreList( )
  {
		
  }

  @Override
  public void clearDataList()
  {
    if (dataObjects != null) dataObjects.clear( );
  }
	
  @Override
  public void removeDataObject( String objectID )
  {
    super.removeDataObject( objectID );
		
    int index = -1, i = 0;
    for (DataObject chOb : dataObjects )
    {
      if (chOb.getID() != null && chOb.getID().equals( objectID ))
      {
        index = i;
        break;
      }
      ++i;
    }
		
    if (index >= 0) removeDataObject( index );
  }

  @Override
  public Object get( Object key )
  {
    LOG.debug( "get: '" + key.toString( ) + "'" );
		
    if ( key.toString().equals( getListName( ) ))
    {
      LOG.debug( "returning dataObjects!" );
      return dataObjects;
    }
		
    return super.get( key );
  }
}
