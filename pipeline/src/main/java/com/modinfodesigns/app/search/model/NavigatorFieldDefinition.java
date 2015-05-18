package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.search.INavigatorField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a Navigator or 'Faceted' search field. 
 * 
 * @author Ted Sullivan
 */
// TO DO:  Add maximum facets to display
// more link ...

// ADD Name of EntityExtractor that can be used with the Navigator ...

public class NavigatorFieldDefinition extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NavigatorFieldDefinition.class );
    
	public static final String NAME = "Name";
	public static final String DISPLAY_NAME = "DisplayName";
	
	public static final String SORT_BY = "SortBy";
	public static final String SORT_BY_COUNT = INavigatorField.SORT_BY_COUNT;
	public static final String SORT_BY_NAME  = INavigatorField.SORT_BY_NAME;
	
	public static final String MULTI_SELECT = "MultiSelect";
	public static final String MULTI_SELECT_AND = "MultiSelectAnd";
	public static final String MULTI_SELECT_OR  = "MultiSelectOr";
	
	public static final String MULTI_VALUE = "MultiValue";
	
	public static final String SHOW_COUNTS = "ShowCounts";
	public static final String QUERY_FIELD = "QueryField";
	
	public static final String MAX_FACETS = "MaxFacets";
	
	public static final String NAVIGATOR_RENDERER = "NavigatorRenderer";
	public static final String VISUALIZATION_TRANSFORM = "VisualizationTransform";
	public static final String VISUALIZATION_RENDERER = "VisualizationRenderer";
	
	public static final String MORE_LABEL = "MoreLabel";
	public static final String defaultMoreLabel = "More ...";
	
    // Field name
    // Display Name
	
    // sort by - counts | alphabetical
    // multi-select: and - or - none
	// show counts
	// max to show before "more ..." link
	
	public NavigatorFieldDefinition( ) {  }
	
	public NavigatorFieldDefinition( FinderField ff )
	{
		LOG.debug( "FinderField constructor " + ff.getFieldName( ) );
		
		// do the initialization from FinderField properties
		setName( ff.getFieldName( ) );
		setDisplayName( ff.getDisplayName( ) );
		
		if ( ff.getFacetQueryField( ) != null)
		{
		    setQueryField( ff.getFacetQueryField( ));
		}
		
		setMultiValue( ff.isMultiValue( ) );
		LOG.debug( this + " Is Multi-Value = " + isMultiValue( ) );
	}
	
	public NavigatorFieldDefinition( String fieldName )
	{
		setName( fieldName );
		setDisplayName( fieldName );
	}
	
	public void setDisplayName( String displayName )
	{
		LOG.debug( "setDisplayName( '" + displayName + "' )" );
		doSetProperty( new StringProperty( DISPLAY_NAME, displayName ) );
	}
		
	public String getDisplayName( )
	{
		IProperty nameProp = getProperty( DISPLAY_NAME );
		return (nameProp != null) ? nameProp.getValue( ) : null;
	}
	
	public void setSortBy( String sortBy )
	{
		if (sortBy != null && sortBy.equals( SORT_BY_COUNT))
		{
			setSortByCount( );
		}
		else if (sortBy != null && sortBy.equals( SORT_BY_NAME ))
		{
			setSortByName( );
		}
	}
	
	public void setSortByCount( )
	{
		doSetProperty( new StringProperty( SORT_BY, SORT_BY_COUNT ) );
	}
	
	public void setSortByName( )
	{
		doSetProperty( new StringProperty( SORT_BY, SORT_BY_NAME ) );
	}
	
	public String getSortBy( )
	{
		IProperty sortProp = getProperty( SORT_BY );
		return (sortProp != null) ? sortProp.getValue( ) : null;
	}
	
	public void setMultiSelectAnd( )
	{
		doSetProperty( new StringProperty( MULTI_SELECT, MULTI_SELECT_AND ) );
	}
	
	public void setMultiSelectOr( )
	{
		doSetProperty( new StringProperty( MULTI_SELECT, MULTI_SELECT_OR ) );
	}
	
	public String getMultiSelect( )
	{
		IProperty multProp = getProperty( MULTI_SELECT );
		return (multProp != null) ? multProp.getValue( ) : null;
	}
	
	public boolean isMultiSelect( )
	{
		return (getProperty( MULTI_SELECT ) != null);
	}
	
	public void setMultiValue( boolean multiValue )
	{
		doSetProperty( new BooleanProperty( MULTI_VALUE, multiValue ) );
	}
	
	public boolean isMultiValue( )
	{
		IProperty multiValProp = getProperty( MULTI_VALUE );
		return (multiValProp != null && multiValProp.getValue().equals( "true" ));
	}
	
	// Show Counts
	public void setShowCounts( boolean showCounts )
	{
		LOG.debug( "setShowCounts: " + showCounts );
        doSetProperty( new BooleanProperty( SHOW_COUNTS, showCounts ));
	}
	
	public boolean isShowCounts( )
	{
		return (getProperty( SHOW_COUNTS ) != null && ((BooleanProperty)getProperty(SHOW_COUNTS)).getBooleanValue() );
	}
	
	public void setMaxFacets( int maxFacets )
	{
		doSetProperty( new IntegerProperty( MAX_FACETS, maxFacets ));
	}
	
	public int getMaxFacets( )
	{
		IntegerProperty maxFacetsProp = (IntegerProperty)getProperty( MAX_FACETS );
		return (maxFacetsProp != null) ? maxFacetsProp.getIntegerValue( ) : 0;
	}
	
    public void setMoreLabel( String  moreLabel )
    {
    	doSetProperty( new StringProperty( MORE_LABEL, moreLabel ) );
    }
	
	public String getMoreLabel( )
	{
		IProperty moreProp = getProperty( MORE_LABEL );
		return (moreProp != null) ? moreProp.getValue( ) : defaultMoreLabel;
	}
	
    public void setQueryField( String queryField )
    {
    	doSetProperty( new StringProperty( QUERY_FIELD, queryField ) );
    }
    
    public String getQueryField(  )
    {
    	IProperty queryProp = getProperty( QUERY_FIELD );
    	return (queryProp != null) ? queryProp.getValue() : null;
    }
	/**
	 * NavigatorRenderer - specialized Renderer for Navigator (on search page) - e.g. NavigatorTaxonomyRenderer
	 * 
	 * @param navigatorRenderer
	 */
	public void setNavigatorRenderer( String navigatorRenderer )
	{
		doSetProperty( new StringProperty( NAVIGATOR_RENDERER, navigatorRenderer ) );
	}
	
	public String getNavigatorRenderer( )
	{
		IProperty rendProp = getProperty( NAVIGATOR_RENDERER );
		return (rendProp != null) ? rendProp.getValue( ) : null;
	}
	
	
	/**
	 * Associated IPropertyHolderTransform for creating visualization of selected navigator
	 * i.e. "Why did this navigator hit?"
	 * 
	 * @param visualizationTransform
	 */
	public void setVisualizationTransform( String visualizationTransform )
	{
		doSetProperty( new StringProperty( VISUALIZATION_TRANSFORM, visualizationTransform ));
	}
	
	public String getVisualizationTransform(  )
	{
		IProperty vizProp = getProperty( VISUALIZATION_TRANSFORM );
		return (vizProp != null) ? vizProp.getValue( ) : null;
	}
	
	/**
	 * name of ResultFieldRenderer used to render the output of the Visualization Transform,
	 * if null, will use BasicResultFieldRenderer
	 * 
	 * @param visualizationRenderer
	 */
	public void setVisualizationRenderer( String visualizationRenderer )
	{
		doSetProperty( new StringProperty( VISUALIZATION_RENDERER, visualizationRenderer ));
	}
	
	public String getVisualizationRenderer(  )
	{
		IProperty vizProp = getProperty( VISUALIZATION_RENDERER );
		return (vizProp != null) ? vizProp.getValue( ) : null;
	}
	
	public void setNavigatorProperties( NavigatorDisplayProperties navProps )
	{
		LOG.debug( "setNavigatorProperties ..." );
		// if field is multiValue and navProps is multiSelect
		//    setMultiSelectAND
		// if field is NOT multiValue and navProps is multiSelect
		//    setMultiSelectOR
		
		if (navProps.isMultiSelect())
		{
			if (isMultiValue( ))
			{
				setMultiSelectAnd( );
			}
			else
			{
				setMultiSelectOr();
			}
		}
		
		setSortBy( navProps.getSortBy( ) );
		setShowCounts( navProps.getShowCounts( ) );
		setMaxFacets( navProps.getMaxFacets( ) );
		setMoreLabel( navProps.getMoreLabel( ) );

		if (navProps.getNavigatorRenderer() != null)      setNavigatorRenderer( navProps.getNavigatorRenderer() );
		if (navProps.getVisualizationTransform() != null) setVisualizationTransform( navProps.getVisualizationTransform() );
		if (navProps.getVisualizationRenderer() != null)  setVisualizationRenderer( navProps.getVisualizationRenderer() );
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		return null;
	}
}
