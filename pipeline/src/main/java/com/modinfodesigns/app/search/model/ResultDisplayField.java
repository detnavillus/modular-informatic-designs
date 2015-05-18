package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.search.FinderField;

/**
 * Represents a Search result displayable field. Contains components needed to render
 * search result objects. 
 * 
 * @author Ted Sullivan
 */

public class ResultDisplayField extends DataObjectBean 
{
	public static final String NAME                  = "FieldName";
	public static final String DISPLAY_NAME          = "DisplayName";
	public static final String IS_ID_FIELD           = "IDField";
	public static final String URL_FIELD             = "URLField";
	public static final String IMG_URL_FIELD         = "ImgURLField";
	public static final String RESULT_TRANSFORM      = "ResultTransform";
	public static final String RESULT_FIELD_RENDERER = "ResultFieldRenderer";
	
	public ResultDisplayField( ) {  }
	
	public ResultDisplayField( FinderField ff )
	{
		setName( ff.getFieldName() );
		setDisplayName( ff.getDisplayName() );
		setIDField( ff.isIDField() );
	}
	
	public ResultDisplayField( String fieldName )
	{
		setName( fieldName );
		setDisplayName( fieldName );
	}
    
    // Name
	// Display Name (for grid views)
	
	public void setDisplayName( String displayName )
	{
		doSetProperty( new StringProperty( DISPLAY_NAME, displayName ) );
	}
	
	public String getDisplayName( )
	{
		IProperty displayProp = getProperty( DISPLAY_NAME );
		return (displayProp != null) ? displayProp.getValue() : null;
	}
	
	/**
	 * Sets true if this field can be used to do a single record retrieval
	 * from the Search server. Can be used to generate full display URL request.
	 * 
	 * @param isIDField
	 */
	public void setIDField( boolean isIDField )
	{
		doSetProperty( new BooleanProperty( IS_ID_FIELD, isIDField ));
	}
	
	public boolean isIDField( )
	{
		BooleanProperty idField = (BooleanProperty)getProperty( IS_ID_FIELD );
		return (idField != null && idField.getBooleanValue());
	}
	
	
	/**
	 * Sets the field used to provide hyperlink URLs that will be associated
	 * with this result display field. (for example a document URL associated
	 * with a title or document name field).
	 * 
	 * @param urlField
	 */
	public void setURLField( String urlField )
	{
		doSetProperty( new StringProperty( URL_FIELD, urlField ));
	}
	
	public void addURLField( String urlField )
	{
		setURLField( urlField );
	}
	
	public String getURLField( )
	{
		IProperty urlFieldProp = getProperty( URL_FIELD );
		return (urlFieldProp != null) ? urlFieldProp.getValue() : null;
	}
	
	public void setImgURLField( String imgURLField )
	{
		doSetProperty( new StringProperty( IMG_URL_FIELD, imgURLField ));	
	}
	
	public String getImgURLField( )
	{
		IProperty imgUrlProp = getProperty( IMG_URL_FIELD );
		return (imgUrlProp != null) ? imgUrlProp.getValue() : null;
	}
	
	public void setResultTransform( String resultTransform )
	{
		doSetProperty( new StringProperty( RESULT_TRANSFORM, resultTransform ));	
	}
	
	public String getResultTransform( )
	{
		IProperty resXformProp = getProperty( RESULT_TRANSFORM );
		return (resXformProp != null) ? resXformProp.getValue() : null;
	}
	
	public void setResultFieldRenderer( String rendererName )
	{
		doSetProperty( new StringProperty( RESULT_FIELD_RENDERER, rendererName ));	
	}
	
	public String getResultFieldRenderer( )
	{
		IProperty resRendProp = getProperty( RESULT_FIELD_RENDERER );
		return (resRendProp != null) ? resRendProp.getValue() : null;
	}

	public void setDisplayFieldProperties( ResultDisplayFieldProperties dispProps ) 
	{
		if (dispProps != null)
		{
			if (dispProps.getURLField() != null)            setURLField( dispProps.getURLField( ) );
			if (dispProps.getImgURLField() != null)         setImgURLField( dispProps.getImgURLField( ) );
			if (dispProps.getResultTransform() != null)     setResultTransform(dispProps.getResultTransform( ) );
			if (dispProps.getResultFieldRenderer() != null) setResultFieldRenderer( dispProps.getResultFieldRenderer( ) );
		}	
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		return null;
	}
}
