package com.modinfodesigns.property.persistence;

import com.modinfodesigns.property.DataObjectTemplate;
import com.modinfodesigns.property.DataObjectTemplateManager;
import com.modinfodesigns.property.IProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a set of SQLTemplates for a DataObject Schema needed to implement the CRUD (Create/Update/Delete)
 * persistence. Used by SQLTemplatePersistenceManager in which the template set is generated from a 
 * set of PropertyDescriptor objects.
 * 
 * @author Ted Sullivan
 */

public class SQLTemplateSet extends DataObjectTemplateManager
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SQLTemplateSet.class );

  public static final String CREATE = "CREATE";
  public static final String UPDATE = "UPDATE";
    
  @Override
  public void addDataObjectTemplate( DataObjectTemplate sqlTemplate )
  {
    if (sqlTemplate == null || sqlTemplate.getName() == null)
    {
      LOG.error( "Cannot add SQLTemplate: null or name is null!" );
      return;
    }
    	
    if (sqlTemplate.getName().equalsIgnoreCase( CREATE ))
    {
      setCreateTemplate( sqlTemplate );
    }
    else if (sqlTemplate.getName().equalsIgnoreCase( UPDATE ))
    {
      setUpdateTemplate( sqlTemplate );
    }
    else
    {
      LOG.error( "Template name must be one of 'CREATE', 'UPDATE' or 'DELETE'!" );
    }
  }
    
  public void setCreateTemplate( DataObjectTemplate createTemplate )
  {
    createTemplate.setName( CREATE );
    setProperty( createTemplate );
  }

  public DataObjectTemplate getCreateTemplate(  )
  {
    IProperty createProp = getProperty( CREATE );
    return (createProp != null) ? (DataObjectTemplate)createProp : null;
  }
    
  public void setUpdateTemplate( DataObjectTemplate updateTemplate )
  {
    updateTemplate.setName( UPDATE );
    setProperty( updateTemplate );
  }
    
  public DataObjectTemplate getUpdateTemplate(  )
  {
    IProperty updateProp = getProperty( UPDATE );
    return (updateProp != null) ? (DataObjectTemplate)updateProp : null;
  }
 
}
