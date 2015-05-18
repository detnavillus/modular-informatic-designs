package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.xml.XMLConstants;

import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.FileMethods;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML-based DataObject Sources
 * 
 * Extended by classes that get XML from file system, URL, FTP, etc.
 * STYLE=FieldValueTags
 * <Result>
 *   <Field><Name>fieldname</Name><Value>field value</Value><Type>field type</Type></Field>
 * </Result>
 * 
 * STYLE=TagnameIsFieldName
 * <Result>
 *   <FieldName type="field type" >field value</FieldName>
 * </Result>
 * 
 * STYLE=FieldTypeAttributes
 * <Result>
 *   <Field name="field name" type="field type" >field value</Field>
 * </Result>
 * 
 * @author Ted Sullivan
 */

public class XMLDataSource extends BaseDataObjectSource
{
  private transient static final Logger LOG = LoggerFactory.getLogger( XMLDataSource.class );

  private String xmlFilePath;
    
  private String recordTag = "Result";
  private String fieldTag = "Field";
  private String fieldNameTag = "Name";
  private String valueTag = "Value";
  private String typeTag = "Type";
    
  private String namespace = null;
    
  private String IDField = null;
    
  private String fieldTypeAttribute = "type";
  private String fieldNameAttribute = "name";
    
  private int dataListSize = 10;
    
  private String style = XMLConstants.FIELD_VALUE_TAG_STYLE;
    
    
  public void setXMLFilePath( String xmlFilePath )
  {
    this.xmlFilePath = xmlFilePath;
  }
    
  public void setXmlFilePath( String xmlFilePath )
  {
    this.xmlFilePath = xmlFilePath;
  }
    
  public void setRecordTag( String recordTag )
  {
    this.recordTag = recordTag;
  }
    
  public void setFieldTag( String fieldTag )
  {
    this.fieldTag = fieldTag;
  }
    
  public void setFieldNameTag( String fieldNameTag )
  {
    this.fieldNameTag = fieldNameTag;
  }
    
  public void setValueTag( String valueTag )
  {
    this.valueTag = valueTag;
  }
    
  public void setTypeTag( String typeTag )
  {
    this.typeTag = typeTag;
  }
    
  public void setDataListSize( int dataListSize )
  {
    this.dataListSize = dataListSize;
  }
    
  public void setStyle( String style )
  {
    this.style = style;
  }
    
  public void setNamespace( String namespace )
  {
    this.namespace = namespace;
  }
    
  public void setIDField( String IDField )
  {
    this.IDField = IDField;
  }
    
  @Override
  public void run()
  {
    LOG.debug( "run()... " + xmlFilePath );
    DataList dList = new DataList( );
        
    String[] filenames = FileMethods.getFileList( xmlFilePath, true );
    processFiles( filenames, dList );
    
    sendProcessComplete( (IPropertyHolder)null, true );
  }
    
  private void processFiles( String[] filenames, DataList dList )
  {
    if (filenames != null)
    {
      for (int i = 0; i < filenames.length; i++)
      {
        LOG.debug( "Processing file: " + filenames[i] );
        processFile( filenames[i], dList );
      }
    }
        
    if (dList.size() > 0)
    {
      doProcessDataList( dList );
    }
  }
    
    
  private void processFile( String filename, DataList dList )
  {
    if (filename.toLowerCase().endsWith( "xml" ))
    {
      Document doc = DOMMethods.getDocument( filename );
      if (doc != null)
      {
        Element docElem = doc.getDocumentElement( );
        processElement( dList, docElem );
        if (dList.size() >= dataListSize )
        {
          doProcessDataList( dList );
          dList.clearDataList( );
        }
      }
    }
    else
    {
      File file = new File( filename );
      if (file.isDirectory() )
      {
        String subfilepath = file.getAbsolutePath( );
        String[] files = FileMethods.getFileList( subfilepath, true );
        processFiles( files, dList );
      }
    }
  }
    
  public void processElement( IDataList dList, Element dataEl )
  {
    LOG.debug( "processElement ..." + dataEl.getTagName( ) );
        
    if (dataEl.getTagName().equals( recordTag ))
    {
      DataObject dObj = createDataObject( dataEl );
      if (IDField != null)
      {
        IProperty idProp = dObj.getProperty( IDField );
        if (idProp != null)
        {
          LOG.debug( "Setting object ID = " + idProp.getValue( ) );
          dObj.setID( idProp.getValue(  ) );
        }
      }
      dList.addDataObject( dObj );
    }
    else
    {
            
      NodeList recordLst = dataEl.getElementsByTagName( recordTag );
      if (recordLst == null || recordLst.getLength() == 0)
      {
        recordLst = (namespace != null) ? dataEl.getElementsByTagNameNS( namespace, recordTag )
                                        : dataEl.getElementsByTagName( recordTag );
      }
        	
      if (recordLst != null && recordLst.getLength() > 0)
      {
        for (int r = 0; r < recordLst.getLength(); r++ )
        {
          Element recEl = (Element)recordLst.item( r );
          processElement( dList, recEl );
          if (dList.size() >= dataListSize )
          {
            doProcessDataList( dList );
            dList.clearDataList( );
          }
        }
      }
      else
      {
        LOG.debug( "No tags of type '" + recordTag + "'" );
      }
    }
        
  }
    
    
  private DataObject createDataObject( Element dataEl )
  {
    if (style != null && style.equals( XMLConstants.FIELD_VALUE_TAG_STYLE ))
    {
      return createFieldValueObject( dataEl );
    }
    else if (style != null && style.equals( XMLConstants.TAGNAME_IS_FIELD_STYLE ))
    {
      return createTagnameIsField( dataEl );
    }
    else if (style != null && style.equals( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE ))
    {
      return createFieldTypeAttributes( dataEl );
    }
       
    return null;
  }
    
