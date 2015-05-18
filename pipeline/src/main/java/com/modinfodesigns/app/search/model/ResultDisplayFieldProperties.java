package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;

public class ResultDisplayFieldProperties extends DataObjectBean
{
	public static final String URL_FIELD = "URLField";
	public static final String IMG_URL_FIELD = "ImgURLField";
	public static final String RESULT_TRANSFORM = "ResultTransform";
	public static final String RESULT_FIELD_RENDERER = "ResultFieldRenderer";
	
	private String fieldName;
    private String resultFieldRenderer;   // name of special IResultFieldRenderer for this field

    public void setName( String fieldName )
    {
    	this.fieldName = fieldName;
    }
    
    public String getName( )
    {
    	return this.fieldName;
    }
    
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
    	IProperty urlProp = getProperty( URL_FIELD );
    	return (urlProp != null) ? urlProp.getValue() : null;
    }
    
    public void setImgURLField( String imgURLField )
    {
    	doSetProperty( new StringProperty( IMG_URL_FIELD, imgURLField ));
    }
    
    public String getImgURLField( )
    {
    	IProperty imgURLProp = getProperty( IMG_URL_FIELD );
    	return (imgURLProp != null) ? imgURLProp.getValue() : null;
    }
    
    /**
     * sets name of Result IPropertyHolderTransform
     */
    public void setResultTransform( String resultTransform )
    {
    	doSetProperty( new StringProperty( RESULT_TRANSFORM, resultTransform ));
    }
    

    public String getResultTransform(  )
    {
    	IProperty resTransProp = getProperty( RESULT_TRANSFORM );
    	return (resTransProp != null) ? resTransProp.getValue() : null;
    }

    public void setResultFieldRenderer( String resultFieldRenderer )
    {
    	doSetProperty( new StringProperty( RESULT_FIELD_RENDERER, resultFieldRenderer ));
    }
    
    public String getResultFieldRenderer( )
    {
    	IProperty resRendProp = getProperty( RESULT_FIELD_RENDERER );
    	return (resRendProp != null) ? resRendProp.getValue() : null;
    }

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		return null;
	}
}
