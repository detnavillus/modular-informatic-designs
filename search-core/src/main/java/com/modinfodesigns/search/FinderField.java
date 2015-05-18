package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.ScalarQuantity;

/**
 * Describes the properties of an IFinder Source Field - a record field that is maintained in a 
 * search-able content source accessible through an IFinder implementation.  The properties of the Finder
 * fields determine what can be done via a search interface, the application requirements determine 
 * what IS done based on these capabilities. (i.e. what capabilities or features are exposed in 
 * a given search interface).
 * 
 * DefaultTextField - boolean property 
 * 
 * @author Ted Sullivan
 */
public class FinderField extends DataObjectBean
{
  public static final String FIELD_NAME         = "FieldName";
  public static final String SOURCE_FIELD_NAME  = "SourceFieldName";
  public static final String DISPLAY_NAME       = "DisplayName";
  public static final String SEARCHABLE         = "Searchable";
  public static final String DEFAULT_TEXT       = "DefaultTextField";
  public static final String DATA_TYPE          = "DataType";
  public static final String FACET_FIELD        = "FacetField";
  public static final String MULTI_VALUE        = "MultiValue";
  public static final String DISPLAYABLE        = "Displayable";
  public static final String SORTABLE           = "Sortable";
  public static final String ID_FIELD           = "IDField";
  public static final String EXACT_MATCH        = "ExactMatch";
  public static final String SUPPORTS_PROXIMITY = "SupportsProximity";
  public static final String SUPPORTS_WILDCARD  = "SupportsWildcard";
  public static final String FIELD_BOOST        = "FieldBoost";
  public static final String FACET_QUERY_FIELD  = "FacetQueryField";
	
  public static final String ENTITY_EXTRACTOR   = "EntityExtractor";  // name of EntityExtractor
  public static final String INDEX_MATCHER_FACTORY = "IndexMatcherFactory";
	
  public static final String STRING  = "STRING";		// Shorter text fields
  public static final String TEXT    = "TEXT";
  public static final String DATE    = "DATE";
  public static final String INTEGER = "INTEGER";
  public static final String FLOAT   = "FLOAT";
	
  public static EnumerationProperty dataTypeProperty;
	
  static
  {
    dataTypeProperty = new EnumerationProperty( );
    dataTypeProperty.addChoice( new StringProperty( STRING, STRING ));
    dataTypeProperty.addChoice( new StringProperty( TEXT, TEXT ));
    dataTypeProperty.addChoice( new StringProperty( DATE, DATE ));
    dataTypeProperty.addChoice( new StringProperty( INTEGER, INTEGER ));
    dataTypeProperty.addChoice( new StringProperty( FLOAT, FLOAT ));
  }
	
  public FinderField( ) {  }
	
  public FinderField( String fieldName )
  {
    setFieldName( fieldName );
  }
    
  /**
   * Set the abstract field name or the name as it is known to the search application.
   *
   * @param fieldName
   */
  public void setFieldName( String fieldName )
  {
    doSetProperty( new StringProperty( FIELD_NAME, fieldName ));
  }
	
  public String getFieldName( )
  {
    IProperty fieldNameProp = getProperty( FIELD_NAME );
    return (fieldNameProp != null) ? fieldNameProp.getValue( ) : null;
  }
	

  /**
   * Set the source field name, or the name as it is known to the back end information
   * retrieval system.
   *
   * @param sourceFieldName
   */
  public void setSourceFieldName( String sourceFieldName )
  {
    doSetProperty( new StringProperty( SOURCE_FIELD_NAME, sourceFieldName ));
  }
    
  // source field name
  public String getSourceFieldName( )
  {
    IProperty sourceNameProp = getProperty( SOURCE_FIELD_NAME );
    return (sourceNameProp != null) ? sourceNameProp.getValue( ) : null;
  }
    
    
  /**
   * Set the human-readable name for the field.
   *
   * @return
   */
  public void setDisplayName( String displayName )
  {
    doSetProperty( new StringProperty( DISPLAY_NAME, displayName ));
  }
    
