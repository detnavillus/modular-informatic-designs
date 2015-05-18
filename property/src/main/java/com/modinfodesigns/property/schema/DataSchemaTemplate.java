package com.modinfodesigns.property.schema;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as an Initialization template for DataObjectSchema that are created from
 * a fixed starting point. Template can specify the DataObject class to be used
 * with DataObjectSchema's derived from the template as well as any PropertyDescriptors that
 * should be included when a new schema based on the template is created.
 * 
 * @author Ted Sullivan
 */
public class DataSchemaTemplate extends DataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataSchemaTemplate.class );

  public static final String TEMPLATE_NAME = "TemplateName";

  public void setTemplateName( String templateName )
  {
    LOG.debug( "setTemplateName: " + templateName );
    setProperty( new StringProperty( TEMPLATE_NAME, templateName ));
  }
	
  public String getTemplateName(  )
  {
    IProperty tempNameProp = getProperty( TEMPLATE_NAME );
    return (tempNameProp != null) ? tempNameProp.getValue( ) : null;
  }
	
}
