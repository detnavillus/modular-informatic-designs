package com.modinfodesigns.pipeline;

import com.modinfodesigns.network.webservices.soap.ISOAPWebServiceHandler;
import com.modinfodesigns.network.webservices.soap.WebServiceHandlerException;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.pipeline.source.XMLDataSource;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.utils.DOMMethods;

import com.modinfodesigns.property.transform.string.IDataListRenderer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import java.io.StringReader;
import java.io.Writer;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Service Handler that instantiates and runs a Data Processing Pipeline as a 
 * SOAP Web Service. Uses one or more DataProcessor elements (IDataObjectProcessor)
 * 
 * By default - the WebService Pipeline Handler returns an XML formatted string using the
 * native DataObject (IPropertyHolder) getValue( XML_FORMAT ).
 * 
 * An optional XML Renderer can be used to modify the formatted output.
 * 
 * @author Ted Sullivan
 */

public class WebServicePipelineHandler implements ISOAPWebServiceHandler
{
  private transient static final Logger LOG = LoggerFactory.getLogger( WebServicePipelineHandler.class );
	
  private ArrayList<IDataObjectProcessor> dataProcessors;
  private String pipelineServiceTag = "ProcessData";
  private String inputStyle;  // one of ENCODED | NESTED
    
  // XMLDataSource Record Style types
  private String recordStyle; // FieldValueTags | TagNameIsFieldName | FieldTypeAttributes
    
  private String recordTag;
    
  private boolean returnDataList = true;
    
  private IDataListRenderer dataListRenderer;
  private String outputFormat = IProperty.XML_FORMAT;
    
  public void addDataProcessor( IDataObjectProcessor dataProcessor )
  {
    if (dataProcessors == null) dataProcessors = new ArrayList<IDataObjectProcessor>( );
    dataProcessors.add( dataProcessor );
  }
    
  public void setPipelineServiceTag( String pipelineServiceTag )
  {
    this.pipelineServiceTag = pipelineServiceTag;
  }
    
  public void setInputStyle( String inputStyle )
  {
    this.inputStyle = inputStyle;
  }
    
  public void setRecordStyle( String recordStyle )
  {
    this.recordStyle = recordStyle;
  }

  public void setRecordTag( String recordTag )
  {
    this.recordTag = recordTag;
  }
    
  /**
   * Sets the IDataListRenderer that will create the SOAP response body.
   *
   * @param dataListRenderer The IDataListRenderer that will create the response
   *                           body.
   */
  public void setDataListRenderer( IDataListRenderer dataListRenderer )
  {
    this.dataListRenderer = dataListRenderer;
  }
    
  /**
   * Sets the output format used by the DataObject (IPropertyHolder) getValue( format )
   * method. Defaults to IProperty.XML_FORMAT
   *
   * Choices are DELIMITED | JSON
   *
   * If a custom output is required, consider using an implementation of IPropertyHolderRenderer
   *
   * @param outputFormat
   */
  public void setOutputFormat( String outputFormat )
  {
    this.outputFormat = outputFormat;
  }
    
    
  @Override
  public boolean handleWebServiceCall( Element webServiceMessage,
                                       Writer response)
                                       throws WebServiceHandlerException,IOException
  {
    LOG.debug( "handleWebServiceCall( ) ..." );
		
    LOG.debug( DOMMethods.getXML( webServiceMessage, false ) );
		
    Element dataEl = null;
		
    if (webServiceMessage.getTagName().equals( pipelineServiceTag ))
    {
      dataEl = webServiceMessage;
    }
    else
    {
      NodeList myLst = webServiceMessage.getElementsByTagNameNS( "*", pipelineServiceTag );
      if (myLst != null && myLst.getLength() > 0)
      {
        dataEl = (Element)myLst.item( 0 );
      }
    }
		
    if (dataEl == null)
    {
      response.write( "No Data Element Found: '" + pipelineServiceTag + "'" );
      return false;
    }

    // -----------------------------------------------------------------
    // If Internal data is CDATA Encoded (i.e. a String to us)
    // Need to get the string, turn it into a DOM object and have the
    // XMLDataSource process THAT.
    // -----------------------------------------------------------------
    if (inputStyle != null && inputStyle.equals( "ENCODED" ))
    {
      String xmlString = DOMMethods.getText( dataEl );
      Document innerDoc = DOMMethods.getDocument( new StringReader( xmlString ));
      if (innerDoc == null)
      {
        response.write( "No Inner Data Element found." );
        return false;
      }
        
      dataEl = innerDoc.getDocumentElement( );
    }
		
    XMLDataSource dataSource = initDataSource( );
    IDataList dList = new DataList( );
    dataSource.processElement( dList, dataEl );
    
    boolean allProcessorsSucceeded = true;
    String dataProcessorFailed = null;
    
    if (dataProcessors != null)
    {
      for (int i = 0; i < dataProcessors.size(); i++)
      {
        IDataObjectProcessor dataProcessor = dataProcessors.get( i );
        dList = dataProcessor.processDataList( dList );
        if ( dList == null)
        {
          allProcessorsSucceeded = false;
          dataProcessorFailed = dataProcessor.toString();
          break;
        }
      }
    }
    else
    {
      response.write( "No Data Processors Defined." );
      return false;
    }
		
    if (allProcessorsSucceeded && returnDataList )
    {
      if (dataListRenderer != null)
      {
        response.write( dataListRenderer.renderDataList( dList ) );
      }
      else
      {
        response.write( dList.getValue( outputFormat ));
      }
    }
    else if (!allProcessorsSucceeded)
    {
      response.write( "Data Processor Failed: " + dataProcessorFailed );
    }
	
    return allProcessorsSucceeded;
  }
	
  private XMLDataSource initDataSource( )
  {
    XMLDataSource xmlDataSource = new XMLDataSource( );
    
    // initial XMLDataSource parameters from configuration
    if (recordStyle != null)
    {
      xmlDataSource.setStyle( recordStyle );
    }
        
    if (recordTag != null)
    {
      xmlDataSource.setRecordTag( recordTag );
    }
        
    return xmlDataSource;
  }
}