  public String getDisplayName(  )
  {
    IProperty displayNameProp = getProperty( DISPLAY_NAME );
    return (displayNameProp != null) ? displayNameProp.getValue( ) : null;
  }
    
  public void setFacetQueryField( String facetQueryField )
  {
    doSetProperty( new StringProperty( FACET_QUERY_FIELD, facetQueryField ));
  }
    
  public String getFacetQueryField(  )
  {
    IProperty facetQueryProp = getProperty( FACET_QUERY_FIELD );
    return (facetQueryProp != null) ? facetQueryProp.getValue( ) : null;
  }
    

  /**
   *  Is the field searchable?
   *
   * @param searchable
   */
    
  public void setSearchable( String searchable )
  {
    doSetProperty( new BooleanProperty( SEARCHABLE, searchable ));
  }
    
  public void setSearchable( boolean searchable )
  {
    doSetProperty( new BooleanProperty( SEARCHABLE, searchable ));
  }
	

  public boolean isSearchable( )
  {
    IProperty searchableProp = getProperty( SEARCHABLE );
    return searchableProp != null && searchableProp.getValue().equals( BooleanProperty.TRUE );
  }


  /**
   * Is the field the (or one of the) default fields that is used for general
   * text-based searching?
   *
   * @param defaultField
   */

  public void setDefaultTextField( String defaultTextField )
  {
    doSetProperty( new BooleanProperty( DEFAULT_TEXT, defaultTextField ));
  }
    
  public void setDefaultTextField( boolean defaultTextField )
  {
    doSetProperty( new BooleanProperty( DEFAULT_TEXT, defaultTextField ));
  }
    
  public boolean isDefaultTextField( )
  {
    IProperty defaultTextProp = getProperty( DEFAULT_TEXT );
    return defaultTextProp != null && defaultTextProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Sets the data type of data contained in this field.
   * One of STRING | TEXT | DATE | INTEGER | FLOAT
   * @param dataType
   */
  public void setDataType( String dataType )
  {
    EnumerationProperty typeProp = (EnumerationProperty)dataTypeProperty.copy( );
    typeProp.setName( DATA_TYPE );
    try
    {
      typeProp.setValue( dataType, "" );
      doSetProperty( typeProp );
    }
    catch (PropertyValidationException pve )
    {
    		
    }
  }
    
  public String getDataType(  )
  {
    IProperty dataTypeProp = getProperty( DATA_TYPE );
    return (dataTypeProp != null) ? dataTypeProp.getValue( ) : null;
  }
	
  /**
   *  is the field a facet field (i.e. can be used for a Navigator)?
   *
   * @return  true if is a Facet field, false otherwise
   */
  public void setIsFacet( boolean facetField )
  {
    doSetProperty( new BooleanProperty( FACET_FIELD, facetField ));
  }
    
  public void setIsFacet( String facetField )
  {
    doSetProperty( new BooleanProperty( FACET_FIELD, facetField ));
  }
    
  public boolean isFacet( )
  {
    IProperty facetProp = getProperty( FACET_FIELD );
    return facetProp != null && facetProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Can a record have more than one value for this field?
   *
   * @param multiValue  true if record can have more than one value,
   *                    false otherwise
   */
  public void setMultiValue( String multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ));
  }
    