  private DataObject createFieldValueObject( Element dataEl )
  {
    LOG.debug( "createFieldValueObject( )..." );
        
    DataObject dObj = new DataObject( );
        
    NodeList fieldLst = (namespace != null) ? dataEl.getElementsByTagNameNS( namespace, fieldTag )
                                            : dataEl.getElementsByTagName( fieldTag );
    if (fieldLst != null && fieldLst.getLength() > 0)
    {
      for (int i = 0; i < fieldLst.getLength(); i++)
      {
        Element fieldEl = (Element)fieldLst.item( i );
        String fieldName = null;
        String fieldValue = null;
        String fieldType = null;
                
        NodeList chNodes = fieldEl.getChildNodes( );
        if (chNodes != null && chNodes.getLength() > 0)
        {
          for (int k = 0; k < chNodes.getLength(); k++)
          {
            Node n = chNodes.item( k );
            if (n instanceof Element && ((Element)n).getTagName().equals( fieldNameTag ))
            {
              Element el = (Element)n;
              fieldName = DOMMethods.getText( el );
            }
            else if (n instanceof Element && ((Element)n).getTagName().equals( valueTag ))
            {
              Element el = (Element)n;
              fieldValue = DOMMethods.getText( el );
            }
            else if (n instanceof Element && ((Element)n).getTagName().equals( typeTag ))
            {
              Element el = (Element)n;
              fieldType = DOMMethods.getText( el );
            }
          }
        }
                
        if (fieldName != null && fieldValue != null)
        {
          if (fieldType == null || fieldType.equals( "String" ))
          {
            LOG.debug( "Adding property: " + fieldName + " = " + ((fieldValue.length() > 20) ? fieldValue.substring(0,20) : fieldValue) );
            dObj.addProperty( new StringProperty( fieldName, fieldValue ) );
          }
        }
        else
        {
          LOG.debug( "Could not get fieldName and fieldValue!" );
        }
      }
    }
        
    return dObj;
  }
    
  private DataObject createTagnameIsField( Element dataEl )
  {
    LOG.debug( "createTagnameIsField( ) ..." );
        
    DataObject dObj = new DataObject( );
    NodeList childNodes = dataEl.getChildNodes( );
    if (childNodes != null && childNodes.getLength() > 0)
    {
      for (int i = 0; i < childNodes.getLength(); i++)
      {
        Node cn = childNodes.item( i );
        if (cn instanceof Element)
        {
          Element tagEl = (Element)cn;
          String tagName = tagEl.getTagName( );
          String fieldType = tagEl.getAttribute( fieldTypeAttribute );
          if (fieldType != null && fieldType.trim().length() == 0)
          {
            fieldType = null;
          }
                    
          String fieldValue = DOMMethods.getText( tagEl );
                    
          LOG.debug( "Got tagName = " + tagName );
          LOG.debug( "Got fieldValue = " + fieldValue );
                    
          if (fieldValue != null && fieldValue.trim().length() > 0
          && (fieldType == null || fieldType.equals( "String" )))
          {
            LOG.debug( "Adding property " + tagName );
            dObj.addProperty( new StringProperty( tagName, fieldValue ) );
          }
                    
          // Find any attributes in tag
          NamedNodeMap attrMap = tagEl.getAttributes( );
          if (attrMap != null)
          {
            for (int a = 0; a < attrMap.getLength(); a++)
            {
              Node attrNode = attrMap.item( a );
              String attrName = attrNode.getLocalName( );
              String attrValue = attrNode.getNodeValue( );
              dObj.addProperty( new StringProperty( tagName + "_" + attrName, attrValue ) );
            }
          }
        }
      }
    }
        
    return dObj;
  }
    
  private DataObject createFieldTypeAttributes( Element dataEl )
  {
    LOG.debug( "createFieldTypeAttributes( )..." );
        
    DataObject dObj = new DataObject( );
        
    NodeList fieldLst = (namespace != null) ? dataEl.getElementsByTagNameNS( namespace, fieldTag )
                                            : dataEl.getElementsByTagName( fieldTag );
    if (fieldLst != null && fieldLst.getLength() > 0)
    {
      for (int i = 0; i < fieldLst.getLength(); i++)
      {
        Element fieldEl = (Element)fieldLst.item( i );
        String fieldName = fieldEl.getAttribute( fieldNameAttribute );
        String fieldType = fieldEl.getAttribute( fieldTypeAttribute );
        String fieldValue = DOMMethods.getText( fieldEl );

        if (fieldType == null || fieldType.equals( "String" ))
        {
          dObj.addProperty( new StringProperty( fieldName, fieldValue ) );
        }
      }
    }
    return dObj;
  }

  @Override
  public void run(IUserCredentials withUser)
  {
    run( );
  }

}
