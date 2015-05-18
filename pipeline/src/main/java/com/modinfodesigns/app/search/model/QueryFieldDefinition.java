package com.modinfodesigns.app.search.model;

import com.modinfodesigns.pipeline.source.IPropertyListSource;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IRangeProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.search.FinderField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes an abstract or source-agnostic query field. The field definition is used by the
 * Presentation layer to construct the necessary User Interface elements and by 
 * the Controller layer (QueryFormatter) to create a QueryField that contains the users input value(s). 
 * The QueryField is then transformed by an IFinder implementation to translate the request 
 * information into an executable structured query.
 * 
 * Some of the properties are derived from the field properties of the source (name, data type, single|multiple value, etc.)
 * other properties are based on application | user interface requirements (single|multiple selection, numeric range)
 * 
 * For advanced queries, each query field can have an associated QueryModeSchema with one or more
 * allowed Query Modes (such as AND, OR, NOT, NEAR, PREFIX, BOOST, etc.).
 * 
 * For Range Queries, an Associated QueryFieldDefinition can be added using an IAssociation link. The Source 
 * of the association will be interpreted as the Range Minimum (for the purposes of the schema) and 
 * the Target of the association as the Range Maximum - for the purposes of formulating a QueryField that contains
 * the user's input for one of both of these fields.  The Source (Range Minimum) QueryFieldDefinition will be part of the QuerySchema and
 * the Target (Range Maximum) will be derived from the Source.
 * 
 * @author Ted Sullivan
 */

// To Do: This class should contain information on formats - specifically Date Format that it expects.
// Useful for renderers such as JQueryFieldRenderer - AND for QueryFormatter...

// Also for range property - JQuery slider may need to get the step value -
// these misc. properties could be added as basic properties and retrieved through naming convention
// semantics.