  public void setMultiValue( boolean multiValue )
  {
    doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ));
  }
    
  public boolean isMultiValue( )
  {
    IProperty multiProp = getProperty( MULTI_VALUE );
    return multiProp != null && multiProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Can the field be used for exact match queries?
   *
   * @param exactMatchField
   */
  public void setExactMatch( boolean exactMatchField )
  {
    doSetProperty( new BooleanProperty( EXACT_MATCH, exactMatchField ));
  }

  public void setExactMatch( String exactMatchField )
  {
    doSetProperty( new BooleanProperty( EXACT_MATCH, exactMatchField ));
  }
    
  public boolean isExactMatch( )
  {
    IProperty exactProp = getProperty( EXACT_MATCH );
    return exactProp != null && exactProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Is the value of this field unique within the content repository (i.e. can it be used
   * for record searching)?
   *
   * @param unique
   */
  public void setIDField( boolean idField )
  {
    doSetProperty( new BooleanProperty( ID_FIELD, idField ));
  }
    
  public void setIDField( String idField )
  {
    doSetProperty( new BooleanProperty( ID_FIELD, idField ));
  }

  public boolean isIDField(  )
  {
    IProperty idProp = getProperty( ID_FIELD );
    return idProp != null && idProp.getValue().equals( BooleanProperty.TRUE );
  }
    

    
  /**
   * Is the field displayable?
   *
   * @param displayable
   */
  public void setDisplayable( String displayable )
  {
    doSetProperty( new BooleanProperty( DISPLAYABLE, displayable ));
  }
    
  public void setDisplayable( boolean displayable )
  {
    doSetProperty( new BooleanProperty( DISPLAYABLE, displayable ));
  }
	
  public boolean isDisplayable( )
  {
    IProperty dispProp = getProperty( DISPLAYABLE );
    return dispProp != null && dispProp.getValue().equals( BooleanProperty.TRUE );
  }
    
    
  /**
   * Is the field sortable?
   *
   * @param sortable
   */
  public void setSortable( boolean sortable )
  {
    doSetProperty( new BooleanProperty( SORTABLE, sortable ));
  }

  public void setSortable( String sortable )
  {
    doSetProperty( new BooleanProperty( SORTABLE, sortable ));
  }
    
  public boolean isSortable(  )
  {
    IProperty sortProp = getProperty( SORTABLE );
    return sortProp != null && sortProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Does the field support proximity queries (NEAR, ONEAR)?
   *
   * @param supportsProximity
   */
  public void setSupportsProximity( boolean supportsProximity )
  {
    doSetProperty( new BooleanProperty( SUPPORTS_PROXIMITY, supportsProximity ));
  }
    
  public boolean supportsProximity( )
  {
    IProperty proxProp = getProperty( SUPPORTS_PROXIMITY );
    return proxProp != null && proxProp.getValue().equals( BooleanProperty.TRUE );
  }
    
    
  /**
   * Does the field support wildcard queries?
   *
   * @param supportsWildcard
   */
  public void setSupportsWildcard( boolean supportsWildcard )
  {
    doSetProperty( new BooleanProperty( SUPPORTS_WILDCARD, supportsWildcard ));
  }
    
  public boolean supportsWildcard( )
  {
    IProperty wildProp = getProperty( SUPPORTS_WILDCARD );
    return wildProp != null && wildProp.getValue().equals( BooleanProperty.TRUE );
  }
    
  /**
   * Sets the name (reference) of the EntityExtractor module that should be used to generate
   * the field data.
   *
   * @param entityExtractor  Name of EntityExtractor module
   */
  public void setEntityExtractorRef( String entityExtractor )
  {
    doSetProperty( new StringProperty( ENTITY_EXTRACTOR, entityExtractor ));
  }
    
  public String getEntityExtractorRef(  )
  {
    IProperty entExtProp = getProperty( ENTITY_EXTRACTOR );
    return (entExtProp != null) ? entExtProp.getValue( ) : null;
  }
    
  /**
   * Sets the IndexMatcherFactory that will be used to generate data for this
   * field.
   *
   * @param indexMatcherFactory  Name of IndexMatcherFactory module
   */
  public void setIndexMatcherFactoryRef( String indexMatcherFactory )
  {
    doSetProperty( new StringProperty( INDEX_MATCHER_FACTORY, indexMatcherFactory ));
  }
    
  public String getIndexMatcherFactoryRef(  )
  {
    IProperty ndxFacProp = getProperty( INDEX_MATCHER_FACTORY );
    return (ndxFacProp != null) ? ndxFacProp.getValue( ) : null;
  }
    
  public void setFieldBoost( String fieldBoost )
  {
    doSetProperty( new ScalarQuantity( FIELD_BOOST, fieldBoost ) );
  }
    
  public float getFieldBoost(  )
  {
    ScalarQuantity boost = (ScalarQuantity)getProperty( FIELD_BOOST );
    if (boost != null)
    {
      return (float)boost.getQuantity( );
    }
    	
    return (float)0.0;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    return null;
  }
}
