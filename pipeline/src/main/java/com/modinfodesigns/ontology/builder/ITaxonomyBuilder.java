package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.schema.ICreateDataObjectSchema;

/**
 * Base Interface for factory objects that can create Taxonomies (ITaxonomyNode).
 * 
 * @author Ted Sullivan
 */

public interface ITaxonomyBuilder
{
  ITaxonomyNode buildTaxonomy( );
    
  ITaxonomyNode buildTaxonomy( DataObject context );
}
