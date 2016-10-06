package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;

import com.modinfodesigns.property.transform.string.IDataListRenderer;
import com.modinfodesigns.property.transform.string.IPropertyHolderRenderer;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.utils.FileMethods;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Data Output Processor. Creates an String rendering of a each DataObject within a DataList or
 * of the DataList as a whole using an IDataListRenderer to control the format. By default, the Data Objects will
 * be rendered CDATA encoded native XML Format.  Other native formats (such as JSON) can be used via configuration.
 * 
 * @author Ted Sullivan
 */

public class FileDataProcessor implements IDataObjectProcessor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FileDataProcessor.class );

  private static final String DEFAULT_FILE_PREFIX = "XMLData_";
	
  private String outputDirectory = ".";
    
  // Property that contains the file name or will be given the file name
  private String fileNameProperty;
    
  private String fileNameOutputProperty;

  private String fileNamePrefix = DEFAULT_FILE_PREFIX;
    
  private String format = IProperty.XML_FORMAT_CDATA;
    
  private static int fileNum;
    
  private IDataListRenderer dataListRenderer;
    
  private IPropertyHolderRenderer dataRenderer;
    
  private Integer pageSize;
    
  private String name;
    
  public void setName( String name )
  {
    this.name = name;
  }


  public void setOutputDirectory( String outputDirectory )
  {
    this.outputDirectory = outputDirectory;
  }
    
  public void setFileNameProperty( String fileNameProperty )
  {
    this.fileNameProperty = fileNameProperty;
  }
    
  public void setFileNameOutputProperty( String fileNameOutputProperty )
  {
    this.fileNameOutputProperty = fileNameOutputProperty;
  }
    
  public void setFileNamePrefix( String fileNamePrefix )
  {
    this.fileNamePrefix = fileNamePrefix;
  }
    
  public void addDataListRenderer( IDataListRenderer dataListRenderer )
  {
    this.dataListRenderer = dataListRenderer;
  }
   
  public void addDataRenderer( IPropertyHolderRenderer dataRenderer )
  {
    this.dataRenderer = dataRenderer;
  }
    
  public void setOutputFormat( String format )
  {
    this.format = format;
  }
    
  public void setPageSize( String pageSize ) {
    this.pageSize = new Integer( pageSize );
  }
    
  /**
   * Convert the DataList to a string and store in file provided.
   *
   */
  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList( ) ..." );
		
    if (dataListRenderer != null)
    {
      if (pageSize == null)
      {
        String xml = dataListRenderer.renderDataList( data );
        String fileName = getFileName( (DataObject)data );
        FileMethods.writeFile( fileName, xml );
      }
      else
      {
        DataList tempList = new DataList( );
        for ( Iterator<DataObject> dit = data.getData(); dit.hasNext(); )
        {
          tempList.addDataObject( dit.next() );
          if (tempList.size() == pageSize.intValue())
          {
            String xml = dataListRenderer.renderDataList( tempList );
            String fileName = getFileName( (DataObject)tempList );
            FileMethods.writeFile( fileName, xml );
            tempList.clearDataList();
          }
        }
        if (tempList.size() > 0)
        {
          String xml = dataListRenderer.renderDataList( tempList );
          String fileName = getFileName( (DataObject)tempList );
          FileMethods.writeFile( fileName, xml );
        }
      }
    }
    else if (dataRenderer != null)
    {
      for ( Iterator<DataObject> dit = data.getData(); dit.hasNext(); )
      {
        DataObject dobj = dit.next();
        String xml = dataRenderer.renderPropertyHolder( dobj );
        String fileName = getFileName( dobj );
        FileMethods.writeFile( fileName, xml );
      }
    }
    else
    {
      for ( Iterator<DataObject> dit = data.getData(); dit.hasNext(); )
      {
        DataObject dobj = dit.next();
			
        String fileName = getFileName( dobj );
        LOG.debug( "Saving file: " + fileName );
        String xml = dobj.getValue( format );
        System.out.println( fileName + ":    " + xml );
        FileMethods.writeFile( fileName, xml );
      }
    }
		
    return data;
  }

  private String getFileName( DataObject dobj )
  {
    String fileName = null;
    if (fileNameProperty != null && dobj.getProperty( fileNameProperty) != null)
    {
      fileName = dobj.getProperty( fileNameProperty ).getValue();
    }
    else if (fileNamePrefix != null)
    {
      fileName = fileNamePrefix + Integer.toString( fileNum++ );
    }
    else
    {
      fileName = DEFAULT_FILE_PREFIX  + Integer.toString( fileNum++ );
    }
      
    String outputPath = FileMethods.resolveRelativePath( outputDirectory );
    fileName = new String( outputPath + "/" + fileName + ".xml" );
	    
    if (fileNameOutputProperty != null && dobj.getProperty( fileNameOutputProperty ) == null)
    {
      dobj.setProperty( new StringProperty( fileNameOutputProperty, fileName ));
    }
	    
    return fileName;
  }
	
  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {

  }

}
