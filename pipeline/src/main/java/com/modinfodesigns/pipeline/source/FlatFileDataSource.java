package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.security.IUserCredentials;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.utils.FileMethods;
import com.modinfodesigns.utils.StringMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatFileDataSource extends BaseDataObjectSource
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FlatFileDataSource.class );

  private static final boolean DEBUG_DATA = false;
    
  private String fileName;
    
  private String fileDirectory;
    
  private String fileNameProperty;
    
  private String delimiter = "|";
  private int dataListSize = 10;
    
  private String skipPattern = "_SKIP_";
    
  private ArrayList<String> properties = new ArrayList<String>( );
    
  // Map of field name to PropertyDescriptor
  private HashMap<String,PropertyDescriptor> propertyDescriptorMap = new HashMap<String,PropertyDescriptor>( );
    
  public void setFileName( String fileName )
  {
    this.fileName = fileName;
  }
    
  public void setFileDirectory( String fileDirectory )
  {
    this.fileDirectory = fileDirectory;
  }
    
  public void setFileNameProperty( String fileNameProperty )
  {
    this.fileNameProperty = fileNameProperty;
  }
    
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
    
  public void addPropertyDescriptor( PropertyDescriptor pd )
  {
    properties.add( pd.getName( ) );
    propertyDescriptorMap.put( pd.getName( ), pd );
  }

  public void addColumns( DataList columnList )
  {
    setColumns( columnList );
  }
    
  public void setColumns( DataList columnList )
  {
    setProperties( columnList );
  }
    
  public void setProperties( DataList propertyList )
  {
    LOG.debug( "setProperties" );
        
    for (Iterator<DataObject> dit = propertyList.getData(); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      addPropertyDescriptor( new PropertyDescriptor( dobj ) );
    }
  }
    
  @Override
  public void run()
  {
    run( (IUserCredentials)null );
  }

  @Override
  public void run( IUserCredentials withUser )
  {
    LOG.debug( "run ..." );
        
    // use FileUtils to get lines in file name
    if (this.fileName != null)
    {
      process( this.fileName );
    }
        
    if (fileDirectory != null)
    {
      String[] filenames = FileMethods.getFileList( fileDirectory,  true );
      for (int i = 0; i < filenames.length; i++)
      {
        process( StringTransform.replaceSubstring( filenames[i], "\\", "/" ) );
      }
    }
  }
    
    
  private void process( String filename )
  {
    LOG.debug( "Processing" + filename );
        
    String[] lines = getFlatFileLines( filename );
    DataList dList = new DataList( );
    if (fileNameProperty != null)
    {
      dList.addProperty( new StringProperty( fileNameProperty, filename ));
    }
        
    if ( lines == null || lines.length == 0)
    {
      sendProcessComplete( (IPropertyHolder)null, false );
      return;
    }
        
    for (int i = 0; i < lines.length; i++)
    {
      try
      {
        DataObject dobj = createDataObject( lines[i] );
        dList.addDataObject( dobj );
        if (dList.size() == dataListSize)
        {
          doProcessDataList( dList );
          dList = new DataList( );
          if (fileNameProperty != null)
          {
            dList.addProperty( new StringProperty( fileNameProperty, filename ));
          }
        }
      }
      catch (PropertyValidationException pve )
      {
        LOG.error( "Cannot create DataObject from: " + lines[i] + " got PropertyValidationException " + pve.getMessage( ) );
      }
    }
        
    if (dList.size() > 0)
    {
      doProcessDataList( dList );
    }
        
    sendProcessComplete( (IPropertyHolder)null, true );
  }
    
  private String[] getFlatFileLines( String filename )
  {
    return FileMethods.readFileLines( filename );
  }
    
  private DataObject createDataObject( String line ) throws PropertyValidationException
  {
    String theDelimiter = delimiter;
    if ( delimiter.equalsIgnoreCase( "tab" ))
    {
      theDelimiter = "\t";
    }
        
    ArrayList<String> values = getStringArray( line, theDelimiter  );
    int limit = Math.min( values.size( ), properties.size( ) );
    DataObject dobj = new DataObject( );
    for (int i = 0; i < limit; i++)
    {
      String propName = properties.get( i );
      if (DEBUG_DATA) LOG.debug( propName + " value: " + values.get( i ) );
            
      if ( propName.startsWith( skipPattern ) == false )
      {
        IProperty prop = null;
                
        PropertyDescriptor pd = propertyDescriptorMap.get( propName );
        if (pd != null)
        {
          String propertyClass = pd.getPropertyType( );
          try
          {
            prop = (IProperty)Class.forName( propertyClass ).newInstance( );
            prop.setValue( values.get( i ), pd.getPropertyFormat( ) );
            prop.setName( pd.getName( ) );
          }
          catch ( Exception e )
          {
            LOG.error( "Could not instantiate IProperty '" + propertyClass + "'" );
          }
        }
        else
        {
          prop = new StringProperty( );
          prop.setValue( values.get( i ), null );
          prop.setName( propName );
        }
                
        if (prop != null)
        {
          dobj.addProperty( prop );
        }
      }
    }
        
    LOG.debug( "created DataObject: " + dobj.getValue( ) );
        
    return dobj;
  }
    
  private ArrayList<String> getStringArray( String line, String delimiter )
  {
    ArrayList<String> strings = new ArrayList<String>( );
    int start = 0;
    int i = 0;
    while ( i < line.length( ) )
    {
      char ic = line.charAt( i );
      if (delimiter.indexOf( ic ) >= 0)
      {
        String str = new String( line.substring( start, i ));
        LOG.debug( str );
        strings.add( str );
        i += delimiter.length();
        start = i;
      }
      else if (ic == '\"' )
      {
        // find the next " char
        int endQuote = StringMethods.findEnd( line, '\"', '\"', i );
        String quotedStr = new String( line.substring( start+1, endQuote ));
        LOG.debug( "Quoted str " + quotedStr );
        strings.add( quotedStr );
        i = endQuote + 1 + delimiter.length();
        start = i;
      }
      else
      {
        ++i;
      }
    }
        
    if (i > start)
    {
      String lastStr =  new String( line.substring( start, i ));
      LOG.debug( "last str " + lastStr );
      strings.add( lastStr );
    }
        
    return strings;
  }
}
