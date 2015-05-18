package com.modinfodesigns.property.schema.soap;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

import com.modinfodesigns.utils.DOMMethods;

import com.modinfodesigns.network.http.HttpClientWrapper;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOAPDataObjectManager
{
  private transient static final Logger Log = LoggerFactory.getLogger( SOAPDataObjectManager.class );
    
  private HashMap<String,DataObject> schemaMap = new HashMap<String,DataObject>( );
    
  private String serviceAddress;
  private String namespaceURI;
  private String namespacePrefix = "ns1";

  public SOAPDataObjectManager( String wsdlString, String serviceName, String servicePort ) throws Exception
  {
    // Create a set of DataObjectSchema objects from xsdSchema
    Document soapDoc = DOMMethods.getDocument( new StringReader( wsdlString ), "UTF-8", true );
    initialize( soapDoc, serviceName, servicePort );
  }
    
  private void initialize( Document soapDoc, String serviceName, String servicePort ) throws Exception
  {
    Log.info( "initialize ...." );
    if (soapDoc == null)
    {
      throw new Exception( "WSDL document is NULL!" );
    }
    Element docEl = soapDoc.getDocumentElement( );
    Log.debug( docEl.getTagName( ) );
      
    NodeList schemaEls = docEl.getElementsByTagNameNS( "*", "schema" );

    if (schemaEls.getLength() == 0)
    {
      throw new Exception( "No Schema element found" );
    }
      
    Element schemaEl = (Element)schemaEls.item( 0 );
    NodeList childNodes = schemaEl.getChildNodes( );
    for (int i = 0; i < childNodes.getLength(); i++)
    {
      Node n = childNodes.item( i );
      if (n instanceof Element) {
        Element ne = (Element)n;
        String tagName = ne.getTagName( );
        if (tagName.indexOf( ":" ) > 0)
        {
          tagName = new String( tagName.substring( tagName.indexOf( ":" ) + 1 ));
        }
        
        Log.debug( "got tagName: " + tagName );
        if (tagName.equals( "element" ) || tagName.equals( "complexType" ))
        {
          // if it has a type - don't add it here
          String type = DOMMethods.getAttribute( ne, "type" );
          if (type == null || type.startsWith( "tns:") == false )
          {
            // if element tagname == element of complexType
            DataObjectSchema dobjSchema = createDataObjectSchema( ne );
            if (dobjSchema.getName( ) != null)
            {
              Log.debug( "adding Schema " + dobjSchema.getName( ) + " == " + dobjSchema );
              schemaMap.put( dobjSchema.getName( ), dobjSchema );
            }
          }
        }
        else if (tagName.equals( "simpleType" ))
        {
          PropertyDescriptor simpleProp = createPropertyDescriptor( schemaEl );
          schemaMap.put( simpleProp.getName(), simpleProp );
        }
      }
    }
      
    // Get the Service Address:
    this.serviceAddress = getServiceAddress( soapDoc.getDocumentElement( ), serviceName, servicePort );
    this.namespaceURI = getNamespaceURI( soapDoc.getDocumentElement( ) );
    Log.debug( "got namespaceURI: " + namespaceURI );
  }
    
  public DataObjectSchema getDataObjectSchema( String name )
  {
    DataObject schemaOb = schemaMap.get( name );
    if (schemaOb != null && schemaOb instanceof DataObjectSchema)
    {
      return (DataObjectSchema)schemaOb;
    }
      
    return null;
  }
    
  private String getServiceAddress( Element soapDoc, String serviceName, String servicePort ) throws Exception
  {
    NodeList serviceNodes = soapDoc.getElementsByTagNameNS( "*", "service" );
    if (serviceNodes != null && serviceNodes.getLength() > 0)
    {
      for (int i = 0; i < serviceNodes.getLength(); i++)
      {
        Element serviceEl = (Element)serviceNodes.item( i );
        Attr name = (Attr)serviceEl.getAttributes().getNamedItem( "name" );
        if (name.getValue( ).equals( serviceName ))
        {
          NodeList portNodes = serviceEl.getElementsByTagNameNS( "*", "port" );
          if (portNodes != null && portNodes.getLength() > 0)
          {
            for (int j = 0; j < portNodes.getLength(); j++)
            {
              Element portEl = (Element)portNodes.item( j );
              Attr port = (Attr)portEl.getAttributes().getNamedItem( "name" );
              if (port.getValue( ).equals( servicePort ))
              {
                NodeList addressList = portEl.getElementsByTagNameNS( "*", "address" );
                if (addressList != null && addressList.getLength() > 0)
                {
                  Element addressEl = (Element)addressList.item( 0 );
                  Attr location = (Attr)addressEl.getAttributes().getNamedItem( "location" );
                  if (location != null)
                  {
                    return location.getValue( );
                  }
                }
              }
            }
          }
        }
      }
        
      throw new Exception( "Could not get Service Address for " + serviceName + " " + servicePort );
    }
    else
    {
        throw new Exception( "No Service Nodes Defined!" );
    }
    
  }
    
  private String getNamespaceURI( Element soapElem )
  {
      return DOMMethods.getAttribute( soapElem, "xmlns:tns" );
  }
    
  /* Enumeration types */
  private PropertyDescriptor createPropertyDescriptor( Element schemaEl )
  {
    String name = DOMMethods.getAttribute( schemaEl, "name" );
    Log.info( "createPropertyDescriptor: " + name );
      
    NodeList enumLst = schemaEl.getElementsByTagName( "enumeration" );
    if (enumLst != null && enumLst.getLength() > 0)
    {
      PropertyDescriptor enumDesc = new PropertyDescriptor( );
      enumDesc.setName( name );
      enumDesc.setPropertyType( enumDesc.getPropertyType( "Enumeration" ));
      ArrayList<String> propValues = new ArrayList<String>( );
      for (int i = 0; i < enumLst.getLength(); i++)
      {
        Element enumEl = (Element)enumLst.item( i );
        String value = DOMMethods.getAttribute( enumEl, "value" );
        propValues.add( value );
      }
      String[] valList = new String[ propValues.size() ];
      valList = propValues.toArray( valList ); // ???
      enumDesc.setPropertyValues( valList );
      return enumDesc;
    }
    else
    {
      return null;  /// something else???
    }
  }

   
  private DataObjectSchema createDataObjectSchema( Element schemaEl )
  {
    Log.debug( DOMMethods.getXML( schemaEl, true ) );
      
    DataObjectSchema dobjSchema = new DataObjectSchema( );
    String name = DOMMethods.getAttribute( schemaEl, "name" );
    if (name != null)
    {
      dobjSchema.setName( name );
    }
      
    Log.debug( "createDataObjectSchema: " + name );
    // get 'element' elements
    // get if element name starts with "tns:"
    NodeList elemLst = schemaEl.getElementsByTagNameNS( "*", "element" );
    if (elemLst != null && elemLst.getLength() > 0) {
      for (int i = 0; i < elemLst.getLength(); i++) {
        Element el = (Element)elemLst.item( i );
        addProperty( dobjSchema, el );
      }
    }
      
    return dobjSchema;
  }
    
  private void addProperty( DataObjectSchema dobjSchema, Element propEl )
  {
    Log.debug( "addProperty: " + DOMMethods.getXML( propEl, true ) + " to " + dobjSchema );
      
    // get attributes
    // get type attribute - if namespace uri is "tns" - make this a DataSchemaObject with this name ...
    NamedNodeMap attrMap = propEl.getAttributes();
    Attr type = (Attr)attrMap.getNamedItem( "type" );
    String typeVal = type.getValue( );
    Log.debug( "got type value " + typeVal );
    Attr name = (Attr)attrMap.getNamedItem( "name" );
    Log.debug( "got name = " + name.getValue() );
    String namespacePrefix = (typeVal.indexOf( ":" ) > 0) ? new String( typeVal.substring( 0, typeVal.indexOf( ":" ))) : null;
    if (namespacePrefix != null && namespacePrefix.equals( "tns" )) {
      String parentName = new String( typeVal.substring( typeVal.indexOf( ":" ) + 1 ));
      Log.debug( name.getValue( ) + " creating child schema from " + parentName );

      // Create a DataObjectSchema with parentName == type name
      DataObjectSchema childSchema = new DataObjectSchema( );
      childSchema.setName( name.getValue( ) );
      childSchema.setParentSchema( parentName );
        
      Log.debug( dobjSchema.getName( ) + " adding nested schema " + childSchema.getName( ) );
      dobjSchema.addChildSchema( childSchema );
    }
    else
    {
      // create a PropertyDescriptor from the attrMap
      PropertyDescriptor propDescriptor = createPropertyDescriptor( attrMap );
      dobjSchema.addPropertyDescriptor( propDescriptor );
    }
  }
    
  // minOccurs="0" maxOccurs="1" name="RequestHost" type="xs:string"
  private PropertyDescriptor createPropertyDescriptor( NamedNodeMap attributeMap )
  {
    PropertyDescriptor propDesc = new PropertyDescriptor( );
    Attr nameAttr = (Attr)attributeMap.getNamedItem( "name" );
    propDesc.setName( nameAttr.getValue( ) );
      
    Log.debug( "createPropertyDescriptor: " + nameAttr.getValue() );
      
    Attr typeAttr = (Attr)attributeMap.getNamedItem( "type" );
    String type = typeAttr.getValue( );
    type = (type.indexOf( ":" ) > 0 ) ? new String( type.substring( type.indexOf( ":" ) + 1)) : type;
    Log.debug( "creating property of type " + type );
    propDesc.setPropertyType( propDesc.getPropertyType( type ));
      
    // get min occurs, max occurs
    // if minoccurs == 1 and maxoccurs == 1: set required = true
    // if maxoccurs == unbounded - set list type
    Attr minoccursAt = (Attr)attributeMap.getNamedItem( "minOccurs" );
    String minoccurs = (minoccursAt != null) ? minoccursAt.getValue( ) : "0";
    Attr maxoccursAt = (Attr)attributeMap.getNamedItem( "maxOccurs" );
    String maxoccurs = (maxoccursAt != null) ? maxoccursAt.getValue( ) : "1";
      
    if (minoccurs.equals( "1" ) && maxoccurs.equals( "1" ))
    {
      Log.debug( propDesc.getName( ) + " is required" );
      propDesc.setRequired( true );
    }
      
    if (maxoccurs.equals( "unbounded" ))
    {
      Log.debug( propDesc.getName( ) + " is a List" );
      propDesc.setMultiValue( true );
    }
      
    return propDesc;
  }
    
  public String getServiceAddress( )
  {
    return this.serviceAddress;
  }
    
  // pass a map of name, value pairs
  public DataObject createDataObject( String objectType, Map<String,String> values ) throws PropertyValidationException
  {
    Log.debug( "createDataObject: " + objectType );
    DataObject schema = schemaMap.get( objectType );
    if (schema == null)
    {
      return null;
    }
    if (schema instanceof DataObjectSchema )
    {
      return createDataObject( (DataObjectSchema)schema, values );
    }
    else if (schema instanceof PropertyDescriptor )
    {
      return null;
    }
      
    return null;
  }
    
  public DataObject createDataObject( DataObjectSchema schema, Map<String,String> values ) throws PropertyValidationException
  {
    Log.debug( "createDataObject from value map " + schema.getName( ) );
    DataObject dobj = new DataObject( );
    dobj.setName( schema.getName( ) );
      
    String parentSchemaName = schema.getParentSchema( );
    if (parentSchemaName != null)
    {
      DataObject parentSchema = schemaMap.get( parentSchemaName );
      if (parentSchema != null)
      {
        Log.info( "Adding properties from parent schema " + parentSchemaName );
        if (parentSchema instanceof PropertyDescriptor )
        {
            // add this property to the dobj
            IProperty theProp = ((PropertyDescriptor)parentSchema).createProperty();
            dobj.setProperty( theProp, false );
        }
        else
        {
          addProperties( (DataObjectSchema)parentSchema, dobj, false );
        }
      }
    }
      
    addProperties( schema, dobj, true );
      
    if (values != null)
    {
      for (String field : values.keySet( ) )
      {
        Log.debug( "setting " + field + " = " + values.get( field ) );
        IProperty prop = dobj.getProperty( field );
        if (prop != null)
        {
          prop.setValue( values.get( field ), null );
        }
        else
        {
            System.out.println( "No prop: " + field );
        }
      }
    }
    
    Log.info( "Created DataObject: " + dobj.toString( ) );
    return dobj;
  }
    
    
  private void addProperties( DataObjectSchema schema, DataObject dobj, boolean override ) throws PropertyValidationException
  {
    Log.debug( dobj.getName( ) + " addProperties from " + schema.getName( ) );
    Iterator<String> propertyNames = schema.getPropertyNames( );
    while ( propertyNames != null && propertyNames.hasNext( ) )
    {
      String propName = propertyNames.next( );
      Log.debug( dobj.getName( ) + " addProperty: " + propName );
      PropertyDescriptor propDesc = schema.getPropertyDescriptor( propName );
        
      IProperty theProp = propDesc.createProperty();
      if (override)
      {
        dobj.setProperty( theProp, false );
      }
      else
      {
        dobj.addProperty( theProp, false );
      }
    }
      
    List<DataObjectSchema> childSchemas = schema.getChildSchemas( );
    if (childSchemas != null)
    {
      Log.debug( "Adding child DataObjects " );
      for (DataObjectSchema childSchema : childSchemas )
      {
        DataObject childObj = null;
        if (childSchema.getParentSchema( ) != null )
        {
          childObj = new DataObject( );
          childObj.setName( childSchema.getName( ) );
          DataObjectSchema parentSchema = (DataObjectSchema)schemaMap.get( childSchema.getParentSchema( ) );
          addProperties( parentSchema, childObj, true );
        }
        else
        {
          childObj = createDataObject( childSchema, null );
        }
          
        if (dobj instanceof DataList )
        {
          ((DataList)dobj).addDataObject( childObj );
        }
        else
        {
          dobj.addProperty( childObj, false );
        }
      }
    }
  }
    
  public String createSOAPRequest( String messageType, Map<String,String> inputValues ) throws Exception
  {
    Log.info( "createSOAPRequest for " + messageType );
    // get the request object type
    // get the DataObject for the request type
    // render it for SOAP
    DataObject dobj = createDataObject( messageType, inputValues );
    DataObjectSchema dobjSchema = (DataObjectSchema)schemaMap.get( messageType );
    return createSOAPRequest( dobj, dobjSchema );
  }
    
  public String createSOAPRequest( DataObject dobj, DataObjectSchema dobjSchema ) throws Exception
  {
    // build the XML from the dobj.  Check schema for required,
    StringBuilder stb = new StringBuilder( );
    stb.append( "<" ).append( namespacePrefix ).append( ":" ).append( dobj.getName( ) ).append( " xmlns:" ).append( namespacePrefix ).append( "=\"" ).append( namespaceURI ).append( "\">" );
    Iterator<IProperty> properties = dobj.getProperties( );
    while (properties != null && properties.hasNext( ) )
    {
      IProperty prop = properties.next( );
      if (prop instanceof DataObject )
      {
        stb.append( createSOAPRequest( (DataObject)prop, dobjSchema ));
      }
      else
      {
        // TO DO Check if is required
        String propValue = prop.getValue( );
        if (propValue != null)
        {
          stb.append( "<" ).append(namespacePrefix).append( ":" ).append( prop.getName( ) ).append( ">" )
             .append( propValue ).append( "</" ).append( namespacePrefix).append( ":" ).append( prop.getName( ) ).append( ">" );
        }
        else
        {
          stb.append( "<" ).append( namespacePrefix ).append( ":" ).append( prop.getName( ) ).append( "/>" );
        }
      }
    }
      
    stb.append( "</" ).append( namespacePrefix ).append( ":" ).append( dobj.getName( ) ).append( ">" );
    Log.info( "createSOAPRequest returning:\n" + stb.toString( ) );
    return stb.toString( );
  }
    
  public DataObject createDataObject( String soapEnvelope ) throws PropertyValidationException
  {
    Log.debug( "createDataObject from:\n" + soapEnvelope );
    // create a DOM Document from the soapEnvelope
    Document doc = DOMMethods.getDocument( new StringReader( soapEnvelope ), "UTF-8", true );
    if (doc == null)
    {
      throw new PropertyValidationException( "XML is not well formed!" );
    }
      
    NodeList bodyLst = doc.getElementsByTagNameNS( "*", "Body" );
    if (bodyLst == null)
    {
      throw new PropertyValidationException( "body is missing!" );
    }
      
    Element bodyEl = (Element)bodyLst.item( 0 );
    NodeList childNodes = bodyEl.getChildNodes( );
    ArrayList<DataObject> dataObjs = new ArrayList<DataObject>( );
    for (int i = 0; i < childNodes.getLength(); i++ )
    {
      Node n = childNodes.item( i );
      if (n instanceof Element )
      {
        Element dataEl = (Element)n;
        dataObjs.add( createDataObject( dataEl ));
      }
    }
      
    if (dataObjs.size() == 1)
    {
      Log.info( "returning:\n" + dataObjs.get( 0 ).toString( ) );
      return dataObjs.get( 0 );
    }
    else if (dataObjs.size() > 1)
    {
      DataList dl = new DataList( );
      for (DataObject dobj : dataObjs )
      {
        dl.addDataObject( dobj );
      }
        
      Log.info( "returning list:\n" + dl.toString( ));
      return dl;
    }
    else
    {
      return null;
    }
  }
    
  private DataObject createDataObject( Element dataEl ) throws PropertyValidationException
  {
    // get the name of the data object schema from the body tag name
    // create the DataObject
    String tagName = dataEl.getTagName( );
    Log.debug( "createDataObject from Element " + tagName );
    DataObject dobj = createDataObject( tagName, (Map<String,String>)null );
    if (dobj != null) initializeDataObject( dobj, dataEl );
    return dobj;
  }
    
  private void initializeDataObject( DataObject dobj, Element dataEl ) throws PropertyValidationException
  {
    Log.debug( "initializeDataObject from Element " + dataEl.getTagName( ) );
    // for each property name, get the property tag
    Iterator<String> propNames = dobj.getPropertyNames( );
    if ( propNames != null )
    {
      ArrayList<IProperty> listProps = null;
      while ( propNames.hasNext( ) )
      {
        String propName = propNames.next( );
        IProperty prop = dobj.getProperty( propName );
        Log.debug( "Initializing property: " + propName + " " + prop.getType( ) );
        NodeList propEls = dataEl.getElementsByTagName( propName );
        if (propEls != null)
        {
          if (propEls.getLength() == 1 )
          {
            Element pe = (Element)propEls.item( 0 );
            initializeProperty( prop, pe );
          }
          else
          {
            PropertyList pl = new PropertyList( );
            pl.setName( propName );
              
            // create a PropertyList for this property,
            // replace property with PL, fill the list
            for (int i = 0; i < propEls.getLength(); i++ )
            {
              Element pe = (Element)propEls.item( i );
                
              IProperty copy = null;
              if (prop instanceof DataObject)
              {
                copy = ((DataObject)prop).copy( false );
              }
              else
              {
                copy = prop.copy( );
              }
            
              initializeProperty( copy, pe );
              pl.addProperty( copy );
            }
            if (listProps == null)
            {
              listProps = new ArrayList<IProperty>( );
            }
            listProps.add( pl );
          }
        }
        else
        {
          System.out.println( propName + " has no elements!" );
        }
      }
      
      if (listProps != null)
      {
        for ( IProperty listProp : listProps )
        {
          dobj.setProperty( listProp, false );
        }
      }
    }
    Log.debug( "Initialized:\n" + dobj.getValue( ) );
  }
    
  private void initializeProperty( IProperty prop, Element pe ) throws PropertyValidationException
  {
    if (prop instanceof DataObject)
    {
      initializeDataObject( (DataObject)prop, pe );
    }
    else
    {
      String propVal = DOMMethods.getText( pe );
      Log.debug( "setting property " + prop.getName() + " = " + propVal );
      prop.setValue( propVal, null );
    }
  }
    
  public DataObject executeSOAPRequest( String soapMethod, Map<String,String> values ) throws Exception
  {
    String soapRequest = createSOAPRequest( soapMethod, values );
        
    String submitRequest = soapHeader + soapRequest + soapTrailer;
    Log.debug( submitRequest );
      
    String soapResponse = HttpClientWrapper.executePost( this.serviceAddress, submitRequest, "text/xml" );
    Log.debug( soapResponse );
    return createDataObject( soapResponse );
  }
    
  // create a SOAPBody from a DataObject
  public String renderDataObject( DataObject dobj ) throws Exception
  {
    DataObjectSchema dobjSchema = (DataObjectSchema)schemaMap.get( dobj.getName( ) );
    return createSOAPRequest( dobj, dobjSchema );
  }
    
  private static final String soapHeader = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                                         + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                                         + "<soap:Body>";
  private static final String soapTrailer= "</soap:Body></soap:Envelope>";
}