public class QueryFieldDefinition extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryFieldDefinition.class );
    
  // Properties
  //   Name
  //   Display Name
  //   Description
  //   Data Type
  //   Required|Optional - used for validation - how to render this ...
  //   Single|Multiple Selection
  //   Single|Multiple Value
  //   Numeric Range
  //   Default Value
  //   Text length expected - What type of text values are expected? Will they
  //                          be easily entered using an Input Text or a TextArea?
  //
  //   init from property ( Request | None | Session (?) )
	
  // Field Operator - QueryMode Field - QueryModeSchema
	
  public static final String NAME            = "Name";
  public static final String DISPLAY_NAME    = "DisplayName";
  public static final String DATA_TYPE       = "DataType";
  public static final String REQUIRED        = "Required";
  public static final String MULTI_SELECT    = "MultiSelect";
  public static final String MULTI_VALUE     = "MultiValue";
  public static final String RANGE           = "Range";
  public static final String DESCRIPTION     = "Description";
  public static final String CHOICES         = "ChoiceList";
  public static final String INITIALIZE_FROM = "InitializeFrom";

  public static final String STRING_TYPE = "String";
  public static final String INTEGER_TYPE = "Integer";
  public static final String BOOLEAN_TYPE = "Boolean";
  public static final String TEXT_TYPE = "Text";
  public static final String TEXT_AREA_ROWS = "TextAreaRows";
	
  public static final String TEXT_AREA_COLS = "TextAreaCols";
	
  public static final String QUERY_MODE_FIELD = "QueryModeField";
  public static final String RENDERER_NAME    = "RendererName";
	
  public static final String REQUEST = "REQUEST";
	
  private String rangePropertyName;
  private String choicePropertyName = CHOICES;
	
  private int default_rows = 10;
  private int default_cols = 50;
	
  private IPropertyListSource propListSource;
	
  public QueryFieldDefinition(  )
  {
		
  }
	
  public QueryFieldDefinition( FinderField finderField )
  {
    setName( finderField.getFieldName() );
    setDisplayName( finderField.getDisplayName() );
		
    setDataType( finderField.getDataType( ) );
    setMultiValue( finderField.isMultiValue( ) );
  }
	
  public QueryFieldDefinition( String name )
  {
    LOG.debug( "QueryFieldDefinition " + name );
    setName( name );
    setDisplayName( name );
    setDataType( "String" );
    setRequired( new Boolean( false ) );
    setMultiSelect( new Boolean( false ) );
  }

  public void setDisplayName( String displayName )
  {
    doSetProperty( new StringProperty( DISPLAY_NAME, displayName ) );
  }
		
  public String getDisplayName( )
  {
    IProperty nameProp = getProperty( DISPLAY_NAME );
    return (nameProp != null) ? nameProp.getValue( ) : null;
  }
	
  public void setDescription( String name )
  {
    doSetProperty( new StringProperty( DESCRIPTION, name ) );
  }
		
  public String getDescription( )
  {
    IProperty nameProp = getProperty( DESCRIPTION );
    return (nameProp != null) ? nameProp.getValue( ) : null;
  }
		
  public void setDataType( String dataType )
  {
    doSetProperty( new StringProperty( DATA_TYPE, dataType ));
  }
		
  public String getDataType(  )
  {
    IProperty typeProp = getProperty( DATA_TYPE );
    return (typeProp != null) ? typeProp.getValue( ) : null;
  }
	
  public void setRequired( Boolean required )
  {
    doSetProperty( new BooleanProperty(  REQUIRED, required.booleanValue() ));
  }
	
  public void setRequired( boolean required )
  {
    doSetProperty( new BooleanProperty(  REQUIRED, required ));
  }
	
  public boolean isRequired(  )
  {
    IProperty multiValProp = getProperty( REQUIRED );
    if (multiValProp == null || !(multiValProp instanceof BooleanProperty)) return false;
    
    BooleanProperty boolProp = (BooleanProperty)multiValProp;
    return boolProp.getBooleanValue( );
  }
	
	
  /**
   * Enables the user be able to select more than one value at a time.
   *
   * (Used by Finders to determine boolean logic for multi-select
   *  fields. If multi select and multi value - use AND. if multi select
   *  and NOT multi value, use OR
   *
   * @param multiSelect
   */
  public void setMultiSelect( Boolean multiSelect )
  {
    doSetProperty( new BooleanProperty( MULTI_SELECT, multiSelect.booleanValue() ));
  }
	
  public void setMultiSelect( boolean multiSelect )
  {
    doSetProperty( new BooleanProperty( MULTI_SELECT, multiSelect ));
  }
	
  public boolean isMultiSelect(  )
  {
    IProperty reqProp = getProperty( MULTI_SELECT );
    if (reqProp == null || !(reqProp instanceof BooleanProperty)) return false;
		
    BooleanProperty boolProp = (BooleanProperty)reqProp;
    return boolProp.getBooleanValue( );
  }
	
  /**
   * Can a record have more than one value of this field?
   *
   * @param multiValued
   */
  public void setMultiValue( Boolean multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue.booleanValue() ));
  }
	
  public void setMultiValue( boolean multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ));
  }
	
  public boolean isMultiValue(  )
  {
    IProperty reqProp = getProperty( MULTI_VALUE );
    if (reqProp == null || !(reqProp instanceof BooleanProperty)) return false;
		
    BooleanProperty boolProp = (BooleanProperty)reqProp;
    return boolProp.getBooleanValue( );
  }
	
  /**
   * Sets the valid range of the query field. Useful for setting UI controls that
   * have maximum and minimum settings.
   *
   * @param rangeProperty
   */
  public void setValidRange( IRangeProperty rangeProperty )
  {
    LOG.debug( "setRange" );
		
    String rangeName = rangeProperty.getName( );
    if (rangeName != null)
    {
      this.rangePropertyName = rangeName;
    }
    else
    {
      this.rangePropertyName = RANGE;
      rangeProperty.setName( rangePropertyName );
    }
		
    doSetProperty( rangeProperty );
  }
	
  /**
   * Returns an IRangeProperty specifying the valid range for this query field.
   *
   * @return
   */
  public IRangeProperty getValidRange(  )
  {
    if (rangePropertyName == null) return null;
		
    IProperty rangeProp = getProperty( this.rangePropertyName );
    if (rangeProp == null || !(rangeProp instanceof IRangeProperty)) return null;
		
    return (IRangeProperty)rangeProp;
  }
	
	

  /**
   * Sets the set of valid values for this field.
   *
   * @param choiceList
   */
  public void setChoiceList( PropertyList choiceList )
  {
    String choiceName = choiceList.getName( );
    if (choiceName != null)
    {
      this.choicePropertyName = choiceName;
    }
    else
    {
      this.choicePropertyName = CHOICES;
      choiceList.setName( choicePropertyName );
    }
		
    doSetProperty( choiceList );
  }
	
  public void setChoiceSource( IPropertyListSource listSource )
  {
    this.propListSource = listSource;
  }

  public PropertyList getChoiceList(  )
  {
    if (propListSource != null) return propListSource.getPropertyList( );
		
    if (choicePropertyName == null) return null;
		
    IProperty choiceProp = getProperty( this.choicePropertyName );
    if (choiceProp == null || !(choiceProp instanceof PropertyList)) return null;
		
    return (PropertyList)choiceProp;
  }
	
  public void setInitializeFrom( String initializeFrom )
  {
    doSetProperty( new StringProperty( INITIALIZE_FROM, initializeFrom ) );
  }
	
  public String getInitializeFrom( )
  {
    IProperty initFromProp = getProperty( INITIALIZE_FROM );
    return (initFromProp != null) ? initFromProp.getValue( ) : null;
  }
	
  // Sets the query mode for this field:
  public void setQueryModeSchema( QueryModeSchema queryModeSchema )
  {
    LOG.debug( "setQueryModeSchema( )" );
    if (queryModeSchema == null) return;
    queryModeSchema.setName( QueryModeSchema.NAME );
    doSetProperty( queryModeSchema );
  }
	
  public QueryModeSchema getQueryModeSchema( )
  {
    return (QueryModeSchema)getProperty( QueryModeSchema.NAME );
  }
	
  public String getQueryModeField( )
  {
    QueryModeSchema qms = getQueryModeSchema( );
    return (qms != null) ? qms.getQueryModeField() : null;
  }
	
  /**
   * Sets the IQueryFieldRenderer instance that will be used to render
   * this query field.
   *
   * @param rendererName
   */
  public void setRendererName( String rendererName )
  {
    doSetProperty( new StringProperty( RENDERER_NAME, rendererName ) );
  }
	
  public String getRendererName(  )
  {
    IProperty rendProp = getProperty( RENDERER_NAME );
    return (rendProp != null) ? rendProp.getValue( ) : null;
  }
	
  public void setQueryFormField( QueryFormField queryFormField )
  {
    LOG.debug( "setQueryFormField( )..." );
		
    if ( queryFormField != null )
    {
      if ( queryFormField.isMultiSelect() ) setMultiSelect( true );
        
      if ( queryFormField.getChoices() != null )
      {
        setChoiceList( queryFormField.getChoices() );
      }
      else if ( queryFormField.getChoiceSource() != null )
      {
        String choiceListSource = queryFormField.getChoiceSource( );
        ApplicationManager appManager = ApplicationManager.getInstance( );
        IPropertyListSource choiceSource = (IPropertyListSource)appManager.getApplicationObject( choiceListSource, "PropertyListSource" );
        if (choiceSource != null)
        {
          setChoiceSource( choiceSource );
        }
      }
			
      if ( queryFormField.getInitializeFrom( ) != null )
      {
        setInitializeFrom( queryFormField.getInitializeFrom( ) );
      }
			
      QueryModeSchema queryModeSchema = queryFormField.getQueryModeSchema( );
      if (queryModeSchema != null )
      {
        setQueryModeSchema( queryModeSchema );
      }
    }
  }
	
  public void setTextAreaRows( String textAreaRows )
  {
    doSetProperty( new IntegerProperty( TEXT_AREA_ROWS, textAreaRows ) );
  }
	
  public void setTextAreaRows( int textAreaRows )
  {
    doSetProperty( new IntegerProperty( TEXT_AREA_COLS, textAreaRows ) );
  }
	
  public int getTextAreaRows(  )
  {
    IProperty rowsProp = getProperty( TEXT_AREA_ROWS );
    if (rowsProp == null || !(rowsProp instanceof IntegerProperty)) return default_rows;
    IntegerProperty intProp = (IntegerProperty)rowsProp;
    return intProp.getIntegerValue( );
  }
	
  public void setTextAreaCols( String textAreaCols )
  {
    doSetProperty( new IntegerProperty( TEXT_AREA_COLS, textAreaCols ) );
  }
	
  public void setTextAreaCols( int textAreaCols )
  {
    doSetProperty( new IntegerProperty( TEXT_AREA_COLS, textAreaCols ) );
  }
	
  public int getTextAreaCols(  )
  {
    IProperty colsProp = getProperty( TEXT_AREA_COLS );
    if (colsProp == null || !(colsProp instanceof IntegerProperty)) return default_cols;
    IntegerProperty intProp = (IntegerProperty)colsProp;
    return intProp.getIntegerValue( );
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "QueryFieldDefinition" );
    
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( DISPLAY_NAME );
    dos.addPropertyDescriptor( pd );
		
    pd = new PropertyDescriptor( );
    pd.setName( DATA_TYPE );
    dos.addPropertyDescriptor( pd );
      
    pd = new PropertyDescriptor( );
    pd.setName( REQUIRED );
    pd.setPropertyType( "Boolean" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( MULTI_SELECT );
    pd.setPropertyType( "Boolean" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( MULTI_VALUE );
    pd.setPropertyType( "Boolean" );
    dos.addPropertyDescriptor( pd );
    
    // public static final String RANGE           = "Range";
    
    pd = new PropertyDescriptor( );
    pd.setName( DESCRIPTION );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( CHOICES );
    pd.setPropertyType( "Enumeration" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( INITIALIZE_FROM );
    dos.addPropertyDescriptor( pd );
    
    return dos;
  }
}
