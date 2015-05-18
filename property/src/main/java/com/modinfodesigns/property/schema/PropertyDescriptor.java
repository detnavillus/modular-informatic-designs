package com.modinfodesigns.property.schema;

import com.modinfodesigns.app.Constants;

import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.utils.StringMethods;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a Property that a DataObject may have. A set of PropertyDescriptor(s)
 * comprise a DataObjectSchema. 
 * 
 * The PropertyDescriptor is itself a DataObject so that it may be persisted, copied,
 * etc. as any other DataObject can.
 * 
 * @author Ted Sullivan
 */

public class PropertyDescriptor extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertyDescriptor.class );

  public static final String PROPERTY_NAME = "PropertyName";
  public static final String DISPLAY_NAME  = "DisplayName";
  public static final String DESCRIPTION   = "Description";
  public static final String PROPERTY_TYPE = "PropertyType";
  public static final String PROPERTY_FORMAT = "PropertyFormat";
  public static final String PROPERTY_VALUES = "PropertyValues";
  public static final String DEFAULT_VALUE = "DefaultValue";
    
  public static final String MULTI_VALUE = "MultiValue";
  public static final String REQUIRED    = "Required";
    
  public static final String IS_DATA_OBJECT = "IsDataObject";
  public static final String DATA_SCHEMA    = "DataSchema";
    
  public static final String IS_FUNCTION    = "IsFunction";
  public static final String FUNCTION       = "Function";
    
    
  public static final String IS_INTERFACE   = "IsInterface";
    
  // Maximum length for String properties null or 0 = no maximum
  public static final String MAX_LENGTH = "MaximumLength";
    
  public static final String COMPONENTS = "Components";
    
  public static String PROP_TEMPLATE_PARAM = "propTemplate";
  public static String PROP_NAME_PARAM     = "propName";
  public static String PROP_TYPE_PARAM     = "propType";
  public static String PROP_FORMAT_PARAM   = "propFormat";
  public static String PROP_VALUES_PARAM   = "propValues";
  public static String DEFAULT_VALUE_PARAM = "defaultValue";
  public static String DISPLAY_NAME_PARAM  = "displayName";
  public static String PROP_SCHEMA_PARAM   = "propSchema";
  public static String PROP_MAPPING_PARAM  = "propMapping";
  public static String IS_FUNCTION_PARAM   = "isFunction";
  public static String FUNCTION_DESC_PARAM = "function";

  public static String PROP_REQUIRED_PARAM = "required";
  public static String MULTIVAL_PROP_PARAM = "multiValue";

  public static String DELETE_PROP = "DELETE";
    
  // ========================================================
  // Property Semantic Types
  // ========================================================
  public static final String SEMANTIC_TYPE     = "SemanticType";
  public static final String AFFILIATION_TYPE  = "AffiliationType";
  public static final String IDENTIFIER        = "Identifier";
  public static final String UNIQUE_IDENTIFIER = "UniqueIdentifier";
  public static final String CREATOR           = "Creator";
  public static final String LABEL             = "Label";
  public static final String CATEGORICAL       = "Categorical";
  public static final String DESCRIPTIVE       = "Descriptive";
  public static final String COMPONENT         = "Component";
  public static final String TEMPORAL          = "Temporal";
  public static final String SPATIAL           = "Spatial";
  public static final String AFFILIATION       = "Affiliation";

  public static final String[] SEMANTIC_TYPES = { AFFILIATION, IDENTIFIER, UNIQUE_IDENTIFIER, CREATOR, LABEL,
                                                  CATEGORICAL, DESCRIPTIVE,COMPONENT,TEMPORAL,SPATIAL};
    
  // The name of the property
  private String propertyName;
    
  private String delimiter = "|";
    
  private Map<String,String> propertyMappings;
    
  private boolean lazyEvaluation = true;
    
  public PropertyDescriptor( )
  {
    setRequired( true );
  }
    
  public PropertyDescriptor( DataObject dobj )
  {
    IProperty prop = dobj.getProperty( "name" );
    if (prop != null) propertyName = prop.getValue( );
        
    LOG.debug( "PropertyDescriptor " + propertyName );
        
    prop = dobj.getProperty( "format" );
    if (prop != null) setPropertyFormat( prop.getValue( ) );
    
    prop = dobj.getProperty( "class" );
    if (prop != null) setPropertyType( prop.getValue() );
        
    if (prop == null && dobj.getProperty( "type" ) != null)
    {
      IProperty typeProp = dobj.getProperty( "type" );
      setPropertyType( typeProp.getValue() );
    }
        
    prop = dobj.getProperty( REQUIRED );
    if (prop != null)
    {
      setRequired( prop.getValue() );
    }
        
    // get values -- should be a property list or string list property
    prop = dobj.getProperty( "values" );
    if (prop != null)
    {
      if ( prop instanceof StringListProperty )
      {
        StringListProperty slp = (StringListProperty)prop;
        setPropertyValues( slp.getStringList() );
      }
      else
      {
        String[] values = StringMethods.getStringArray( prop.getValue( ), delimiter );
        setPropertyValues( values );
      }
    }
  }
    
  public void setName( String propertyName )
  {
    this.propertyName = propertyName;
    doSetProperty( new StringProperty( PROPERTY_NAME, propertyName ));
  }
    
  public String getName(  )
  {
    return this.propertyName;
  }
    
  @Override
  protected String getXMLTagName( )
  {
    return "PropertyDescriptor";
  }
    
  public void setDisplayName( String displayName )
  {
    doSetProperty( new StringProperty( DISPLAY_NAME, displayName ));
  }
    
  public String getDisplayName(  )
  {
    IProperty dispNameProp = getProperty( DISPLAY_NAME );
    return (dispNameProp != null) ? dispNameProp.getValue( ) : this.name;
  }
    
  public void setDescription( String description )
  {
    doSetProperty( new StringProperty( DESCRIPTION, description ));
  }
    
  public String getDescription(  )
  {
    IProperty descProp = getProperty( DESCRIPTION );
    return (descProp != null) ? descProp.getValue( ) : null;
  }
    
  /**
   * Sets the Implementation type (class name) of the Property
   *
   * @param propertyType
   */
  public void setPropertyType( String propertyType )
  {
    if (propertyType.equals( DELETE_PROP ))
    {
      removeProperty( PROPERTY_TYPE );
    }
    else
    {
      doSetProperty( new StringProperty( PROPERTY_TYPE, propertyType ));
    }
  }
    
  public String getPropertyType( )
  {
    return getPropertyType( "String" );
  }
    
  public String getPropertyType( String defaultType )
  {
    String propertyType = null;
    IProperty classProp = getProperty( PROPERTY_TYPE );
    if (classProp != null)
    {
      propertyType = classProp.getValue( );
    }
    else
    {
      propertyType = defaultType;
    }
        
    if (propertyType == null) return null;
    
    LOG.debug( "getPropertyType for '" + propertyType + "'" );
        
    if ( propertyType.equalsIgnoreCase( "String" ))
    {
      return "com.modinfodesigns.property.string.StringProperty";
    }
    else if ( propertyType.equalsIgnoreCase( "Text"))
    {
      return "com.modinfodesigns.property.string.TextProperty";
    }
    else if ( propertyType.equalsIgnoreCase( "Hidden" ))
    {
      return "com.modinfodesigns.property.string.HiddenProperty";
    }
    else if (propertyType.equalsIgnoreCase( "StringList" ) || propertyType.equalsIgnoreCase( "StringListProperty" ))
    {
      return "com.modinfodesigns.property.string.StringListProperty";
    }
    else if (propertyType.equalsIgnoreCase( "Boolean" ))
    {
      return "com.modinfodesigns.property.BooleanProperty";
    }
    else if (propertyType.equalsIgnoreCase( "Binary" ))
    {
      return "com.modinfodesigns.property.BinaryProperty";
    }
    else if (propertyType.equalsIgnoreCase( "int" ) || propertyType.equalsIgnoreCase( "Integer" ) || propertyType.equalsIgnoreCase( "short"))
    {
      return "com.modinfodesigns.property.quantity.IntegerProperty";
    }
    else if (propertyType.equalsIgnoreCase( "Date" ) || propertyType.equalsIgnoreCase( "DateProperty" ) || propertyType.equalsIgnoreCase( "dateTime" ))
    {
      return "com.modinfodesigns.property.time.DateProperty";
    }
    else if (propertyType.equalsIgnoreCase( "ComputableProperty" ))
    {
      return "com.modinfodesigns.property.ComputablePropertyDelegate";
    }
    else if (propertyType.equalsIgnoreCase( "Enumeration" ) || propertyType.equalsIgnoreCase( "EnumerationProperty" ))
    {
      return "com.modinfodesigns.property.EnumerationProperty";
    }
    else if (Constants.getClassName( propertyType ) != null)
    {
      return Constants.getClassName( propertyType );
    }
    
    return propertyType;
  }
    
    
  public IProperty createProperty(  )
  {
    try
    {
      String propertyType = getPropertyType( );
      IProperty prop = (IProperty)Class.forName( propertyType ).newInstance( );
      prop.setName( getName( ) );
        
      // TODO set up other things...
      return prop;
    }
    catch ( Exception e )
    {
      return null;
    }
  }
    
  /**
   * Sets the semantic or meta type of the property. Value should be one of the SEMANTIC_TYPES
   * enumeration.
   *
   * @param semanticType
   */
  public void setSemanticType( String semanticType )
  {
    doSetProperty( new StringProperty( SEMANTIC_TYPE, semanticType ));
  }
    
  public String getSemanticType(  )
  {
    IProperty semanticProp = getProperty( SEMANTIC_TYPE );
    return (semanticProp != null) ? semanticProp.getValue() : null;
  }
    
  /**
   * Sets the target object type for an affiliation property:
   * e.g. "Undergraduate School" = "Cornell University"
   *
   * where the property "Undergraduate School" is an AFFILIATION semantic type
   * and the affiliation type is "Educational Institution"
   *
   * @param affiliationType
   */
  public void setAffiliationType( String affiliationType )
  {
    doSetProperty( new StringProperty( AFFILIATION_TYPE, affiliationType ));
  }
    
  public String getAffiliationType(  )
  {
    IProperty affiliationProp = getProperty( AFFILIATION_TYPE );
    return (affiliationProp != null) ? affiliationProp.getValue() : null;
  }
    
  public void setPropertyFormat( String propertyFormat )
  {
    LOG.debug( "setPropertyFormat: " + propertyFormat );
    doSetProperty( new StringProperty( PROPERTY_FORMAT, propertyFormat ));
  }
    
  public String getPropertyFormat(  )
  {
    IProperty formatProp = getProperty( PROPERTY_FORMAT );
    return (formatProp != null) ? formatProp.getValue() : null;
  }
    
  public void setPropertyValues( String valueList )
  {
    if (valueList != null && valueList.indexOf( delimiter) > 0)
    {
      setPropertyValues( StringMethods.getStringArray( valueList, delimiter ));
    }
  }
    
  public void setPropertyValues( String[] values )
  {
    LOG.debug( "setPropertyValues( ) " );
    	
    EnumerationProperty enumProp = new EnumerationProperty( PROPERTY_VALUES );
    
    // ===================================================================
    // create an array of the property type of this PropertyDescriptor,
    // set the values to the array of values, and add as choices to
    // the EnumerationProperty
    // ===================================================================
    // String propType = getPropertyType( );
    // NEED TO HAVE an Enumeration Type!!
        
    ArrayList<IProperty> props = new ArrayList<IProperty>( );
    for (int i = 0, isz = values.length; i < isz; i++)
    {
      try
      {
        // IProperty prop = (IProperty)Class.forName( propType ).newInstance( );
        LOG.debug( "Adding value: " + values[i] );
            	
        StringProperty prop = new StringProperty( values[i], values[i] );
        //prop.setName( values[i] );
        //prop.setValue( values[i], getPropertyFormat( ));
        props.add( prop );
      }
      catch( Exception e )
      {
        // drat!
      }
    }
        
    enumProp.setChoices( props );
    doSetProperty( enumProp );
  }
    
  /**
   * Returns a list of property values (PropertyList) that are the
   * valid values for this property or NULL if there are no choices set.
   *
   * @return
   */
  public PropertyList getPropertyValues(  )
  {
    EnumerationProperty enumProp = (EnumerationProperty)getProperty( PROPERTY_VALUES );
    return (enumProp != null) ? enumProp.getChoiceList( ) : null;
  }
    

  public void setValue( String value )
  {
    LOG.debug( "setValue: " + value );
    setDefaultValue( value );
  }
    
  public void setDefaultValue( String defaultValue )
  {
    doSetProperty( new StringProperty( DEFAULT_VALUE, defaultValue ));
  }
    
  public String getDefaultValue(  )
  {
    IProperty defValProp = getProperty( DEFAULT_VALUE );
    return (defValProp != null) ? defValProp.getValue() : "";
  }
    
  public void setRequired( boolean required )
  {
    doSetProperty( new BooleanProperty( REQUIRED, required ));
  }
    
  public void setRequired( String required )
  {
    doSetProperty( new BooleanProperty( REQUIRED, required ));
  }
    
  public boolean isRequired(  )
  {
    LOG.debug( "isRequired: " + getProperty( REQUIRED ));
    	
    BooleanProperty requiredProp = (BooleanProperty)getProperty( REQUIRED );
    return (requiredProp != null && requiredProp.getValue().equals( "true" ) );
  }
    
  public void setMultiValue( boolean multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ));
  }
    
  public void setMultiValue( String multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ));
  }
    
  public boolean isMultiValue( )
  {
    BooleanProperty multiValProp = (BooleanProperty)getProperty( MULTI_VALUE );
    return (multiValProp != null && multiValProp.getValue().equals( "true" ) );
  }
    
  public void setMaximumLength( int maximumLength )
  {
    doSetProperty( new IntegerProperty( MAX_LENGTH, maximumLength ));
  }

  public void setMaximumLength( String maximumLength )
  {
    doSetProperty( new IntegerProperty( MAX_LENGTH, maximumLength ));
  }
    
  public int getMaximumLength( )
  {
    IntegerProperty maxLenProp = (IntegerProperty)getProperty( MAX_LENGTH );
    return (maxLenProp != null) ? maxLenProp.getIntegerValue( ) : 0;
  }
    
  // =======================================================================
  // Is this property a nested data object?
  // =======================================================================
  public void setIsDataObject( String isDataObject )
  {
    LOG.debug( "setIsDataObject( " + isDataObject + " )" );
    doSetProperty( new BooleanProperty( IS_DATA_OBJECT, isDataObject ));
    if (isDataObject.equals( "true" )) setPropertyFormat( DataObject.NAME_ID );
  }
    
  public void setIsDataObject( boolean isDataObject )
  {
    LOG.debug( "setIsDataObject( " + isDataObject + " )" );
    doSetProperty( new BooleanProperty( IS_DATA_OBJECT, isDataObject ));
    if (isDataObject) setPropertyFormat( DataObject.NAME_ID );
  }
    
  public boolean isDataObject(  )
  {
    BooleanProperty dataObProp = (BooleanProperty)getProperty( IS_DATA_OBJECT );
    return (dataObProp != null && dataObProp.getValue().equals( "true" ) );
  }
    
  public void setDataObjectSchema( String dataObjectSchema )
  {
    LOG.debug( "setDataObjectSchema( '" + dataObjectSchema + "' )" );
    if (dataObjectSchema != null && dataObjectSchema.trim().length() > 0)
    {
      doSetProperty( new StringProperty( DATA_SCHEMA, dataObjectSchema ));
      setIsDataObject( true );
    }
  }
    
  public String getDataObjectSchema( )
  {
    StringProperty dataSchemaProp = (StringProperty)getProperty( DATA_SCHEMA );
    return (dataSchemaProp != null) ? dataSchemaProp.getValue( ) : null;
  }
    
  // =======================================================================
  // Is this property a Function property?
  // =======================================================================
  public void setIsFunction( String isFunction )
  {
    doSetProperty( new BooleanProperty( IS_FUNCTION, isFunction ));
  }
    
  public void setIsFunction( boolean isFunction )
  {
    doSetProperty( new BooleanProperty( IS_FUNCTION, isFunction ));
  }
    
  public boolean isFunction(  )
  {
    BooleanProperty functProp = (BooleanProperty)getProperty( IS_FUNCTION );
    return (functProp != null && functProp.getValue().equals( "true" ) );
  }
    
  public void setFunctionDescription( String functionDescription )
  {
    doSetProperty( new StringProperty( FUNCTION, functionDescription ));
  }
    
  public String getFunctionDescription( )
  {
    StringProperty functProp = (StringProperty)getProperty( FUNCTION );
    return (functProp != null) ? functProp.getValue( ) : null;
  }
    
  public void setIsInterface( String isInterface )
  {
    doSetProperty( new BooleanProperty( IS_INTERFACE, isInterface ));
  }
    
  public void setIsInterface( boolean isInterface )
  {
    doSetProperty( new BooleanProperty( IS_INTERFACE, isInterface ));
  }

  public boolean isInterface(  )
  {
    BooleanProperty interProp = (BooleanProperty)getProperty( IS_INTERFACE );
    return (interProp != null && interProp.getValue().equals( "true" ) );
  }
    

  public void setLazyEvaluation( boolean lazyEvaluation )
  {
    this.lazyEvaluation = lazyEvaluation;
  }
    
  public boolean isLazilyEvaluated( )
  {
    return this.lazyEvaluation;
  }
    
    
  @Override
  public boolean equals( Object another )
  {
    if ((another instanceof PropertyDescriptor) == false)
    {
      return false;
    }
    	
    PropertyDescriptor anotherDescriptor = (PropertyDescriptor)another;
    if (anotherDescriptor.getName().equals( getName( ) ) == false )
    {
      return false;
    }
    	
    if (anotherDescriptor.getPropertyType( ).equals( getPropertyType( ) ) == false )
    {
      return false;
    }
    	
    // etc ...
    	
    return true;
  }
    
  public void createComponentProperty( IProperty component )
  {
    LOG.debug( "createComponentProperty " );
    	
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( component.getName( ) );
    pd.setPropertyType( component.getType( ) );
    pd.setDefaultValue( component.getValue( ) );
    	
    if (component instanceof EnumerationProperty )
    {
      EnumerationProperty enumProp = (EnumerationProperty)component;
      pd.setPropertyValues( enumProp.getValues( component.getDefaultFormat() ));
    }
    	
    addComponentProperty( pd );
  }
    
    
  public void addComponentProperty( PropertyDescriptor component )
  {
    LOG.debug( this + " addComponentProperty: " + component + " " + component.getName( ) + " = " + component.getDefaultValue( ) );
    	
    PropertyList componentList = (PropertyList)getProperty( COMPONENTS );
    if (componentList == null)
    {
      LOG.debug( "Creating Components PropertyList" );
      componentList = new PropertyList( COMPONENTS );
      doSetProperty( componentList );
    }
    	
    componentList.addProperty( component );
		
    LOG.debug( this + "Have components \n" + getProperty( COMPONENTS ) );
  }
    
  public void setComponents( List<PropertyDescriptor> components )
  {
    LOG.debug( "setComponents " + components );
    	
    if (components != null && components.size() > 0)
    {
      PropertyList componentList = new PropertyList( COMPONENTS );
      doSetProperty( componentList );
    	
      for (PropertyDescriptor component : components )
      {
        LOG.debug( "Adding component: " + component.getName( ) );
        componentList.addProperty( component );
      }
    }
  }
    
  public List<PropertyDescriptor> getComponents(  )
  {
    return getComponents( true );
  }
    
  public List<PropertyDescriptor> getComponents( boolean allComponents )
  {
    LOG.debug( this + " getComponents( )" );
    	
    PropertyList componentList = (PropertyList)getProperty( COMPONENTS );
    if (componentList != null)
    {
      LOG.debug( "Have Components..." );
    		
      ArrayList<PropertyDescriptor> compList = new ArrayList<PropertyDescriptor>( );
    		
      Iterator<IProperty> propIt = componentList.getProperties( );
      while( propIt != null && propIt.hasNext( ) )
      {
        PropertyDescriptor pd = (PropertyDescriptor)propIt.next( );
        LOG.debug( "Adding component: " + pd.getName( ) );
    			
        IProperty itsProp = getProperty( pd.getName( ) );
        if (itsProp != null)
        {
          LOG.debug( "Have set prop: " + itsProp.getValue( ) );
          pd.setDefaultValue( itsProp.getValue( ) );
        }
    			
        compList.add( pd );
      }
    		
      return compList;
    }
    else if ( allComponents )
    {
      // create an instance of the propery and set any components that it has ...
      String propClass = getPropertyType( );
      LOG.debug( "Creating component list from type " + propClass );
      try
      {
        IProperty prop = (IProperty)Class.forName( propClass ).newInstance( );
        if (prop instanceof IExposeInternalProperties)
        {
          IExposeInternalProperties intPropsProp = (IExposeInternalProperties)prop;
          List<PropertyDescriptor> internalList = intPropsProp.getInternalProperties( );
          if (internalList != null && internalList.size( ) > 0)
          {
            LOG.debug( "Got internalList with " + internalList.size( ) );
            setComponents( internalList );
            return internalList;
          }
        }
      }
      catch ( Exception e )
      {
        LOG.debug( "getComponents got Exception: " + e );
      }
    }
    	
    return null;
  }
    
  public void setInternalProperties( IProperty property )
  {
    LOG.debug( "setInternalProperties: " + getName( ) + " " + property );
    	
    List<PropertyDescriptor> components = getComponents(  );
    if (components != null)
    {
      LOG.debug( "Got components ..." );
      // iterate through the components, if PropertyDescriptor has the property value
      // for the component's property, call the set[componentName] method on the IProperty ...
      for (PropertyDescriptor component : components )
      {
        LOG.debug( "Got component: " + component.getName( ));
    			
        IProperty pdProp = getProperty( component.getName( ) );
        String propValue = (pdProp != null) ? pdProp.getValue( ) : component.getDefaultValue( );
          
        if (propValue != null)
        {
          LOG.debug( "setting " + component.getName() + " property to: " + propValue );
    				
          try
          {
            String methodName = "set" + StringMethods.initialCaps( component.getName( ) );
            LOG.debug( "invoking method: " + methodName );
            Object[] params = new Object[1];
            params[0] = propValue;
    				    
            Method m = null;
            @SuppressWarnings("rawtypes")
            Class[] paramArray = new Class[1];
            paramArray[0] = propValue.getClass( );
            LOG.debug( "propValue is class " + paramArray[0].getCanonicalName( ) );
            m = property.getClass().getMethod( methodName, paramArray );

            if (m != null)
            {
              LOG.debug( "Invoking Method..." );
              m.invoke( property, params );
            }
          }
          catch (NoSuchMethodException nme )
          {
            LOG.debug( "NoSuchMethodException: " + nme.getMessage( ) );
          }
          catch ( Exception e )
          {
            LOG.debug( "Got Exception: " + e );
          }
        }
      }
    }
  }
    
  public void addPropertyMapping( String from, String to )
  {
    if (propertyMappings == null) propertyMappings = new HashMap<String,String>( );
    propertyMappings.put( from, to );
  }
    
  public void setPropertyMappings( Map<String,String> propertyMappings )
  {
    this.propertyMappings = propertyMappings;
  }
    
  public Map<String,String> getPropertyMappings(  )
  {
    return propertyMappings;
  }
    
  // TO DO - check class - provide data type
  public String getDataType(  )
  {
    String propertyClass = getPropertyType( );
    try
    {
      IProperty prop = (IProperty)Class.forName( propertyClass ).newInstance( );
      if (prop instanceof IQuantity)
      {
        return "Double";
      }
      else if (prop instanceof IntegerProperty)
      {
        return "Integer";
      }
    }
    catch ( Exception e )
    {
        
    }
        
    return "String";
  }
    
  public List<String> getPropertyFormats(  )
  {
    String type = getPropertyType( );
		
    ArrayList<String> formats = new ArrayList<String>( );
    IProperty theProp = null;
    try
    {
      theProp = (IProperty)Class.forName( type ).newInstance( );
    }
    catch ( Exception e )
    {
        	
    }
        
    if (theProp != null)
    {
      if (theProp instanceof IQuantity)
      {
        IQuantity theQuan = (IQuantity)theProp;
        String[] units = theQuan.getUnits( );
        for (int i = 0, usz = units.length; i < usz; i++)
        {
          formats.add( units[i] );
        }
      }
      else
      {
        if (theProp.getDefaultFormat( ) != null)
        {
          formats.add( theProp.getDefaultFormat( ) );
        }
      }
    }
        
    return formats;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    return null;
  }
	
	
  // ==================================================================================
  //   Map methods
  // ==================================================================================
	

}
