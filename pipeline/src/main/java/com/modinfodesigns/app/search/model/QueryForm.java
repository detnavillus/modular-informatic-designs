package com.modinfodesigns.app.search.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.BooleanProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines fields in a Query Form (list of QueryFieldDefinition).  The Query Fields list defines 
 * the set of searchable fields. The default search field is typically the 'free text' field that 
 * is used to search all text fields (non field-specific query). Each field can have a QueryModeSchema 
 * associated with it to provide more advanced query options (see QueryFieldDefinition).
 * 
 * Query Form 
 *    One or more QueryFieldDefinition(s)
 *    QueryFieldDefinition for the default or free-text field.
 *    Sort Schema
 *    
 * Previous Query Parameter - if set, adds a JSON encoded version of the previous query in a hidden
 * field. This can be used for Search Within - Controller turns the previous query into a Scope Query if
 * the user selects Search Within.
 *     
 * @author Ted Sullivan
 */

// Query Form may need to add a prefix to query field names so that downstream code
// can detect that the parameters are coming from this query form.

public class QueryForm extends DataObject
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryForm.class );
    
  public static final String DEFAULT_SEARCH_FIELD = "DefaultSearchField";
  public static final String HTTP_METHOD      = "HttpMethod";
  public static final String REQUEST_ACTION   = "Action";
  public static final String FIELD_OPERATOR   = "FieldOperator";
  public static final String QUERY_FIELDS     = "QueryFields";
  public static final String ENABLE_BOOLEAN   = "EnableBoolean";
  public static final String ENABLE_PROXIMITY = "EnableProximity";
  public static final String ENABLE_WILDCARD  = "EnableWildcard";

  public static final String PREVIOUS_QUERY_FIELD = "PreviousQueryField";
  public static final String SEARCH_WITHIN_FIELD = "SearchWithin";
  public static final String SEARCH_WITHIN_OPERATOR_FIELD = "SearchWithinOperatorField";

  public static final String SUBMIT_BUTTON_TEXT = "SubmitButtonText";
    
  public static final String RELEVANCE_QUERY = "RelevanceQuery";
    
  private static final String defaultFieldOperator = "AND";
    
  private HashMap<String,QueryFormField> queryPropsMap;
    
  private QuerySchema querySchema;
  private List<QueryFieldDefinition> queryFieldList;
    
  public QueryForm( ) {  }
    
  public QueryForm( String name )
  {
    setName( name );
  }
    
  public void setQuerySchema( QuerySchema qSchema )
  {
    LOG.debug( "setQuerySchema( )..." );
    this.querySchema = qSchema;
  }

  /**
   * Sets the field that is used to transmit a 'free text' query - a query that
   * goes against all searchable fields (non field-specific query).
   */
  public void setDefaultSearchField( String defaultSearchField )
  {
    LOG.debug( "setDefaultSearchField( " + defaultSearchField + " )" );
    addProperty( new StringProperty( DEFAULT_SEARCH_FIELD, defaultSearchField ));
  }
    
    
  public String getDefaultSearchField(  )
  {
    IProperty defFieldProp = getProperty( DEFAULT_SEARCH_FIELD );
    return (defFieldProp != null) ? defFieldProp.getValue() : null;
  }
    
    
  /**
   * Sets the Boolean operator to use for across fields (Typically "AND")
   * @param fieldOperator
   */
  public void setFieldOperator( String fieldOperator )
  {
    setProperty( new StringProperty( FIELD_OPERATOR, fieldOperator ));
  }
    
    
  public String getFieldOperator(  )
  {
    IProperty fieldOpProp = getProperty( FIELD_OPERATOR );
    return (fieldOpProp != null) ? fieldOpProp.getValue() : defaultFieldOperator;
  }
    
  public void setEnableBooleanModes( boolean enableBooleanModes )
  {
    setProperty( new BooleanProperty( ENABLE_BOOLEAN, enableBooleanModes ) );
  }
    
  public boolean getEnableBooleanModes(  )
  {
    IProperty boolProp = getProperty( ENABLE_BOOLEAN);
    if (boolProp == null) return false;
        
    return ((BooleanProperty)boolProp).getBooleanValue();
  }
    
  public void setEnableProximityModes( boolean enableProximityModes )
  {
    setProperty( new BooleanProperty( ENABLE_PROXIMITY, enableProximityModes ) );
  }
    
  public boolean getEnableProximityModes(  )
  {
    IProperty proxProp = getProperty( ENABLE_PROXIMITY );
    if (proxProp == null) return false;
    
    return ((BooleanProperty)proxProp).getBooleanValue();
  }
    
  public void setEnableWildcardModes( boolean enableWildcardModes )
  {
    setProperty( new BooleanProperty( ENABLE_WILDCARD, enableWildcardModes ) );
  }
    
  public boolean getEnableWildcardModes(  )
  {
    IProperty wildProp = getProperty( ENABLE_WILDCARD );
    if (wildProp == null) return false;
    
    return ((BooleanProperty)wildProp).getBooleanValue();
  }
    
  public void setSubmitButtonText( String submitButtonText )
  {
    setProperty( new StringProperty( SUBMIT_BUTTON_TEXT, submitButtonText ));
  }
    
  public String getSubmitButtonText(  )
  {
    IProperty subButTxt = getProperty( SUBMIT_BUTTON_TEXT );
    return (subButTxt != null) ? subButTxt.getValue( ) : null;
  }
    
  public void addQueryFormField( QueryFormField queryProps )
  {
    LOG.debug( "addQueryFormField: " + queryProps.getName( ) );
    StringListProperty queryFields = (StringListProperty)getProperty( QUERY_FIELDS );
    if (queryFields == null)
    {
      queryFields = new StringListProperty( QUERY_FIELDS );
      setProperty( queryFields );
        
      queryPropsMap = new HashMap<String,QueryFormField>( );
    }
        
    queryFields.addString( queryProps.getName( ) );
    queryPropsMap.put( queryProps.getName( ), queryProps );
  }
    
    
  public QueryFormField getQueryFormField( String name )
  {
    return queryPropsMap.get( name );
  }
    
    
  public QueryFieldDefinition getQueryFieldDefinition( String name )
  {
    LOG.debug( "getQueryFieldDefinition: " + name );
    String fName = name;

    if (name != null && name.equals( DEFAULT_SEARCH_FIELD ))
    {
      fName = getDefaultSearchField( );
    }
        
    LOG.debug( "getQueryFieldDefinition: " + fName );
    QueryFieldDefinition qfd = (fName != null && querySchema != null) ? querySchema.getQueryField( fName ) : null;

    if (qfd == null && queryPropsMap.get( fName ) != null)
    {
      LOG.debug( "Adding " + fName + " to QuerySchema" );
      QueryFieldDefinition qfDef = new QueryFieldDefinition( fName );
      QueryFormField qfProps = queryPropsMap.get( fName );
      qfDef.setQueryFormField( qfProps );
        
      if (querySchema == null) querySchema = new QuerySchema( );
        
      querySchema.addQueryField( qfDef );
        	
      return qfDef;
    }
        
    return qfd;
  }
    
  public List<QueryFieldDefinition> getQueryFields(  )
  {
    LOG.debug( "getQueryFields( )" );
    	
    if (queryFieldList != null) return queryFieldList;
    	
    queryFieldList = new ArrayList<QueryFieldDefinition>( );
    StringListProperty queryFields = (StringListProperty)getProperty( QUERY_FIELDS );
    if (queryFields != null)
    {
      for (Iterator<String> fieldIt = queryFields.iterator(); fieldIt.hasNext(); )
      {
        String fieldName = fieldIt.next( );
        LOG.debug( "getting: " + fieldName );
                
        QueryFieldDefinition qfd = (querySchema != null) ? querySchema.getQueryField( fieldName ) : null;
                
        if (qfd == null && getDefaultSearchField(  ) != null &&
            fieldName.equals( getDefaultSearchField( ) ))
        {
          qfd = getQueryFieldDefinition( fieldName );
        }
                
        if (qfd != null)
        {
          QueryFieldDefinition qfdCopy = (QueryFieldDefinition)qfd.copy( );
                
          // check flags - if the query modes are disabled, remove them from the
          // QueryFieldDefinition
            
          QueryFormField qfProps = queryPropsMap.get( fieldName );
          qfdCopy.setQueryFormField( qfProps );
                
          if (!getEnableBooleanModes() && !getEnableProximityModes())
          {
            qfdCopy.removeProperty( QueryFieldDefinition.QUERY_MODE_FIELD );
          }
                
          queryFieldList.add( qfdCopy );
        }
        else if (queryPropsMap.get( fieldName ) != null)
        {
          QueryFieldDefinition qfDef = new QueryFieldDefinition( fieldName );
          QueryFormField qfProps = queryPropsMap.get( fieldName );
          qfDef.setQueryFormField( qfProps );
          queryFieldList.add( qfDef );
        }
        else
        {
          LOG.debug( "Cannot find field: " + fieldName );
        }
      }
    }
        
    return queryFieldList;
  }
    
  public boolean hasField( String queryField )
  {
    return (querySchema != null && querySchema.getQueryField( queryField ) != null);
  }
    
  /**
   * Sets the Sort Schema available to the user.
   *
   * @param sortSchema
   */
  public void setSortSchema( SortSchema sortSchema )
  {
    addProperty( sortSchema );
  }

  public void addSortSchema( SortSchema sortSchema )
  {
    setSortSchema( sortSchema );
  }
    
  public SortSchema getSortSchema( )
  {
    IProperty sortProp = getProperty( SortSchema.NAME );
    if (sortProp != null && sortProp instanceof SortSchema )
    {
      return (SortSchema)sortProp;
    }
        
    return null;
  }
    
  /**
   * Sets the HTTP Request method that is to be used by the UI
   *
   * @param httpRequestMethod
   */
  public void setHttpRequestMethod( String httpRequestMethod )
  {
    if (httpRequestMethod != null && (httpRequestMethod.equalsIgnoreCase( "get" ) ||
        httpRequestMethod.equalsIgnoreCase( "post" )))
    {
      StringProperty httpMethodProp = new StringProperty( HTTP_METHOD, httpRequestMethod );
      addProperty( httpMethodProp );
    }
  }
    
  public String getHttpRequestMethod(  )
  {
    IProperty httpMethodProp = getProperty( HTTP_METHOD );
    return (httpMethodProp != null) ? httpMethodProp.getValue() : "get";
  }
    
  public void setRequestAction( String requestAction )
  {
    addProperty( new StringProperty( REQUEST_ACTION, requestAction ) );
  }
    
  public String getRequestAction(  )
  {
    IProperty actionProp = getProperty( REQUEST_ACTION );
    return (actionProp != null) ? actionProp.getValue() : "";
  }
    
  /**
   * Sets the name of the hidden field that will hold the Previous query in the form
   * of a JSON string.
   *
   * @param previousQueryField
   */
  public void setPreviousQueryField( String previousQueryField )
  {
    addProperty( new StringProperty( PREVIOUS_QUERY_FIELD, previousQueryField));
  }
    
  public String getPreviousQueryField(  )
  {
    IProperty prevQueryProp = getProperty( PREVIOUS_QUERY_FIELD );
    return (prevQueryProp != null) ? prevQueryProp.getValue() : null;
  }
    
  /**
   * Sets the name of the form field that will contain the Search Within selection.
   *
   * @param searchWithinField
   */
  public void setSearchWithin( String searchWithin )
  {
    addProperty( new BooleanProperty( SEARCH_WITHIN_FIELD, searchWithin ));
  }
    
  public void setSearchWithin( boolean searchWithin )
  {
    addProperty( new BooleanProperty( SEARCH_WITHIN_FIELD, searchWithin ));
  }
    
  public boolean isSearchWithin(  )
  {
    BooleanProperty searchWithinProp = (BooleanProperty)getProperty( SEARCH_WITHIN_FIELD );
    return ( searchWithinProp != null ) ? searchWithinProp.getBooleanValue() : false;
  }
    
  public void setSearchWithinOperatorField( String searchWithinOperatorField )
  {
    setProperty( new StringProperty( SEARCH_WITHIN_OPERATOR_FIELD, searchWithinOperatorField ));
  }
    
  public String getSearchWithinOperatorField(  )
  {
    IProperty swOpFieldProp = getProperty( SEARCH_WITHIN_OPERATOR_FIELD );
    return (swOpFieldProp != null) ? swOpFieldProp.getValue( ) : null;
  }
    
  /**
   * Returns the name of the Relevance Query to use.
   *
   * @param relevanceQuery
   */
  public void setRelevanceQuery( String relevanceQuery )
  {
    addProperty( new StringProperty( RELEVANCE_QUERY, relevanceQuery ));
  }
    
  public String getRelevanceQuery(  )
  {
    IProperty relevanceProp = getProperty( RELEVANCE_QUERY );
    return (relevanceProp != null) ? relevanceProp.getValue() : null;
  }
}
