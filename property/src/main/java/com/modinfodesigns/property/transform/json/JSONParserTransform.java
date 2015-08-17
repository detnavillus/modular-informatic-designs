package com.modinfodesigns.property.transform.json;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataObjectBuilder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.BasePropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.quantity.IntegerProperty;

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
    IProperty jsonObject = transformString( jsonString );
    if (jsonObject == null)
    {
      throw new PropertyTransformException( "Could not parse jsonString " );
    }
        
    return jsonObject;
  }

  private IProperty transformString( String jsonString )
  {
    LOG.debug( "transformString: '" + jsonString + "'" );
        
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
    LOG.debug( "createProperty: '" + jsonString + "'" );
        
    String name = (jsonString.indexOf( ":" ) > 0) ? getName( jsonString ) : "";
    Object value = getValue( jsonString );
        
    if (value instanceof String)
    {
      String valStr = (String)value;
      if (StringMethods.isInteger( valStr ))
      {
        return new IntegerProperty( name, Integer.parseInt( valStr ) );
      }
      else
      {
        return new StringProperty( name, (String)value );
      }
    }
    else if (value instanceof Integer )
    {
      return new IntegerProperty( name, (Integer)value );
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
        prop.setName( name );
        pl.addProperty( prop );
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
        
    LOG.debug( "getName: " + jsonString );
    String name = jsonString.substring( 0, jsonString.indexOf( ":" ));
        
    LOG.debug( "name " + name );
        
    // strip quotes from name ...
    if (name.trim().startsWith( "\""))
    {
      name = StringTransform.replaceSubstring( name, "\"", "" );
    }
        
    LOG.debug( "returning '" + name.trim() + "'" );
    return (name != null) ? name.trim() : null;
  }
    

  private Object getValue( String pJsonString )
  {
    LOG.debug( "getValue: '" + pJsonString + "'" );
        
    if (pJsonString == null || pJsonString.indexOf( ":" ) < 0)
    {
      return (StringTransform.isInteger( pJsonString.trim() ) ) ? new Integer( pJsonString.trim() ) : pJsonString;
    }
        
    String jsonString = pJsonString.trim();
    if (jsonString.startsWith( "{" ))
    {
      String objStr = findMatching( jsonString, "{", "}" );
      return createDataObject( objStr );
    }
        
    String value = (jsonString.indexOf( ":" ) > 0) ? new String( jsonString.substring( jsonString.indexOf( ":" ) + 1 ) ) : jsonString;
    LOG.debug( "value = '" + value.trim() + "'");
        
    if (value.trim().startsWith( "[" ) )
    {
      String trimmed = value.trim();
      String arrayStr = findMatching( trimmed, "[", "]" );
      LOG.debug( "Matching array: " + arrayStr );
            
      return getJsonArray( new String( arrayStr.substring( 1, arrayStr.lastIndexOf( "]" ) ) ) );
    }
    else if (value.trim().startsWith( "{" ))
    {
      String objString = findMatching( value.trim( ), "{", "}" );
      DataObject dobj = createDataObject( objString );
      String name = getName( jsonString.substring( 1 ) );
      dobj.setName( name );
      return dobj;
    }
    else if (value.trim().startsWith( "\"" ))
    {
      LOG.debug( "value = '" + value + "'" );
      String matching = findMatching( value.trim(), "\"", "\"" );
      LOG.debug( "matching = '" + matching + "'" );
      return matching;
    }
    else if (StringTransform.isInteger( value.trim() ))
    {
      LOG.debug( "Integer!" );
      return new Integer( value.trim( ) );
    }
        
    if (value != null && value.trim().endsWith( "}" )) value = value.trim().substring( 0, value.lastIndexOf( "}" ));
    LOG.debug( "returning: '" + value.trim( ) + "'" );
        
    return (value != null) ? value.trim() : null;
  }
    
  public ArrayList<String> getJsonArray( String jsonArray )
  {
    LOG.debug( "getJsonArray '" + jsonArray + "'" );
        
    // parse the JSON array into objects or arrays or values
    // read each character
    ArrayList<String> valueList = new ArrayList<String>( );
    String currStr = jsonArray.trim( );
    while (currStr.trim().length() > 0 )
    {
      currStr = currStr.trim( );
      LOG.debug( "start loop: curStr = '" + currStr + "'" );
      String valStr = currStr;
      if (currStr.startsWith( "\"" ))
      {
        valStr = findMatching( new String( currStr.substring( 1 )), "\"" );
        LOG.debug( "quoted: '" + valStr + "'" );
        // if valStr is followed by a ":" now, - its a property, else its just
        // a string
        String rest = new String( currStr.substring( valStr.length() + 1 ));
        LOG.debug( "the rest: '" + rest + "'" );
        if (rest.trim().startsWith( "\"" ))
        {
          String restRest = rest.substring( 1 );
          if (restRest.trim().startsWith( ":" ))
          {
            if (rest.indexOf( "," ) > 0)
            {
              valStr = "\"" + valStr + new String( rest.substring( 0, rest.indexOf( "," )));
            }
            else
            {
              valStr = "\"" + valStr + rest;
            }
          }
                    
          LOG.debug( "Found pair value: '" + valStr + "'" );
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
        valStr = (currStr.indexOf( "," ) > 0) ? new String( currStr.substring(0, currStr.indexOf( "," ))) : currStr;
      }
            
      LOG.debug( "valStr: '" + valStr + "'" );
      valueList.add( valStr );
      currStr = currStr.substring( currStr.indexOf( valStr ) + valStr.length() );

      LOG.debug( "currStr = '" + currStr + "'" );
      // ... once we have the value, check for a comma - strip it and move on ...
      if (currStr.trim().indexOf( "," ) == 0)
      {
        currStr = new String( currStr.substring( currStr.indexOf( "," ) + 1 ) );
      }
      else
      {
        currStr = "";
      }
            
      LOG.debug( "currStr '" + currStr + "'" );
    }
        
    LOG.debug( "Returning Value List..." );
    return valueList;
  }
    
  @Override
  public DataObject createDataObject( String propString )
  {
    LOG.debug( "createDataObject: '" + propString + "'" );
        
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
        LOG.error( "Could not create Data Object class '" + dataObjectClass + "'" );
        LOG.debug( "Could not create Data Object class '" + dataObjectClass + "'" );
      }
    }
        
    if (dobj == null) dobj = new DataObject( );
    
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
        
    if (pJsonString.trim().startsWith( "{" ) && pJsonString.trim().endsWith( "}"))
    {
      String trimmed = pJsonString.trim();
      jsonString = new String( trimmed.substring( trimmed.indexOf( "{" ) + 1, trimmed.lastIndexOf( "}" ) ) );
    }
        
    String currProp = jsonString;
    if (hasName( jsonString ))
    {
      LOG.debug( "hasName!" );
      String objName = getName( jsonString );
      dobj.setName( objName );
    
      currProp = jsonString.substring( jsonString.indexOf( ":" ) + 1 );
      currProp = new String( currProp.substring( currProp.indexOf( "{" ) + 1, currProp.lastIndexOf( "}" )) );
    }
        
    while( currProp.trim().length() > 0)
    {
      LOG.debug( "currProp = '" + currProp + "'" );
            
      String name = (currProp.trim().startsWith( "{" ) ) ? "" : getName( currProp );
      Object value = getValue( currProp );

      if (value instanceof String)
      {
        String valStr = ((String)value).trim( );
        // if value starts with " find matching " value is a String
        if (valStr.startsWith( "\"" ))
        {
          valStr = findMatching( new String( valStr.substring( 1 ) ), "\"" );
          LOG.debug( "creating String property: " + name + " = " + valStr );
          dobj.addProperty( new StringProperty( name, valStr ));
        }
        // if value starts with { find matching } value is an Object
        else if (valStr.startsWith( "{" ))
        {
          valStr = findMatching( valStr, "{", "}" );
          IProperty prop = transformString( valStr );
          prop.setName( name );
          dobj.addProperty( prop );
        }
        // if value starts with [ find matching ] value is an Array - make it a PropertyList
        // wouldn't be found by getValue( ) if it has a trailing comma ...
        else if (valStr.startsWith( "[" ))
        {
          LOG.debug( "Creating PropertyList for " + name );
                    
          valStr = findMatching( valStr, "[", "]" ).trim( );
          String trimmed = valStr.trim();
          ArrayList<String> jsonArray = getJsonArray( new String( trimmed.substring( 1, trimmed.indexOf( "]" ) ) ) );

          PropertyList pl = new PropertyList( );
          pl.setName( name );
          for (int i = 0; i < jsonArray.size( ); i++)
          {
            IProperty prop = transformString( jsonArray.get( i ) );
            prop.setName( name );
            pl.addProperty( prop );
          }
                    
          dobj.addProperty( pl );
        }
        else
        {
          // find the comma or not, create a property from the valStr
          if (valStr.indexOf( "," ) > 0)
          {
            valStr = new String( valStr.substring( 0, valStr.indexOf( "," ) ) ).trim( );
            LOG.debug( "valStr = '" + valStr + "'" );
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
              LOG.debug( "getting value for '" + valStr + "'" );
              // check if Integer, etc...
              if (valOb instanceof String)
              {
                dobj.addProperty( new StringProperty( name, (String)valOb));
              }
              else if (valOb instanceof Integer)
              {
                dobj.addProperty( new IntegerProperty( name, (Integer)valOb ));
              }
            }
          }
        }
                
        // ... once we have the value, check for a comma - strip it and move on ...
        currProp = currProp.substring( currProp.indexOf( valStr ) + valStr.length() );
        LOG.debug( "currProp is now '" + currProp + "'" );
        if (currProp.indexOf( "," ) >= 0)
        {
          currProp = new String( currProp.substring( currProp.indexOf( "," ) + 1 ) );
        }
        else
        {
          currProp = "";
        }
      }
      else if (value instanceof Integer )
      {
        dobj.addProperty( new IntegerProperty( name, (Integer)value ));
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
          prop.setName( name );
          pl.addProperty( prop );
        }
                
        dobj.addProperty( pl );
                
        String arrayStr = findMatching( currProp.trim(), "[", "]" );
        currProp = new String( currProp.substring( currProp.indexOf( arrayStr ) + arrayStr.length() ) );
        if (currProp.indexOf( "," ) >= 0)
        {
          currProp = new String( currProp.substring( currProp.indexOf( "," ) + 1 ) );
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
        currProp = new String( currProp.substring( currProp.indexOf( dobjStr ) + dobjStr.length() ) );
        if (currProp.indexOf( "," ) >= 0)
        {
          currProp = new String( currProp.substring( currProp.indexOf( "," ) + 1 ) );
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
    char endCh = endChar.charAt( 0 );
      
    int i = 0;
    boolean escaped = false;
    while ( i < input.length() )
    {
      if      (escaped) escaped = false;
      else if (input.charAt( i ) == '\\' )      escaped = true;
      else if (input.charAt( i ) == endCh )   break;
      ++i;
    }
      
    String result = new String( input.substring( 0, i ) );
    return result;
  }
    
  private String findMatching( String input, String start, String end )
  {
    return StringTransform.findNestedExpression( input, start, end );
  }
}
