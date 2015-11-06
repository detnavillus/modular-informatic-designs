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

public class OldJSONParserTransform extends BasePropertyTransform implements IDataObjectBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( JSONParserTransform.class );

  // Optional DataObject subclass - (e.g. a DataObjectBean class )
  private String dataObjectClass;
    
  private String nameLabel = DataObject.OBJECT_NAME_PARAM;
  private String idLabel   = DataObject.OBJECT_ID_PARAM;
  private String schemaLabel = DataObject.SCHEMA_PARAM;
    
    
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
    
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    String jsonString = input.getValue( );
    if (jsonString == null) return input;
    IProperty jsonObject = transformString( jsonString );
    if (jsonObject == null)
    {
      throw new PropertyTransformException( "Could not parse jsonString " );
    }
        
    return jsonObject;
  }

  private IProperty transformString( String jsonString )
  {
    // LOG.debug( "transformString: '" + jsonString + "'" );

    // if the JSON String starts with  "{" and ends with "}" - its a data object
    if (jsonString.trim().startsWith( "{" ) && jsonString.trim().endsWith( "}"))
    {
      String innerStr = new String( jsonString.substring( jsonString.indexOf( "{" ) + 1, jsonString.lastIndexOf( "}" )) );
      return createDataObject( innerStr );
    }
    else
    {
      return createProperty( jsonString );
    }
  }
    
  @SuppressWarnings("unchecked")
  public IProperty createProperty( String jsonString )
  {
    //LOG.debug( "createProperty: '" + jsonString + "'" );

    String name = (jsonString.indexOf( ":" ) > 0) ? getName( jsonString ) : "";
    Object value = getValue( jsonString );
    if (value == null) return null;
        
    if (value instanceof String)
    {
      String valStr = (String)value;
      valStr = valStr.trim( );
      if (StringMethods.isInteger( valStr ))
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
      else if (StringMethods.isNumber( valStr ))
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
    else if (value instanceof Integer )
    {
      return new IntegerProperty( name, (Integer)value );
    }
    else if (value instanceof Long )
    {
      return new LongProperty( name, (Long)value );
    }
    // ...
    else if (value instanceof ArrayList )
    {
      PropertyList pl = new PropertyList( );
      pl.setName( name );
      ArrayList<String> values = (ArrayList<String>)value;
      for (int i = 0; i < values.size(); i++)
      {
        IProperty prop = transformString( values.get(i) );
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
    
    
  private String getName( String jsonString )
  {
    if (jsonString == null || jsonString.indexOf( ":" ) < 0)
    {
      return null;
    }
        
    //LOG.debug( "getName: " + jsonString );
    String name = new String( jsonString.substring( 0, jsonString.indexOf( ":" )));
        
    //LOG.debug( "name " + name );
        
    // strip quotes from name ...
    if (name.trim().startsWith( "\""))
    {
      name = StringTransform.replaceSubstring( name, "\"", "" );
    }
        
    //LOG.debug( "returning '" + name.trim() + "'" );
    return (name != null) ? name.trim() : null;
  }
    

  private Object getValue( String pJsonString )
  {
    //LOG.debug( "getValue: '" + pJsonString + "'" );
      
    if (pJsonString == null || pJsonString.indexOf( ":" ) < 0)
    {
      return (StringTransform.isInteger( pJsonString.trim() ) ) ? new Long( pJsonString.trim() ) : pJsonString;
    }
        
    String jsonString = pJsonString.trim();
    if (jsonString.startsWith( "{" ))
    {
      String objStr = findMatching( jsonString, "{", "}" );
      //LOG.debug( "got objStr " + objStr );
      return (objStr != null) ? createDataObject( objStr ) : null;
    }
        
    String value = (jsonString.indexOf( ":" ) > 0 && jsonString.indexOf( ":" ) < jsonString.length() - 1) ? new String( jsonString.substring( jsonString.indexOf( ":" ) + 1 ) ) : jsonString;
    LOG.debug( "value = '" + value.trim() + "'");
        
    if (value.trim().startsWith( "[" ) )
    {
      String trimmed = value.trim();
      String arrayStr = findMatching( trimmed, "[", "]" );
      //LOG.debug( "Matching array: " + arrayStr );
            
      return (arrayStr != null && arrayStr.indexOf( "]" ) >= 1) ? getJsonArray( new String( arrayStr.substring( 1, arrayStr.lastIndexOf( "]" ) ) ) ) : null;
    }
    else if (value.trim().startsWith( "{" ))
    {
      String objString = findMatching( value.trim( ), "{", "}" );
      if (objString != null)
      {
        DataObject dobj = createDataObject( objString );
        String name = getName( new String( jsonString.substring( 1 ) ) );
        dobj.setName( name );
        //LOG.debug( "CreateDataObject: " + name );
        return dobj;
      }
      else
      {
        return null;
      }
    }
    else if (value.trim().startsWith( "\"" ))
    {
      //LOG.debug( "value = '" + value + "'" );
      String matching = findMatching( new String( value.trim().substring( 1 ) ), "\"" );
      //LOG.debug( "matching = '" + matching + "'" );
        
      return "\"" + matching + "\"";
    }
    else if (StringTransform.isInteger( value.trim() ))
    {
      try
      {
        //LOG.debug( "Integer!" );
        return new Long( value.trim( ) );
      }
      catch ( NumberFormatException nfe )
      {
        return value;
      }
    }
        
    if (value != null && value.trim().endsWith( "}" )) value = new String( value.trim().substring( 0, value.lastIndexOf( "}" )));
    //LOG.debug( "returning: '" + value.trim( ) + "'" );
        
    return (value != null) ? value.trim() : null;
  }
    
  public ArrayList<String> getJsonArray( String jsonArray )
  {
    //LOG.debug( "getJsonArray '" + jsonArray + "'" );
        
    // parse the JSON array into objects or arrays or values
    // read each character
    ArrayList<String> valueList = new ArrayList<String>( );
    String currStr = jsonArray.trim( );
    while (currStr.trim().length() > 0 )
    {
      currStr = currStr.trim( );
      //LOG.debug( "start loop: curStr = '" + currStr + "'" );
      String valStr = currStr;
      if (currStr.startsWith( "\"" ))
      {
        valStr = findMatching( new String( currStr.substring( 1 )), "\"" );
        if (valStr != null)
        {
          //LOG.debug( "quoted: '" + valStr + "'" );
          // if valStr is followed by a ":" now, - its a property, else its just
          // a string
          String rest = new String( currStr.substring( valStr.length() + 1 ));
          //LOG.debug( "the rest: '" + rest + "'" );
          if (rest.trim().startsWith( "\"" ))
          {
            String restRest = new String(rest.substring( 1 ));

            if (restRest.trim().startsWith( ":" ))
            {
              if ( findNext( rest, "," ) > 0)
              {
                valStr = "\"" + valStr + new String( rest.substring( 0, findNext( rest, "," )));
              }
              else
              {
                valStr = "\"" + valStr + rest;
              }
            }
            // currStr = currStr.substring( 1 );
            //LOG.debug( "Found pair value: '" + valStr + "'" );
          }
        }
      }
      else if (currStr.startsWith( "{" ))
      {
        valStr = findMatching( valStr, "{", "}" );
      }
      else if (currStr.startsWith( "[" ))
      {
        valStr = findMatching( valStr, "[", "]" );
      }
      else
      {
        valStr = ( findNext( currStr, "," ) > 0) ? new String( currStr.substring(0, findNext( currStr, "," ))) : currStr;
      }
    
      if (valStr != null)
      {
        //LOG.debug( "valStr: '" + valStr + "'" );
        valueList.add( valStr );
        currStr = new String( currStr.substring( currStr.indexOf( valStr ) + valStr.length() ));
        if (currStr.startsWith ( "\"," )) currStr = new String( currStr.substring( 1 ) );
        //LOG.debug( "currStr = '" + currStr + "'" );
        
        // ... once we have the value, check for a comma - strip it and move on ...
        if ( findNext(currStr.trim(), "," ) == 0)
        {
          currStr = new String( currStr.substring( findNext( currStr, "," ) + 1 ) );
        }
        else
        {
          currStr = "";
        }
      }
      else
      {
        currStr = "";
      }
        
      //LOG.debug( "currStr '" + currStr + "'" );
    }
        
    //LOG.debug( "Returning Value List..." );
    return valueList;
  }
    
  @Override
  public DataObject createDataObject( String propString )
  {
    //LOG.debug( "createDataObject: '" + propString + "'" );
      
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
    if (propString == null) return dobj;
    
    formatDataObject( dobj, propString );
      
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
    
  public void formatDataObject( DataObject dobj, String pJsonString )
  {
    String jsonString = pJsonString;
      
    if (pJsonString == null || (pJsonString.trim().startsWith( "{" ) && !pJsonString.trim().endsWith( "}")))
    {
      return; // malformed
    }
      
    if (pJsonString.trim().startsWith( "{" ) && pJsonString.trim().endsWith( "}"))
    {
      String trimmed = pJsonString.trim();
      jsonString = new String( trimmed.substring( trimmed.indexOf( "{" ) + 1, trimmed.lastIndexOf( "}" ) ) );
    }

    //LOG.debug( "formatDataObject '" + jsonString + "'" );
    
    String currProp = jsonString;
    if (hasName( jsonString ))
    {
      //LOG.debug( "hasName!" );
      String objName = getName( jsonString );
      dobj.setName( objName );
      //LOG.debug( "object name" + objName );
    
      currProp = new String( jsonString.substring( jsonString.indexOf( ":" ) + 1 ) );
      currProp = new String( currProp.substring( currProp.indexOf( "{" ) + 1, currProp.lastIndexOf( "}" )) );
    }
      
        
    while( currProp.trim().length() > 0)
    {
      //LOG.debug( "currProp = '" + currProp + "'" );
            
      String name = (currProp.trim().startsWith( "{" ) ) ? "" : getName( currProp );
      //LOG.debug( "name = " + name );
      Object value = getValue( currProp );
      if (value == null) {
        //LOG.debug( "value was null for " + currProp );
        return;
      }
        
      //LOG.debug( "GOT value '" + value + "'" );
      if (value instanceof String)
      {
        String valStr = ((String)value).trim( );
        //LOG.debug( "ValStr =  '" + valStr + "'" );
        // if value starts with " find matching " value is a String
        if (valStr.startsWith( "\"" ))
        {
          valStr = findMatching( new String( valStr.substring( 1 ) ), "\"" );
          if (valStr != null)
          {
            //LOG.debug( "creating String property: " + name + " = " + valStr );
            dobj.addProperty( new StringProperty( name, valStr ));
          }
        }
        // if value starts with { find matching } value is an Object
        else if (valStr.startsWith( "{" ))
        {
          valStr = findMatching( valStr, "{", "}" );
          if (valStr != null)
          {
            //LOG.debug( "transforming '" + valStr + "'" );
            IProperty prop = transformString( valStr );
            if (prop != null)
            {
              //LOG.debug( "Created prop " + prop + ": " + name );
              prop.setName( name );
              dobj.addProperty( prop );
            }
          }
        }
        // if value starts with [ find matching ] value is an Array - make it a PropertyList
        // wouldn't be found by getValue( ) if it has a trailing comma ...
        else if (valStr.startsWith( "[" ))
        {
          //LOG.debug( "Creating PropertyList for " + name );
                    
          valStr = findMatching( valStr, "[", "]" );
          if (valStr != null)
          {
            String trimmed = valStr.trim();
            //LOG.debug( trimmed );
            ArrayList<String> jsonArray = getJsonArray( new String( trimmed.substring( 1, trimmed.indexOf( "]" ) ) ) );

            PropertyList pl = new PropertyList( );
            pl.setName( name );
            for (int i = 0; i < jsonArray.size( ); i++)
            {
              IProperty prop = transformString( jsonArray.get( i ) );
              if (prop != null)
              {
                //LOG.debug( "Created prop " + prop + ": " + name );
                prop.setName( name );
                pl.addProperty( prop );
              }
            }
                    
            dobj.addProperty( pl );
          }
        }
        else
        {
          // find the comma or not, create a property from the valStr
          if ( findNext( valStr, "," ) > 0)
          {
            valStr = new String( valStr.substring( 0, findNext( valStr, "," ) ) ).trim( );
            //LOG.debug( "valStr = '" + valStr + "'" );
            if (nameLabel != null && name.equals( nameLabel ))
            {
              dobj.setName( valStr );
            }
            else if (idLabel != null && name.equals( idLabel ))
            {
              dobj.setID( valStr );
            }
            else if (schemaLabel != null && name.equals( schemaLabel ))
            {
              dobj.setDataObjectSchema( valStr );
            }
            else
            {
              Object valOb = getValue( valStr );
              //LOG.debug( "getting value for '" + valStr + "'" );
              // check if Integer, etc...
              if (valOb != null && valOb instanceof String)
              {
                String valSt = (String)valOb;
                if (StringMethods.isNumber( valSt ))
                {
                  dobj.addProperty( new ScalarQuantity( name, valSt ) );
                }
                else if (valSt.equalsIgnoreCase( "true" ) || valSt.equalsIgnoreCase( "false" ))
                {
                  dobj.addProperty( new BooleanProperty( name, valSt ) );
                }
                else
                {
                  //LOG.debug( "Creating StringProperty " + name + " = " + valSt );
                  dobj.addProperty( new StringProperty( name, valSt ) );
                }
              }
              else if ( valOb != null && valOb instanceof Integer)
              {
                dobj.addProperty( new IntegerProperty( name, (Integer)valOb ));
              }
              else if ( valOb != null && valOb instanceof Long )
              {
                dobj.addProperty( new LongProperty( name, (Long)valOb ));
              }
            }
          }
          else
          {
            //LOG.debug( "creating String property: " + name + " = " + valStr );
            dobj.addProperty( new StringProperty( name, valStr ));
          }
        }
                
        // ... once we have the value, check for a comma - strip it and move on ...
        currProp = new String( currProp.substring( currProp.indexOf( valStr ) + valStr.length() ));
        //LOG.debug( "currProp is now ' " + currProp + "'" );
        if ( currProp.indexOf( "," ) >= 0)
        {
          currProp = new String( currProp.substring( currProp.indexOf( "," ) + 1 ) ).trim( );
        }
        else
        {
          currProp = "";
        }
        //LOG.debug( "now currProp is '" + currProp );
      }
      else if (value instanceof Integer )
      {
        dobj.addProperty( new IntegerProperty( name, (Integer)value ));
        currProp = "";
      }
      else if (value instanceof Long )
      {
        dobj.addProperty( new LongProperty( name, (Long)value ));
        currProp = "";
      }
      else if (value instanceof ArrayList )
      {
        PropertyList pl = new PropertyList( );
        pl.setName( name );
        @SuppressWarnings("unchecked")
        ArrayList<String> values = (ArrayList<String>)value;
        for (int i = 0, isz = values.size( ); i < isz; i++)
        {
          IProperty prop = transformString( values.get(i) );
          if (prop != null)
          {
            prop.setName( name );
            pl.addProperty( prop );
          }
        }
                
        dobj.addProperty( pl );
                
        String arrayStr = findMatching( currProp.trim(), "[", "]" );
        if (arrayStr != null)
        {
          currProp = new String( currProp.substring( currProp.indexOf( arrayStr ) + arrayStr.length() ) );
          if ( findNext( currProp,  "," ) >= 0)
          {
            currProp = new String( currProp.substring( findNext( currProp, "," ) + 1 ) );
          }
          else
          {
            currProp = "";
          }
        }
        else
        {
          currProp = "";
        }
      }
      else if (value instanceof DataObject )
      {
        DataObject nestedObj = (DataObject)value;
        if (name != null && name.trim().length() > 0)
        {
          nestedObj.setName( name );
          currProp = new String( currProp.substring( currProp.indexOf( "{" ) - 1 ) );
        }
        dobj.addProperty( nestedObj );
                
        String dobjStr = findMatching( currProp.trim(), "{", "}" );
        if (dobjStr != null)
        {
          currProp = new String( currProp.substring( currProp.indexOf( dobjStr ) + dobjStr.length() ) );
          if ( findNext( currProp, "," ) >= 0)
          {
            currProp = new String( currProp.substring( findNext( currProp, "," ) + 1 ) );
          }
          else
          {
            currProp = "";
          }
        }
        else
        {
          currProp = "";
        }
      }
    }
  }
    
  private boolean hasName( String jsonString )
  {
    int firstColon = jsonString.indexOf( ":" );
    int firstBrace = jsonString.indexOf( "{" );
    if (firstColon > 0)
    {
      String value = new String( jsonString.substring( firstColon + 1 ));
      if (value != null && value.trim().startsWith( "{"))
      {
        return (firstBrace < 0 || firstBrace > firstColon);
      }
    }
        
    return false;
  }

  private String findMatching( String input, String endChar )
  {
    //LOG.debug( "findMatching " + input + " '" + endChar + "'" );
    char endCh = endChar.charAt( 0 );
      
    int i = 0;
    boolean escaped = false;
    boolean inQuotes = false;
    while ( i < input.length() )
    {
      if      (escaped) escaped = false;
      else if (input.charAt( i ) == '\\' )    escaped = true;
      else if (!endChar.equals( "\"" ) && input.charAt( i ) == '\"' )  inQuotes = !inQuotes;
      else if (input.charAt( i ) == endCh  )   break;
      ++i;
      if (i == input.length()) return null;
    }
      
    String result = new String( input.substring( 0, i ) );
    return result;
  }
    
  private String findMatching( String input, String start, String end )
  {
    if (input.indexOf( end ) < 0) return null;
    return findNestedExpression( input, start, end );
  }
    
  private String findNestedExpression( String str, String startChar, String endChar )
  {
    int startPos = str.indexOf( startChar );
    if (startPos < 0) return null;
    	
    return findNestedExpression( str, startChar, endChar, startPos );
  }
    
  private String findNestedExpression( String str, String startChar, String endChar, int startPos )
  {
    int endPos = findEnd( str, startChar.charAt(0), endChar.charAt(0), startPos );
    if (endPos > startPos)
    {
      return new String( str.substring( startPos, endPos + 1 ) );
    }
    	
    return null;
  }
    
  private int findEnd( String str, char startChar, char endChar, int startPos )
  {
    int i = startPos + 1;
    boolean escaped = false;
    boolean inQuotes = false;
    for ( int nestCount = 1; nestCount > 0 && i < str.length(); ++i )
    {
      if      (escaped) escaped = false;
      else if (str.charAt( i ) == '\\' )     escaped = true;
      else if (startChar != '\"' && endChar != '\"' && str.charAt( i ) == '\"' )     inQuotes = !inQuotes;
      else if (str.charAt( i ) == startChar ) ++nestCount;
      else if (str.charAt( i ) == endChar )   --nestCount;
    }

    return i-1;
  }
    
  private int findNext( String str, String theChar )
  {
    char c = theChar.charAt( 0 );
    boolean escaped = false;
    boolean inQuotes = false;
    for ( int i = 0; i < str.length(); ++i )
    {
      if      (escaped) escaped = false;
      else if (str.charAt( i ) == '\\' )     escaped = true;
      else if ( c != '\"' && str.charAt( i ) == '\"' )     inQuotes = !inQuotes;
      else if ( str.charAt( i ) == c && !inQuotes ) return i;
    }
      
    return -1;
  }
    
}
