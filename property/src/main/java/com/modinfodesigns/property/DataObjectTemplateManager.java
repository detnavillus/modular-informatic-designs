package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;

import java.util.List;

/**
 * Manages a set of DataObjectTemplates as a DataObject. Enables DataObjectTemplates to be persisted as a set.
 * Enables DataObject's to be translated into strings for storage, retrieval or update, display etc..
 * 
 * @author Ted Sullivan
 */

public class DataObjectTemplateManager extends DataObject
{
	
  public void addDataObjectTemplate( DataObjectTemplate template )
  {
    setProperty( template );
  }
	
  public String renderTemplate( String templateName, IPropertyHolder propHolder )
  {
    if (propHolder == null) return null;
		
    DataObjectTemplate template = (DataObjectTemplate)getProperty( templateName );
    if (template != null)
    {
      DataObjectTemplate tmpCopy = (DataObjectTemplate)template.copy( );
      tmpCopy.setProperties( propHolder.getProperties( ) );
      tmpCopy.setName( propHolder.getName( ) );
      tmpCopy.setID( propHolder.getID( ) );
      tmpCopy.setType( propHolder.getType( ) );
      tmpCopy.setIsRootObject( propHolder.isRootObject( ) );
			
      if (propHolder instanceof DataObject && ((DataObject)propHolder).isProxied( ))
      {
        tmpCopy.setProperty( new StringProperty( "_PROXY_", ((DataObject)propHolder).getProxyKey( ) ));
      }
			
      return tmpCopy.getValue( );
    }
		
    return null;
  }
	
  public String renderTemplate( String templateName, List<IProperty> values )
  {
    if (values == null) return null;
		
    DataObjectTemplate template = (DataObjectTemplate)getProperty( templateName );
    if (template != null)
    {
      DataObjectTemplate tmpCopy = (DataObjectTemplate)template.copy( );
      for (int i = 0; i < values.size(); i++)
      {
        tmpCopy.setProperty( values.get( i ) );
      }
			
      return tmpCopy.getValue( );
    }
		
    return null;
  }
	
  public String renderTemplate( String templateName, PropertyList properties )
  {
    if (properties == null) return null;
		
    DataObjectTemplate template = (DataObjectTemplate)getProperty( templateName );
    if (template != null)
    {
      DataObjectTemplate tmpCopy = (DataObjectTemplate)template.copy( );
      tmpCopy.setProperty( properties );
      return tmpCopy.getValue( );
    }
		
    return null;
  }
	
}
