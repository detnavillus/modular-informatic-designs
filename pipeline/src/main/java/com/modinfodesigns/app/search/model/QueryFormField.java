package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaBean that defines configurable properties of a Query Field.
 * 
 * @author Ted Sullivan
 */
public class QueryFormField extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryFormField.class );
    public static final String MULTI_SELECT = "MultiSelect";
    public static final String CHOICES = "Choices";
    public static final String CHOICE_LIST_SOURCE = "ChoiceListSource";
    public static final String INITIALIZE_FROM = "InitializeFrom";
    
    public static final String ASSOCIATED_RANGE_FIELD = "AssociatedRangeField";
	public static final String RANGE_INCLUSIVE = "RangeInclusive";
    
    private String name;
    
    public QueryFormField(  ) {  }
    
    public QueryFormField( String name )
    {
        this.name = name;
    }
    
    public QueryFormField( QueryFieldDefinition qDef )
    {
        // copy fields ...
        setName( qDef.getName( ) );
        setMultiSelect( qDef.isMultiSelect() );
        if (qDef.getChoiceList() != null)
        {
            setChoices( qDef.getChoiceList() );
        }
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
   
    public void setMultiSelect( boolean multiSelect )
    {
        doSetProperty( new BooleanProperty( MULTI_SELECT, multiSelect ));
    }
   
    public boolean isMultiSelect( )
    {
        BooleanProperty multiSelectProp = (BooleanProperty)getProperty( MULTI_SELECT );
        return (multiSelectProp != null) ? ((BooleanProperty)multiSelectProp).getBooleanValue() : false;
    }
    
    public void setChoices( PropertyList choices )
    {
    	if (choices == null) return;
    	
        choices.setName( CHOICES );
        setProperty( choices );
    }
    
    public PropertyList getChoices(  )
    {
        PropertyList choicesProp = (PropertyList)getProperty( CHOICES );
        return choicesProp;
    }
    
    /**
     * Sets the name of an IPropertyListSource that can be used to
     * retrieve a choice list.
     * 
     * @param choiceListSource
     */
    public void setChoiceSource( String choiceListSource )
    {
        doSetProperty( new StringProperty( CHOICE_LIST_SOURCE, choiceListSource ));
    }
    
    public String getChoiceSource( )
    {
        IProperty choiceListProp = getProperty( CHOICE_LIST_SOURCE );
        return (choiceListProp != null) ? choiceListProp.getValue() : null;
    }
   
    public void setInitializeFrom( String initFrom )
    {
        doSetProperty( new StringProperty( INITIALIZE_FROM, initFrom ));
    }
    
    public String getInitializeFrom(  )
    {
        IProperty initFromProp = getProperty( INITIALIZE_FROM );
        return (initFromProp != null) ? initFromProp.getValue() : null;
    }
    
    public void addQueryModeSchema( String type )
    {
    	QueryModeSchema qms = QueryModeSchema.createQueryModeSchema( type );
    	if (qms != null)
    	{
    		addQueryModeSchema( qms );
    	}
    }
    
	public void addQueryModeSchema( QueryModeSchema queryModeSchema )
	{
		LOG.debug( "addQueryModeSchema: " + queryModeSchema );
		setQueryModeSchema( queryModeSchema );
	}
    
	public void setQueryModeSchema( QueryModeSchema queryModeSchema )
	{
		if (queryModeSchema == null) return;
		addProperty( queryModeSchema );
	}
	
	public QueryModeSchema getQueryModeSchema( )
	{
		return (QueryModeSchema)getProperty( QueryModeSchema.NAME );
	}
	
	
	/**
	 * Sets the parameter that will be used to define a Range Query in addition to this one.
	 * The associated range field should be interpreted as the Range Maximum by IFinder
	 * implementations. This field is the range Minimum.
	 * 
	 * @param rangeProperty
	 */
    public void setAssociatedRangeField( String rangeField )
    {
        doSetProperty( new StringProperty( ASSOCIATED_RANGE_FIELD, rangeField ));
    }
    
    
    public String getAssociatedRangeField(  )
    {
        IProperty assRangeProp = getProperty( ASSOCIATED_RANGE_FIELD );
        return (assRangeProp != null) ? assRangeProp.getValue() : null;
    }
    
    /**
     * Determines whether the range query is inclusive - matches the end points
     * or is exclusive (does not match the endpoints)
     * 
     * @param inclusive
     */
    public void setRangeInclusive( boolean inclusive )
    {
		doSetProperty( new BooleanProperty( RANGE_INCLUSIVE, inclusive ));
    }
    
	public boolean isRangeInclusive(  )
	{
		IProperty rangeInclusiveProp = getProperty(  RANGE_INCLUSIVE );
		if (rangeInclusiveProp == null || !(rangeInclusiveProp instanceof BooleanProperty)) return false;
		
		BooleanProperty boolProp = (BooleanProperty)rangeInclusiveProp;
		return boolProp.getBooleanValue( );
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		return null;
	}

}
