package com.modinfodesigns.ontology;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.List;
import java.util.ArrayList;

public class TaxonomyRootNode extends TaxonomyNode
{
  private ArrayList<PropertyDescriptor> propDescriptors;
	
  public void addPropertyDescriptor( PropertyDescriptor pd )
  {
    if (propDescriptors == null) propDescriptors = new ArrayList<PropertyDescriptor>( );
    propDescriptors.add( pd );
  }
	
  public List<PropertyDescriptor> getPropertyDescriptors( )
  {
    return propDescriptors;
  }
	
	
  public DataObjectSchema createDataObjectSchema(DataObject context)
  {
    DataObjectSchema dos = super.createDataObjectSchema( context );
    	
    if (propDescriptors != null)
    {
      for (PropertyDescriptor pd : propDescriptors )
      {
        dos.addPropertyDescriptor( pd );
      }
    }
    	
    return dos;
  }

}
