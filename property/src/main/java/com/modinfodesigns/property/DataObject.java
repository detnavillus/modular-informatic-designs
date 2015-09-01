package com.modinfodesigns.property;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.property.time.DateProperty;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;
import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.ScalarQuantity;

import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;


import com.modinfodesigns.property.transform.string.StringTransform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Class for Data Objects. Implements the IPropertyHolder interface. A DataObject can represent 
 * a complex data model with nested Property Objects and other IPropertyHolder objects (DataObject,
 * DataList ).
 * 
 * getValue( ) handles XPath like query strings.
 * 
 * @author Ted Sullivan
 */

public class DataObject implements IPropertyHolder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObject.class );

  // Format for nested data objects in persistence mode
  public static final String NAME_ID = "NameID";
  public static final String NAME_ID_SCHEMA = "NameIDSchema";
  public static final String ID_SCHEMA = "IDSchema";
    
  public static final String OBJECT_NAME_PARAM = "objectName";
  public static final String OBJECT_ID_PARAM = "objectId";
  public static final String SCHEMA_PARAM = "schema";
  public static final String ENTITY_TYPE_PARAM = "entityType";
    
  protected String name;
  protected String ID;
    
  protected String jsonNameLabel;
  protected String idLabel = "id";
  protected String schemaLabel = "schema";
    
  private HashMap<String,IProperty> propMap = new HashMap<String,IProperty>( );
    
  private HashMap<String,ArrayList<IAssociation>> associations = new HashMap<String,ArrayList<IAssociation>>( );
    
  private HashMap<String,Object> objects;
    
  private ArrayList<IFunctionProperty> functionProps;
    
  private boolean uniquePropertiesOnly = true;
    
  private boolean isRootObject = false;
    
  private String nestedDelimiter = "/";
    
  private String dataSchemaName;
    
  private DataObject proxyObject;		// remote or reference object
    
  public DataObject(  ) {  }
    
  public DataObject( String name )
  {
    this.name = name;
  }
    
    
  public void setUniquePropertiesOnly( boolean uniquePropertiesOnly )
  {
    this.uniquePropertiesOnly = uniquePropertiesOnly;
  }
    
  public void setNestedDelimiter( String nestedDelimiter )
  {
    this.nestedDelimiter = nestedDelimiter;
  }
    
  public void setDataObjectSchema( String dataSchemaName )
  {
    this.dataSchemaName = dataSchemaName;
  }
    
  public String getDataObjectSchema(  )
  {
    return this.dataSchemaName;
  }
    
    
  @Override
  public String getName()
  {
    return this.name;
  }
    
  public void setName( String name )
  {
    this.name = name;
  }
    
  public void setJsonNameLabel( String jsonNameLabel )
  {
    this.jsonNameLabel = jsonNameLabel;
  }

  @Override
  public String getID()
  {
    return this.ID;
  }
    
  @Override
  public void setID( String ID )
  {
    this.ID = ID;
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
  public String getType()
  {
    return this.getClass().getCanonicalName();
  }
    
  protected String getXMLTagName( )
  {
    return "DataObject";
  }
    
  public void setProxyObject( DataObject proxyObject )
  {
    this.proxyObject = proxyObject;
  }
    
  public DataObject getProxyObject( )
  {
    return this.proxyObject;
  }
    
  public boolean isProxied(  )
  {
    return this.proxyObject != null;
  }
    
  public String getProxyKey( )
  {
    if (this.proxyObject == null) return null;
    return proxyObject.getName( ) + "|"
        + ((proxyObject.getID() != null) ? proxyObject.getID( ) : "") + "|"
        + proxyObject.getDataObjectSchema( );
  }
    
  @Override
  public void setIsRootObject( boolean isRoot )
  {
    LOG.debug( this + " setIsRootObject: " + isRoot );
    this.isRootObject = isRoot;
  }
    
  @Override
  public boolean isRootObject( )
  {
    LOG.debug( this + " isRootObject: " + isRootObject );
    return this.isRootObject;
  }

  @Override
  public String getValue()
  {
    if (proxyObject != null) return proxyObject.getValue( );
    	
    return getValue( IProperty.JSON_FORMAT );
  }

  @Override
  public String getValue( String format )
  {
    HashSet<String> dobjs = new HashSet<String>( );
    dobjs.add( this.toString( ) );
      
    if (proxyObject != null)
    {
      return proxyObject.getValue( format, dobjs );
    }
    return doGetValue( format, dobjs );
  }
    
  protected String doGetValue( String format, Set<String> dobjs )
  {
    if (format == null || format.equals( IProperty.JSON_FORMAT ) || format.equals( IProperty.JSON_VALUE ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "{" );

      if (getName() != null && getName().trim().length() > 0 && jsonNameLabel != null)
      {
        sbr.append( "\"" ).append( jsonNameLabel ).append( "\":\"" ).append( getName( ) ).append( "\"" );
      }
            
      if (getID( ) != null && getID().trim().length() > 0)
      {
        if (sbr.length() > 1 ) sbr.append( "," );
        sbr.append( "\"" ).append( idLabel ).append( "\":\"" ).append( getID( )).append( "\"" );
      }
            
      if (getDataObjectSchema( ) != null && getDataObjectSchema().trim().length() > 0)
      {
        if (sbr.length() > 1 ) sbr.append( "," );
        sbr.append( "\"" ).append( schemaLabel ).append( "\":\"" ).append( getDataObjectSchema( )).append( "\"" );
      }
            
      //if (sbr.length() > 1 ) sbr.append( "," );
      //sbr.append( "\"type\"" ).append( getType( ) ).append( "\"" );

      for (IProperty prop : propMap.values( ) )
      {
        if ((prop instanceof IntrinsicPropertyDelegate) == false &&
            (prop instanceof IFunctionProperty) == false)
        {
          if (sbr.length() > 1 ) sbr.append( "," );
          if (prop instanceof DataObject )
          {
            DataObject chObj = (DataObject)prop;
            String chLabel = chObj.jsonNameLabel;
            chObj.jsonNameLabel = this.jsonNameLabel;
                		
            sbr.append( "\"" ).append( chObj.getName() ).append( "\":" )
               .append( chObj.getValue( IProperty.JSON_FORMAT, dobjs ));
    				    
            chObj.jsonNameLabel = chLabel;
          }
          else
          {
            sbr.append( "\"" ).append( prop.getName() ).append( "\":" );
            String propVal = null;
            if (prop instanceof PropertyList)
            {
              PropertyList pl = (PropertyList)prop;
              propVal = pl.getValue( IProperty.JSON_VALUE, dobjs );
            }
            else
            {
              propVal = prop.getValue( IProperty.JSON_VALUE );
            }
            if (propVal == null) propVal = "";
    				    
            if (!propVal.startsWith( "\"")  && !propVal.endsWith( "\"" ) )
            {
              propVal = "\"" + propVal + "\"";
            }
    				    
            sbr.append( propVal );
          }
        }
      }
            
      sbr.append( "}" );
      return sbr.toString();
    }
    else if (format.equals(IProperty.XML_FORMAT) || format.equals(IProperty.XML_FORMAT_CDATA))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<" ).append( getXMLTagName( ));
      if (name != null && name.trim().length() > 0)
      {
        sbr.append( " name=\"" ).append( StringTransform.escapeXML( this.name )).append( "\"" );
      }
      if (ID != null && ID.trim().length() > 0)
      {
        sbr.append( " ID=\"" ).append( StringTransform.escapeXML( ID )).append( "\"" );
      }
      if (dataSchemaName != null && dataSchemaName.trim().length() > 0)
      {
        sbr.append( " schema=\"" ).append( StringTransform.escapeXML( dataSchemaName )).append( "\"" );
      }
      sbr.append( ">" );
            
      for (IProperty value : propMap.values( ) )
      {
        if ((value instanceof IntrinsicPropertyDelegate) == false)
        {
          sbr.append( getValue( value, format, dobjs ));
        }
      }
            
      sbr.append( "</" ).append( getXMLTagName( )).append( ">" );
      return sbr.toString( );
    }
    else if (format.equals( NAME_ID ))
    {
      return getName( ) + "|" + ID;
    }
    else if (format.equals( NAME_ID_SCHEMA ))
    {
      return ((getName() != null) ? getName( ) : "") + "|"
            + ((ID != null) ? ID : "") + "|"
            + ((dataSchemaName != null) ? dataSchemaName : "");
    }
    else if (format.equals( ID_SCHEMA ))
    {
      return ID + "|" + dataSchemaName;
    }
    else
    {
      String refVal = getReferencedValue( format );
      if (refVal != null)
      {
        return refVal;
      }
      IProperty nestedProp = getProperty( format );
      if (nestedProp != null)
      {
        return getValue( nestedProp, dobjs );
      }
        
      return null;
    }
  }
    
  private String getValue( IProperty prop, String format, Set<String> dobjs )
  {
    if (format == null) return getValue( prop, dobjs );

    if (prop instanceof DataObject)
    {
      DataObject dob = (DataObject)prop;
      return  dob.getValue( format, dobjs );
    }
    else if (prop instanceof PropertyList )
    {
      PropertyList pl = (PropertyList)prop;
      return pl.getValue( format, dobjs );
    }
    else
    {
      return prop.getValue( format );
    }
  }
    
  private String getValue( IProperty prop, Set<String> dobjs )
  {
    if (prop instanceof DataObject)
    {
      DataObject dob = (DataObject)prop;
      return  dob.getValue( dobjs );
    }
    else if (prop instanceof PropertyList )
    {
      PropertyList pl = (PropertyList)prop;
      return pl.getValue( dobjs );
    }
    else
    {
      return prop.getValue(  );
    }
  }
    
  String getValue( String format, Set<String> dobjs )
  {
    if (dobjs.contains( this.toString() ) )
    {
      return "@" + getName( );
    }
    dobjs.add( this.toString( ) );
    return doGetValue( format, dobjs );
  }
        
  String getValue( Set<String> dobjs )
  {
    if (dobjs.contains( this.toString() ) )
    {
      return "@" + getName( );
    }
    dobjs.add( this.toString( ) );
    return doGetValue( null, dobjs );
  }
    
  // Treats [pname] or ['pname'] as getProperty( "pname" ).getValue( )
  public String getReferencedValue( String query )
  {
    HashSet<String> dobjs = new HashSet<String>( );
    dobjs.add( this.toString( ) );
    return getReferencedValue( query, dobjs );
  }

  protected String getReferencedValue( String query, Set<String> dobjs )
  {
    if (proxyObject != null) return proxyObject.getReferencedValue( query, dobjs );
    	
    if (query == null) return null;
            
    IProperty prop = getReferencedProp( query);
    if (prop == null) return null;

    String format = null;
    if (query.indexOf( "]") < query.length())
    {
      format = new String( query.substring( query.indexOf( "]" ) + 1 )).trim( );
    }
    return getValue( prop, format, dobjs );
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    if (proxyObject != null)
    {
      throw new PropertyValidationException( "Is Proxied: Cannot set values" );
    }
    	
    if (value == null)
    {
      return;
    }
    	
    // =========================================================
    // Uses XMLParserTransform or JSONParserTransform to build
    // the DataObject from  a formatted String.
    // =========================================================
    if (format != null && format.equals( IProperty.XML_FORMAT ) && value.trim().startsWith( "<" ))
    {
      XMLParserTransform xmlParserXform = new XMLParserTransform();
      xmlParserXform.formatDataObject( this,  value );
    }
    else if (format != null && format.equals( IProperty.JSON_FORMAT ) && value.trim().startsWith( "{" ))
    {
      JSONParserTransform jsonParserXform = new JSONParserTransform();
      jsonParserXform.setJsonNameLabel( this.jsonNameLabel );
      jsonParserXform.setIDLabel( this.idLabel );
      jsonParserXform.setSchemaLabel( this.schemaLabel );
            
      jsonParserXform.formatDataObject( this, value );
    }
    else if (format != null && format.contains( nestedDelimiter ))
    {
      LOG.debug( " setting nested property " + format );
      IProperty nestedProp = getProperty( format );
      if (nestedProp != null) nestedProp.setValue( value, null );
    }
    else
    {
      throw new PropertyValidationException( "Cannot set property value " + value + " format = " + format );
    }
  }

  @Override
  public IProperty getProperty( String name )
  {
    if (proxyObject != null) return proxyObject.getProperty( name );
    	
    if (name == null) return null;
        
    IProperty prop = (name != null) ? propMap.get( name ) : null;
    LOG.debug( "getProperty( " + name + " ) returned:" + prop );
        
    // Return a copy of a Function Property so that our local copy
    // will remain bound to this DataObject
    if (prop != null && prop instanceof IFunctionProperty)
    {
      IFunctionProperty propCop = (IFunctionProperty)prop.copy( );
      propCop.setPropertyHolder( this );
      return propCop;
    }
        
    if (prop != null && prop instanceof DataObjectDelegate)
    {
      DataObjectDelegate dod = (DataObjectDelegate)prop;
      return dod.getDelegate( );
    }
        
    if (prop != null) return prop;
        
    if ( nestedDelimiter != null && name.indexOf( nestedDelimiter ) >= 0)
    {
      String pName = name;
            
      if ( name.indexOf( nestedDelimiter ) == 0 )
      {
        pName = new String( name.substring( nestedDelimiter.length() ));
      }
      else
      {
        String localName = new String( pName.substring( 0, pName.indexOf( nestedDelimiter )));
        String subPath   = new String( name.substring( name.indexOf( nestedDelimiter ) + nestedDelimiter.length()));
                
        // could check if subPath is .name or .type or is an Intrinsic Property ...
                
        return getNestedProperty( localName, subPath );
      }
    }
      
    if (name.indexOf ("[" ) > 0 && name.indexOf( "]") > 0) {
        
      // its either a PropertyList or a DataList
      String realName = name.substring( 0, name.indexOf( "[" ));
      IProperty listProp = getProperty( realName );
      LOG.debug( "got listProp " + listProp );
      String listNdxSt = name.substring( name.indexOf( "[" ) + 1, name.indexOf( "]" ));
      int listNdx = 0;
      try
      {
        listNdx = Integer.parseInt( listNdxSt );
      }
      catch (NumberFormatException nfe )
      {
        return null;
      }
        
      if (listProp instanceof PropertyList)
      {
        PropertyList pl = (PropertyList)listProp;
        return pl.getProperty( listNdx );
      }
      else if (listProp instanceof DataList )
      {
        DataList dl = (DataList)listProp;
        return dl.item( listNdx );
      }
    }
        
    IProperty dictProp = getReferencedProp( name );
    if (dictProp != null)
    {
      return dictProp;
    }
        
    return null;
  }
    
  private IProperty getReferencedProp( String name )
  {
    if (name == null) return null;
    	
    String pName = name.trim();
        
    if ((pName.indexOf( "[" ) == 0 && pName.indexOf( "]" ) > 0)
      ||  (this.name != null && (pName.indexOf( this.name + "[" ) == 0) && pName.indexOf( "]" ) > 0))
    {
      int startNdx = (pName.indexOf( this.name + "[" ) == 0) ? (name.length() + 1) : 1;
      String propName = new String( pName.substring( startNdx, pName.indexOf( "]" )));
      if (propName.startsWith( "'" ))
      {
        propName = new String( propName.substring( 1, propName.lastIndexOf( "'" )));
      }
            
      return getProperty( propName );
    }
        
    return null;
  }

  public IProperty getNestedProperty( String localName, String subPath )
  {
    LOG.debug( "getNestedProperty " + localName );
    if (proxyObject != null) return proxyObject.getNestedProperty( localName, subPath );
    	
    IProperty prop = getProperty( localName );
    if (prop != null && prop instanceof DataObject )
    {
      DataObject nestedObj = (DataObject)prop;
      return nestedObj.getProperty( subPath );
    }
        
    return prop;
  }

  @Override
  public Iterator<IProperty> getProperties()
  {
    if (proxyObject != null) return proxyObject.getProperties();
    	
    return propMap.values().iterator( );
  }
    
  public List<Object> getPropertyList( )
  {
    if (proxyObject != null) return proxyObject.getPropertyList( );
    	
    ArrayList<Object> propObjs = new ArrayList<Object>( );
    Iterator<IProperty> propIt = getProperties( );
    while ( propIt != null && propIt.hasNext( ) )
    {
      propObjs.add( propIt.next( ) );
    }
    	
    return propObjs;
  }

  @Override
  public Iterator<String> getPropertyNames( )
  {
    if (proxyObject != null) return proxyObject.getPropertyNames( );
    	
    return propMap.keySet().iterator( );
  }
 
  @Override
  public void addProperty( IProperty property )
  {
    addProperty( property, true );
  }
    

  public void addProperty( IProperty property, boolean addIntrinsicProps )
  {
    if (proxyObject != null)
    {
      return;
    }
    	
    if (property == null)
    {
      LOG.error( "addProperty called with NULL!" );
      return;
    }
        
    LOG.debug( "addProperty '" + property.getName( ) + "'" );
        
    String name = property.getName( );
    if (!(property instanceof IntrinsicPropertyDelegate))
    {
      IProperty oldProp = propMap.get( name );
      if (oldProp != null)
      {
        if (oldProp instanceof PropertyList)
        {
          PropertyList propList = (PropertyList)oldProp;
          propList.addProperty( property );
        }
        else
        {
          PropertyList propList = new PropertyList( );
          propList.setName( name );
          propList.setUniqueOnly( uniquePropertiesOnly );
                
          propList.addProperty( oldProp );
          propList.addProperty( property );
          propMap.put( name, propList );
        }
      }
      else
      {
        propMap.put( name, property );
      }
    }
    else
    {
      propMap.put( name, property );
    }
        
    // -------------------------------------------------------
    // Handle installation of a FunctionProperty
    // Retain a local copy so that the original handle can
    // be assigned elsewhere - binding or closure established
    // -------------------------------------------------------
    if (property instanceof IFunctionProperty)
    {
      IFunctionProperty funcProp = (IFunctionProperty)property;
      funcProp.setPropertyHolder( this );
      if (functionProps == null) functionProps = new ArrayList<IFunctionProperty>( );
            
      // Retain a private copy of the IFunctionProperty
      IFunctionProperty propCop = (IFunctionProperty)funcProp.copy( );
      propCop.setPropertyHolder( this );
      functionProps.add( propCop );
    }
        
    if (addIntrinsicProps && property instanceof IComputableProperties)
    {
      IComputableProperties computableProps = (IComputableProperties)property;
      List<String> itsProps = computableProps.getIntrinsicProperties( );
      if (itsProps != null)
      {
        for ( String internalProp : itsProps )
        {
          String propName = property.getName( ) + "." + internalProp;
          IntrinsicPropertyDelegate delegate = new IntrinsicPropertyDelegate( propName, internalProp, computableProps );
          addProperty( delegate );
        }
      }
    }
  }

  @Override
  public void setProperty( IProperty property)
  {
      setProperty( property, true );
  }
    
  public void setProperty( IProperty property, boolean addIntrinsicProps )
  {
    // System.out.println( "setProperty " + property.getName() + " " + addIntrinsicProps );
    if (proxyObject != null)
    {
      return;
    }
    	
    if (property == null)
    {
      LOG.error( "setProperty called with NULL!: ");
      return;
    }
        
    LOG.debug( "setProperty " + property.getName( ) );
    propMap.put( property.getName( ), property );
        
    if (property instanceof IFunctionProperty)
    {
      IFunctionProperty funcProp = (IFunctionProperty)property;
      funcProp.setPropertyHolder( this );
      IFunctionProperty propCop = (IFunctionProperty)funcProp.copy( );
      propCop.setPropertyHolder( this );
            
      if (functionProps == null)
      {
        functionProps = new ArrayList<IFunctionProperty>( );
        functionProps.add( propCop );
      }
      else
      {
        int oldNdx = -1;
        for (int i = 0, isz = functionProps.size(); i < isz; i++)
        {
          IFunctionProperty oldFunc = functionProps.get( i );
          if (oldFunc.getName() != null && oldFunc.getName().equals( funcProp.getName() ))
          {
            oldNdx = i;
            break;
          }
        }
        if (oldNdx >= 0)
        {
          functionProps.set( oldNdx, propCop );
        }
        else
        {
          functionProps.add( propCop );
        }
      }
    }
        
    if (addIntrinsicProps &&  property instanceof IComputableProperties)
    {
      IComputableProperties computableProps = (IComputableProperties)property;
      List<String> itsProps = computableProps.getIntrinsicProperties( );
      if (itsProps != null)
      {
        for (String internalProp : itsProps )
        {
          String propName = property.getName( ) + "." + internalProp;
          IntrinsicPropertyDelegate delegate = new IntrinsicPropertyDelegate( propName, internalProp, computableProps );
          setProperty( delegate );
        }
      }
    }
        
    LOG.debug( "setProperty " + property.getName( ) + " DONE." );
  }
    
  @Override
  public void setProperties( Iterator<IProperty> properties )
  {
    if (proxyObject != null)
    {
      return;
    }
    	
    while (properties != null && properties.hasNext() )
    {
      setProperty( properties.next() );
    }
  }
    
  @Override
  public void removeProperty( String propName )
  {
    if (proxyObject != null)
    {
      return;
    }
    	
    if (propName == null)
    {
      LOG.error( "removeProperty called with NULL!" );
      return;
    }
        
    // check if its the name of a function property -
    // if so, remove it from the function property list
    if (functionProps != null)
    {
      int oldNdx = -1;
      for (int i = 0, isz = functionProps.size(); i < isz; i++)
      {
        IFunctionProperty oldFunc = functionProps.get( i );
        if (oldFunc.getName() != null && oldFunc.getName().equals( propName ))
        {
          oldNdx = i;
          break;
        }
      }
      if (oldNdx >= 0)
      {
        functionProps.remove( oldNdx );
      }
    }
     
    // if property is in a property list - remove it from the list
    if (propMap != null) propMap.remove( propName );
  }
    
  /**
   * Removes a child or descendent DataObject property with the specified ID.
   *
   * @param objectID
   */
  public void removeDataObject( String objectID )
  {
    if (proxyObject != null)
    {
      return;
    }
    	
    // ==========================================================================
    // iterate through properties - if Property is a data object
    // get the dobj, if the dobj has the ID, remove that Property by name
    // else call removeDataObject on the child object so that any descendent
    // with the ID is removed.
    // ==========================================================================
    
    String propToRemove = null;
    Iterator<IProperty> propIt = getProperties( );
    while( propIt != null && propIt.hasNext( ))
    {
      IProperty prop = propIt.next( );
      if (prop instanceof PropertyList )
      {
        PropertyList propLst = (PropertyList)prop;
        int propNdx = -1;
        for (int i = 0; i < propLst.size(); i++) {
          IProperty lProp = propLst.getProperty( i );
          if (lProp instanceof DataObject && ((DataObject)lProp).getID().equals( objectID ))
          {
            propNdx = i;
            break;
          }
          if (propNdx >= 0)
          {
            propLst.removeProperty( propNdx );
          }
        }
      }
      else if (prop instanceof DataObject)
      {
        DataObject chOb = (DataObject)prop;
        if (chOb.getID() != null && chOb.getID().equals( objectID ))
        {
          propToRemove = prop.getName( );
          break;
        }
        else
        {
          chOb.removeDataObject( objectID );
        }
      }
    }
    	
    if (propToRemove != null)
    {
      removeProperty( propToRemove );
    }
    	
  }

  /**
   * Overridden by subclasses (such as TaxonomyNode) that want to create reciprocal associations.
   */
  @Override
  public void addAssociation(IAssociation association) throws AssociationException
  {
    ArrayList<IAssociation> assocList = associations.get( association.getName() );
    if (assocList == null)
    {
      assocList = new ArrayList<IAssociation>( );
      associations.put( association.getName(), assocList );
    }
        
    if (!association.isMultiple()  && assocList.size() == 1)
    {
      throw new AssociationException( "Cannot have more than one!" );
    }
        
    assocList.add( association );
  }
    
  @Override
  public void setAssociation( IAssociation association )
  {
    ArrayList<IAssociation> assocList = associations.get( association.getName() );
    if (assocList == null)
    {
      assocList = new ArrayList<IAssociation>( );
      associations.put( association.getName(), assocList );
    }
    else
    {
      assocList.clear( );
    }
        
    assocList.add( association );
  }
    
    
  @Override
  public Iterator<String> getAssociationNames( )
  {
    return associations.keySet().iterator( );
  }
    
  public List<IAssociation> getAssociations( String name )
  {
    return associations.get( name );
  }
    
  @Override
  public IProperty copy( )
  {
      return copy( true );
  }
    
  public IProperty copy( boolean addIntrinsicProps )
  {
    LOG.debug( "copy: " + getType( ) );
    DataObject copy = null;
    try
    {
      copy = (DataObject)Class.forName( getClass().getCanonicalName() ).newInstance( );
    }
    catch (Exception e )
    {
      LOG.error( "Can't instantiate " + getClass().getCanonicalName() );
      return null;
    }
        
    copy.setName( getName( ) );
    copy.setID( this.ID );
    copy.setDataObjectSchema( this.dataSchemaName );
    copy.setIsRootObject( this.isRootObject );
        
    Iterator<IProperty> props = getProperties();
    while( props.hasNext())
    {
      IProperty thisProp = props.next();
      LOG.debug( "Ready to copy: " + thisProp.getName( ) );
      if ((thisProp instanceof IntrinsicPropertyDelegate) == false)
      {
        IProperty pCopy = null;
        if (thisProp instanceof DataObject )
        {
          pCopy = ((DataObject)thisProp).copy( addIntrinsicProps );
        }
        else pCopy = thisProp.copy( );
        copy.setProperty( pCopy, addIntrinsicProps );
      }
    }
        
    Iterator<String> assocs = getAssociationNames( );
    while (assocs.hasNext())
    {
      String name = assocs.next();
      List<IAssociation> nameAssocs = getAssociations( name );
      for ( IAssociation nameAssoc : nameAssocs )
      {
        IAssociation aCopy = nameAssoc.copy( );
        try
        {
          copy.addAssociation( aCopy );
        }
        catch (AssociationException ae )
        {
          // ERROR
        }
      }
    }
        
    LOG.debug( getType( ) + " Copy DONE." );
    return copy;
  }


  @Override
  public IAssociation getAssociation( String name, String ID )
  {
    ArrayList<IAssociation> namedAssocs = associations.get( name );
    if (namedAssocs == null) return null;
        
    for (IAssociation namedAssoc : namedAssocs )
    {
      if (namedAssoc.getID().equals( ID ))
      {
        return namedAssoc;
      }
    }
    return null;
  }

    
  @Override
  public void removeAssociations( String name )
  {
    associations.remove( name );
  }

    
  @Override
  public void removeAssociation( String name, String ID )
  {
    ArrayList<IAssociation> namedAssocs = associations.get( name );
    if (namedAssocs == null) return;
        
    int assocNdx = -1;
    for (int i = 0, isz = namedAssocs.size(); i < isz; i++)
    {
      IAssociation namedAssoc = namedAssocs.get( i );
      if (namedAssoc.getID().equals(ID ))
      {
        assocNdx = i;
        break;
      }
    }
        
    if (assocNdx >= 0)
    {
      namedAssocs.remove( assocNdx );
    }
  }
    
  public void addObject( String name, Object object )
  {
    if (objects == null) objects = new HashMap<String,Object>( );
    objects.put( name,  object );
  }
    
  public Object getObject( String name )
  {
    return (objects != null) ? objects.get( name ) : null;
  }

  @Override
  public Object getValueObject()
  {
    LOG.debug( "getValueObject( )" );
    return this;
  }

  @Override
  public Map<String, IProperty> getPropertyMap()
  {
    return propMap;
  }

  @Override
  public String getDefaultFormat( )
  {
    return IProperty.XML_FORMAT;
  }
    
  @Override
  public boolean equals( Object another )
  {
    // Implement this - use for Unit testing Assertions, etc.
    if ((another instanceof DataObject) == false)
    {
      return false;
    }
        
    DataObject anotherDob = (DataObject)another;
        
    // check name, type
    // should have all the same properties ... no more, no less
        
    return super.equals( another );
  }

    
  // ====================================================================
  //  java.util.Map Implementation
  // ====================================================================
  @Override
  public void clear()
  {
    if (propMap != null) propMap.clear( );
  }

  @Override
  public boolean containsKey( Object key )
  {
    return (propMap != null && propMap.containsKey( key ));
  }

  // This should probably operate on the set of Object Value Objects ...
  @Override
  public boolean containsValue( Object value )
  {
    Collection<Object> values = values( );
		
    return (values != null && values.contains( value ));
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Set entrySet()
  {
    return (propMap != null) ? propMap.entrySet( ) : null;
  }

  @Override
  public Object get( Object key )
  {
    LOG.debug( "get: '" + key.toString( ) + "'" );
		
    if (key.toString().equalsIgnoreCase( "name " ))
    {
      return getName( );
    }
    else if (key.toString( ).equalsIgnoreCase( "type" ))
    {
      return getType( );
    }
    else if (key.toString( ).equalsIgnoreCase( "ID"))
    {
      return getID( );
    }
    else if (key.toString().equals( "propertyList" ))
    {
      return getPropertyList( );
    }
		
    IProperty theProp = getProperty( key.toString( ) );
    return (theProp != null) ? theProp.getValueObject( ) : null;
  }

  @Override
  public boolean isEmpty()
  {
    return (propMap == null || propMap.isEmpty());
  }

  @Override
  public Set<String> keySet()
  {
    return (propMap != null) ? propMap.keySet() : null;
  }

  @Override
  public Object put( Object key, Object val )
  {
    LOG.debug( "put: " + key + " = " + val );
		
    // delegate to setProperty( )
    // if we have a DataObjectSchema get the property type from
    // the arg0 value
		
    // else create a new StringProperty
		
    if (dataSchemaName != null)
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      DataObjectSchema mySchema = (DataObjectSchema)appMan.getApplicationObject( dataSchemaName, "DataObjectSchema" );
      if (mySchema != null)
      {
        PropertyDescriptor thePropDesc = mySchema.getPropertyDescriptor( key.toString() );
        if (thePropDesc != null)
        {
          String propType = thePropDesc.getPropertyType( );
          String propFormat = thePropDesc.getPropertyFormat( );
					
          try
          {
            IProperty theProp = (IProperty)Class.forName( propType ).newInstance( );
            if (thePropDesc.isFunction( ) )
            {
              IFunctionProperty funProp = (IFunctionProperty)theProp;
              funProp.setFunction( val.toString( ));
              funProp.setPropertyHolder( this );
            }
            else
            {
              theProp.setValue( val.toString( ), propFormat );
            }
						
            setProperty( theProp );
            return theProp;
          }
          catch ( Exception e )
          {
            // should throw exception here??
          }
        }
      }
			
      return null;
    }
    else
    {
      IProperty newProperty = null;
			
      // Do some more work here - if the string is an Integer
      if (val instanceof Integer)
      {
        newProperty = new IntegerProperty( key.toString(), (Integer)val );
      }
      else if (val instanceof Double )
      {
        newProperty = new ScalarQuantity( key.toString(), (Double)val );
      }
      else if (val instanceof Date)
      {
        newProperty = new DateProperty( key.toString(), (Date)val );
      }
      else
      {
        IFunctionProperty funProp = FunctionPropertyFactory.createFunctionProperty( val.toString( ) );
        if (funProp != null)
        {
          newProperty = funProp;
          newProperty.setName( key.toString() );
        }
        else
        {
          newProperty = new StringProperty( key.toString(), val.toString( ) );
        }
      }
			
      LOG.debug( "Setting newProperty " + newProperty );
      setProperty( newProperty );
      return newProperty.getValueObject( );
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void putAll( Map inputMap )
  {
    if (inputMap != null)
    {
      @SuppressWarnings("unchecked")
      Set<Object> keys = inputMap.keySet();
      if (keys != null)
      {
        Iterator<Object> keyIt = keys.iterator( );
        while (keyIt != null && keyIt.hasNext( ) )
        {
          Object key = keyIt.next( );
          Object val = inputMap.get( key );
          if (key != null && val != null)
          {
            put( key, val );
          }
        }
      }
    }
  }

  @Override
  public Object remove( Object arg0 )
  {
    IProperty propToRemove = getProperty( arg0.toString() );
    if (propToRemove != null)
    {
      removeProperty( arg0.toString( ) );
    }
		
    return propToRemove.getValueObject( );
  }

  @Override
  public int size()
  {
    return (propMap != null) ? propMap.size( ) : 0;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Collection values()
  {
    if (propMap != null)
    {
      @SuppressWarnings("unchecked")
      HashMap<String,Object> meMap = (HashMap<String,Object>)getValueObject( );
      return meMap.values( );
    }
    
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }

}
