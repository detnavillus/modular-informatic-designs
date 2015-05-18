package com.modinfodesigns.app.search.model;


import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the configurable display properties of a Navigator. Part of the 
 * NavigatorGroup application configuration.
 * 
 * @author Ted Sullivan
 */

public class NavigatorDisplayProperties extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NavigatorDisplayProperties.class );
	public static final String SORT_BY = "SortBy";
	public static final String MAX_FACETS = "MaxFacets";
	public static final String SHOW_COUNTS = "ShowCounts";
	public static final String MULTI_SELECT = "MultiSelect";
	public static final String RENDERER_NAME = "NavigatorRenderer";
	public static final String VISUALIZATION_TRANSFORM = "VisualizationTransform";
	public static final String VISUALIZATION_RENDERER = "VisualizationRenderer";
	
	public static final String MORE_LABEL = "MoreLabel";
	public static final String defaultMoreLabel = "more ...";
    
    public NavigatorDisplayProperties(  ) {  }
    
    public NavigatorDisplayProperties( String name ) 
    {
    	setName( name );
    }
    
    @Override
    public void setName( String name )
    {
    	this.name = name;
    }
    
    @Override
    public String getName( )
    {
    	return this.name;
    }
    
    public void setSortBy( String sortBy )
    {
    	doSetProperty( new StringProperty( SORT_BY, sortBy ) );
    }
    
    public String getSortBy(  )
    {
    	IProperty sortByProp = getProperty( SORT_BY );
    	return (sortByProp != null) ? sortByProp.getValue() : null;
    }
    
    public void setMaxFacets( String maxFacets )
    {
    	LOG.debug( "setMaxFacets: " + maxFacets );
    	doSetProperty( new IntegerProperty( MAX_FACETS, maxFacets ) );
    }
    
    
    public int getMaxFacets(  )
    {
    	IntegerProperty maxFacetProp = (IntegerProperty)getProperty( MAX_FACETS );
    	return (maxFacetProp != null) ? maxFacetProp.getIntegerValue() : 0;
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
    public void setShowCounts( String showCounts )
    {
    	LOG.debug( "setShowCounts: " + showCounts );
    	doSetProperty( new BooleanProperty( SHOW_COUNTS, showCounts ) );
    }
    
    public boolean getShowCounts( )
    {
    	BooleanProperty showCountsProp = (BooleanProperty)getProperty( SHOW_COUNTS );
    	return (showCountsProp != null) ? showCountsProp.getBooleanValue() : false;
    }
    
    public void setMultiSelect( String multiSelect )
    {
    	doSetProperty( new BooleanProperty( MULTI_SELECT, multiSelect ) );
    }
    
    public boolean isMultiSelect( )
    {
    	BooleanProperty multiSelProp = (BooleanProperty)getProperty( MULTI_SELECT );
    	return (multiSelProp != null) ? multiSelProp.getBooleanValue() : false;

    }
    

    public void setNavigatorRenderer( String navigatorRenderer )
    {
    	doSetProperty( new StringProperty( RENDERER_NAME, navigatorRenderer ));
    }
    
    public String getNavigatorRenderer( )
    {
    	IProperty navRendProp = getProperty( RENDERER_NAME );
    	return (navRendProp != null) ? navRendProp.getValue() : null;
    }
    
	public void setVisualizationTransform( String visualizationTransform )
	{
		doSetProperty( new StringProperty( VISUALIZATION_TRANSFORM, visualizationTransform ));
	}
	
	public String getVisualizationTransform(  )
	{
		IProperty vizProp = getProperty( VISUALIZATION_TRANSFORM );
		return (vizProp != null) ? vizProp.getValue( ) : null;
	}
	
	public void setVisualizationRenderer( String visualizationRenderer )
	{
		doSetProperty( new StringProperty( VISUALIZATION_RENDERER, visualizationRenderer ));
	}
	
	public String getVisualizationRenderer(  )
	{
		IProperty vizProp = getProperty( VISUALIZATION_RENDERER );
		return (vizProp != null) ? vizProp.getValue( ) : null;
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		DataObjectSchema dos = new DataObjectSchema( );
		dos.setName( "NagigatorDisplayProperties" );
		
		PropertyDescriptor pd = new PropertyDescriptor( );
		pd.setName( SORT_BY );
		pd.setPropertyType( "String" );
		dos.addPropertyDescriptor( pd );
		
		return dos;
	}
}
