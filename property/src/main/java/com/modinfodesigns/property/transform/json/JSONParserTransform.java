package com.modinfodesigns.property.transform.json;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataObjectBuilder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.BooleanProperty;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.BasePropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.LongProperty;
import com.modinfodesigns.property.quantity.ScalarQuantity;

import com.modinfodesigns.utils.StringMethods;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms a StringProperty object with a JSON String into an IPropertyHolder (DataObject) object.
 * 
 * @author Ted Sullivan
 */

// TO DO - make this an IPropertyHolderTransform - transform a String Field into a DataObject field

public class JSONParserTransform extends BasePropertyTransform implements IDataObjectBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( JSONParserTransform.class );

  // Optional DataObject subclass - (e.g. a DataObjectBean class )
  private String dataObjectClass;
    
  private String nameLabel = DataObject.OBJECT_NAME_PARAM;
  private String idLabel   = DataObject.OBJECT_ID_PARAM;
  private String schemaLabel = DataObject.SCHEMA_PARAM;
    
  private boolean detectNumbers = false;
    
    
  public void setDataObjectClass( String dataObjectClass )
  {
    this.dataObjectClass = dataObjectClass;
  }
    
  public void setJsonNameLabel( String nameLabel )
  {
    this.nameLabel = nameLabel;
  }
    
  public void setIDLabel( String idLabel )
  {
    this.idLabel = idLabel;
  }
    
  public void setSchemaLabel( String schemaLabel )
  {
    this.schemaLabel = schemaLabel;
  }
    
  public void setDetectNumbers( String detectNumbers )
  {
    this.detectNumbers = detectNumbers.equalsIgnoreCase( "true" );
  }
    
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    String jsonString = input.getValue( );
    LOG.debug( "jsonString == " + jsonString );
    
    if (jsonString == null) return input;
    MarkedCharArray mca = new MarkedCharArray( );
    mca.charArray = jsonString.toCharArray( );

    IProperty jsonObject = transformString( mca.trim( ) );
    if (jsonObject == null)
    {
      throw new PropertyTransformException( "Could not parse jsonString " );
    }
        
    return jsonObject;
  }

  private IProperty transformString( MarkedCharArray mca )
  {
    LOG.debug( "transformString '" + mca.getString(  ) + "'" );
    if (mca == null || mca.atEnd( ) ) return null;
      
    if ( mca.trim().startsWith( '{' ))
    {
      return createDataObject( mca );
    }
    else
    {
      return createProperty( mca );
    }
  }
    
  @SuppressWarnings("unchecked")
  public IProperty createProperty( String jsonString )
  {
    MarkedCharArray mca = new MarkedCharArray( );
    mca.charArray = jsonString.toCharArray( );
      
    return createProperty( mca );
  }
    
  @SuppressWarnings("unchecked")
  public IProperty createProperty( MarkedCharArray mca )
  {
    LOG.debug( "createProperty '" + mca.getString( ) + "'" );
    String name = "";
    int nextEnd = mca.getNextPos( ']' );
    int nextColon = mca.getNextPos( ':' );

    if ((nextEnd > 0 && nextColon > 0 && nextColon < nextEnd ) || (nextEnd < 0 && nextColon > 0)){
      name = getName( mca );
    }

    LOG.debug( "Got name = '" + name  + "'" );
    LOG.debug( "currPos = " + mca.currPos + " length = " + mca.charArray.length );
    LOG.debug( "getValue from '" + mca.getString( ) + "'" );
    Object value = getValue( mca );
    if (value == null) return null;
      
    if (value instanceof DataObject)
    {
        DataObject dob = (DataObject)value;
        if (name != null && name.length() > 0) dob.setName( name );
        return dob;
    }
    if (value instanceof String)
    {
      String valStr = (String)value;
      valStr = valStr.trim( );

      if (detectNumbers && StringMethods.isInteger( valStr ))
      {
        LongProperty longProp = null;
        try
        {
          longProp = new LongProperty( name, Long.parseLong( valStr ));
          if (longProp.getLongValue() >= (long)Integer.MIN_VALUE && longProp.getLongValue() <= (long)Integer.MAX_VALUE)
          {
            return new IntegerProperty( name, Integer.parseInt( valStr ));
          }
          return longProp;
        }
        catch ( NumberFormatException nfe )
        {
          return new StringProperty( name, valStr );
        }
      }
      else if (detectNumbers && StringMethods.isNumber( valStr ))
      {
        return new ScalarQuantity( name, valStr );
      }
      else if (valStr.equalsIgnoreCase( "true" ) || valStr.equalsIgnoreCase( "false" ))
      {
        return new BooleanProperty( name, valStr );
      }
      else if (valStr.length() > 0)
      {
        return new StringProperty( name, valStr );
      }
    }
    else if (value instanceof ArrayList )
    {
      PropertyList pl = new PropertyList( );
      pl.setName( name );
      ArrayList<IProperty> values = (ArrayList<IProperty>)value;
      for (int i = 0; i < values.size(); i++)
      {
        IProperty prop = values.get(i);
        if (prop != null)
        {
          prop.setName( name );
          pl.addProperty( prop );
        }
      }
          
      return pl;
    }
      
    return null;
  }
    
  private String getName( MarkedCharArray mca ) {
    int nextColon = mca.getNextPos( ':' );
    String name = "";

    if (nextColon > mca.currPos && nextColon < mca.charArray.length-1) {
      name = mca.getString( mca.currPos, nextColon );
      mca.currPos = nextColon + 1;
      
      if (name != null && name.trim().startsWith( "\"" )) {
        name = name.trim( );
        name = name.substring( 1 );
        if (name.indexOf( "\"" ) > 0) {
          name = name.substring( 0, name.indexOf( "\"" ));
        }
      }
    }

    return name;
  }
    
  private Object getValue( MarkedCharArray mca  )
  {
    if (mca.atEnd( ) ) return null;
    LOG.debug( "getValue '" + mca.getString( ) + "'" );
    Object theObj = null;
        
    mca.trim();
    if (mca.startsWith( '{' ))
    {
      theObj = createDataObject( mca );
    }
    else if (mca.startsWith( '[' ))
    {
      theObj = getJsonArray( mca );
    }
    else if ( mca.startsWith( '\"' ))
    {
      ++mca.currPos;
      int nextQuote = mca.getNextPos( '\"' );
      String value = mca.getString( mca.currPos, nextQuote );
      mca.currPos = nextQuote + 1;
      theObj = value;
    }
    else
    {
      int nextBrace = mca.getNextPos( '}' );
      int nextClose = mca.getNextPos( ']' );
      int nextComma = mca.getNextPos( ',' );
      if (nextClose > 0 && (nextComma < 0 || nextClose < nextComma )) {
        if (nextBrace > 0 && nextBrace < nextClose ) {
          String val = mca.getString( mca.currPos, nextBrace );
          mca.currPos = nextBrace;
          theObj = val;
        }
        else  {
          String val = mca.getString( mca.currPos, nextClose );
          mca.currPos = nextClose;
          theObj = val;
        }
      }
      else {
        if (nextBrace > 0 && (nextComma < 0 || nextBrace < nextComma )) {
          String val = mca.getString( mca.currPos, nextBrace );
          mca.currPos = nextBrace;
          theObj = val;
        }
        else {
          if (nextComma > 0) {
            String val = mca.getString( mca.currPos, nextComma );
            mca.currPos = nextComma + 1;
            theObj = val;
          }
        }
      }
    }
    
    if (mca.trim().startsWith( ',' )) ++mca.currPos;
    return theObj;
  }
    
  public ArrayList<IProperty> getJsonArray( String jsonString )
  {
    MarkedCharArray mca = new MarkedCharArray( );
    mca.charArray = jsonString.toCharArray( );
      
    return getJsonArray( mca );
  }
    
  private ArrayList<IProperty> getJsonArray( MarkedCharArray mca )
  {
    LOG.debug( "getJsonArray: '" + mca.getString( ) + "'" );
      
    if (mca.trim().startsWith( '[' )) mca.incrementPos( );
    ArrayList<IProperty> valueList = new ArrayList<IProperty>( );
    mca.trim( );
      
    while ( !mca.atEnd( ) && !mca.startsWith( ']' ) )
    {
      IProperty prop = transformString( mca );
      LOG.debug( "adding property " + prop.getValue( ) );
      valueList.add( prop );
      mca.trim( );
      if (mca.startsWith( ',' ) ) mca.incrementPos( );
    }

    if (!mca.atEnd( ) && mca.trim().startsWith( ']' ) ) mca.incrementPos( );
    if (!mca.atEnd( ) && mca.startsWith( ',' )) mca.incrementPos( );
      
    return valueList;
  }
    
  @Override
  public DataObject createDataObject( String jsonString )
  {
    if (jsonString == null) return null;

    MarkedCharArray mca = new MarkedCharArray( );
    mca.charArray = jsonString.toCharArray( );
    return createDataObject( mca );
  }
    
  private DataObject createDataObject( MarkedCharArray mca )
  {
    LOG.debug( "createDataObject '" + mca.getString( ) + "'" );
    // ===========================================================
    // get the set of name: value pairs
    // start in name, once hit colon we are in value,
    // if { find matching } if [ find matching ] hit comma go to the next one.
    // ===========================================================
    DataObject dobj = null;
    
    if (dataObjectClass != null)
    {
      try
      {
        dobj = (DataObject)Class.forName( dataObjectClass ).newInstance( );
      }
      catch (Exception e )
      {
        //LOG.error( "Could not create Data Object class '" + dataObjectClass + "'" );
        //LOG.debug( "Could not create Data Object class '" + dataObjectClass + "'" );
      }
    }
        
    if (dobj == null) dobj = new DataObject( );
    
    formatDataObject( dobj, mca );
      
    LOG.debug( "got data Object: " + dobj.getValue( IProperty.JSON_FORMAT ) );
      
    IProperty proxyProp = dobj.getProperty( "proxyObject" );
    // create the Proxy Object ...
    if ( proxyProp != null )
    {
      if (proxyProp instanceof DataObject)
      {
        dobj.setProxyObject( (DataObject)proxyProp );
      }
      else
      {
        DataObject proxyObj = new DataObject( );
        proxyObj.setName( proxyProp.getValue( ) );
        dobj.setProxyObject( proxyObj );
      }
    }
        
    return dobj;
  }
    
  public void formatDataObject( DataObject dobj, String jsonString )
  {
    MarkedCharArray mca = new MarkedCharArray( );
    mca.charArray = jsonString.toCharArray( );
    formatDataObject( dobj, mca );
  }
    
  private void formatDataObject( DataObject dobj, MarkedCharArray mca )
  {
    // System.out.println( "formatDataObject " + mca.getString( ) );
    if (mca.trim().startsWith( '{' )) mca.incrementPos( );

    while (!mca.atEnd( ) && !mca.startsWith( '}' ) )
    {
      IProperty prop = transformString( mca );
      if (prop != null)
      {
        // System.out.println( "daobj adding property " +  prop.getName( ) + " = " + prop.getValue( ) );
        dobj.addProperty( prop );
        mca.trim( );

        if (mca.startsWith( ',' )) mca.incrementPos( );
      }
      else
      {
        ++mca.currPos;
      }

      if (!mca.atEnd( ) && mca.startsWith( ',' )) mca.incrementPos( );
    }

    if (!mca.atEnd( ) && mca.trim().startsWith( '}' ) ) mca.incrementPos( );
    if (!mca.atEnd( ) && mca.startsWith( ',' )) mca.incrementPos( );

  }
    
  class MarkedCharArray {
    char[] charArray;
    int currPos = 0;
      
    boolean atEnd( ) {
      return (currPos >= charArray.length);
    }
      
    MarkedCharArray trim( ) {
      while ( currPos < charArray.length ) {
        if (charArray[currPos] != ' ' && charArray[currPos] != '\n' && charArray[currPos] != '\r' ) return this;
        if (currPos < charArray.length) ++currPos;
      }
      return this;
    }
      
    boolean startsWith( char character ) {
      return ( !atEnd() && charArray[currPos] == character);
    }
      
    void incrementPos( ) {
      ++currPos;
      if (currPos == charArray.length) --currPos;
    }
      
    int getNextPos( char character ) {
      int nextPos = currPos;
      boolean escaped = false;
      while (nextPos < (int)charArray.length ) {
        if (!escaped && charArray[nextPos] == character ) {
          break;
        }
        if      (escaped) escaped = false;
        else if (charArray[nextPos] == '\\' )     escaped = true;
        ++nextPos;
      }
      return (nextPos < charArray.length) ? nextPos : -1;
    }
      
    String getString( int startPos, int endPos ) {
      if (startPos >= 0 && endPos <= charArray.length && endPos > startPos ) {
        return new String( charArray, startPos, endPos-startPos );
      }
      return "";
    }
    
    String getString( ) {
      return (currPos < charArray.length) ? getString( currPos, charArray.length ) : "";
    }
  }
}
