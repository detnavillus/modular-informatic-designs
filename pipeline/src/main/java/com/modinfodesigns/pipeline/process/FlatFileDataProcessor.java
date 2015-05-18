package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.utils.FileMethods;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Flat File output of pipeline Data.
 * 
 * @author Ted Sullivan
 */

public class FlatFileDataProcessor implements IDataObjectProcessor 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FlatFileDataProcessor.class );

  private String fileName;
    
  private String fileNameProperty;
    
  private String delimiter = ",";
    
  private ArrayList<Column> outputColumns = new ArrayList<Column>( );
    
  public void setFileName( String fileName )
  {
    this.fileName = fileName;
  }
    
  public void setFileNameProperty( String fileNameProperty )
  {
    this.fileNameProperty = fileNameProperty;
  }
    
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
    
  public void addColumn( Column column )
  {
    outputColumns.add( column );
  }
    
  public void setColumn( Column column )
  {
    outputColumns.add( column );
  }
    
  public void setColumns( DataList columnData )
  {
    LOG.debug( "setColumns( )" );
    for (Iterator<DataObject> dit = columnData.getData(); dit.hasNext(); )
    {
      DataObject columnObj = dit.next();
      addColumn( new Column( columnObj ) );
    }
  }
    
    
  @Override
  public IDataList processDataList( IDataList data )
  {
    if (data == null) return data;
		
    LOG.debug( "processDataList( ) " + data.size( ) );
		
    // For each data Item write a flat file line ...
    for (Iterator<DataObject> dit = data.getData(); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      StringBuilder strbuilder = new StringBuilder( );
      for (int i = 0; i < outputColumns.size(); i++)
      {
        Column col = outputColumns.get( i );
        String colValue = "";
        IProperty prop = dobj.getProperty( col.propertyName );
        if (prop != null)
        {
          colValue = (col.format != null) ? prop.getValue( col.format ) : prop.getValue();
        }
				
        strbuilder.append( colValue );
        if (i < outputColumns.size() - 1 ) strbuilder.append( delimiter );
      }
			
      LOG.debug( "writing: " + strbuilder.toString() );
      String theFilename = getFileName( data, dobj );
      if (theFilename != null)
      {
        FileMethods.addToFile( theFilename, strbuilder.toString() );
      }
    }
		
    return data;
  }

  protected String getFileName( IDataList data, DataObject dobj )
  {
    String theFileName = this.fileName;
		
    if (fileNameProperty != null && data.getProperty( fileNameProperty ) != null)
    {
      theFileName = data.getProperty( fileNameProperty ).getValue( );
    }
		
    if (theFileName == null)
    {
      LOG.error( "fileName is NULL: Cannot write data!" );
			
      return null;
    }
		
    LOG.debug( "FileName = " + theFileName );
		
    return theFileName;
  }
	
  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {

  }

  public class Column
  {
    String propertyName;
    String format;
    	
    public Column( ) { }
    	
    public Column( DataObject dobj )
    {
      IProperty prop = dobj.getProperty( "name" );
      if (prop != null) this.propertyName = prop.getValue( );
      LOG.debug( "set propertyName = " + propertyName );
    		
      prop = dobj.getProperty( "format" );
      if (prop != null) this.format = prop.getValue( );
      LOG.debug( "set format = " + format );
    }
  }

}
