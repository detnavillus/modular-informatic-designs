package com.modinfodesigns.ontology.builder;

import java.util.List;

import com.modinfodesigns.search.IQueryTreeSetBuilder;
import com.modinfodesigns.search.QueryTreeSet;
import com.modinfodesigns.search.QueryTreeBuilder;
import com.modinfodesigns.search.QueryTree;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.property.compare.IPropertyMatcher;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.security.IUserCredentials;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Builds a Set of QueryTree objects from a Taxonomy. Builds a QueryTree
 * from each descendant node that (optionally) matches the criteria specified
 * by an IPropertyHolderMatcher.
 * 
 * @author Ted Sullivan
 */

public class TaxonomyQueryTreeSetBuilder implements IQueryTreeSetBuilder
{
  private ITaxonomyNode taxonomy;
    
  private ITaxonomyBuilder taxonomyBuilder;
    
  private IPropertyHolderMatcher nodeMatcher;
    
  private QueryTreeBuilder queryTreeBuilder;
    
  private IUserCredentials userCredentials;
    
  private boolean recursive = true;
    
  private ArrayList<OutputPropertyMapper> outputProperties;
    
    
  public void setTaxonomy( ITaxonomyNode taxonomy )
  {
    this.taxonomy = taxonomy;
  }
    
  public void setTaxonomyBuilder( ITaxonomyBuilder taxonomyBuilder )
  {
    this.taxonomyBuilder = taxonomyBuilder;
  }
    
  private ITaxonomyNode getTaxonomy( )
  {
    if (taxonomy != null) return taxonomy;
        
    if (taxonomyBuilder != null)
    {
      taxonomy = taxonomyBuilder.buildTaxonomy( );
    }
        
    return taxonomy;
  }
    
  public void setDataMatcher( IPropertyHolderMatcher nodeMatcher )
  {
    this.nodeMatcher = nodeMatcher;
  }
    
  public void addOutputPropertyMappers( DataList outputPropList )
  {
    for (Iterator<DataObject> dit = outputPropList.getData(); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      IProperty nameProp = dobj.getProperty( "input" );
      if (nameProp != null)
      {
        String propertyName = nameProp.getValue();
        IProperty mapTo = dobj.getProperty( "output" );
        if (mapTo != null)
        {
          String mapToProp = mapTo.getValue( );
            
          Object pMatcherOb = dobj.getObject( "PropertyMatcher" );
          if (pMatcherOb instanceof IPropertyMatcher)
          {
            OutputPropertyMapper opm = new OutputPropertyMapper( );
            opm.propertyName = propertyName;
            opm.mapTo = mapToProp;
            opm.propertyMatcher = (IPropertyMatcher)pMatcherOb;
                        
            if (outputProperties == null) outputProperties = new ArrayList<OutputPropertyMapper>( );
            outputProperties.add( opm );
          }
        }
      }
    }
  }
    
    
  @Override
  public QueryTreeSet createQueryTreeSet()
  {
    ITaxonomyNode taxonomy = getTaxonomy( );
        
    QueryTreeSet qTreeSet = new QueryTreeSet( );
    List<ITaxonomyNode> descendants = taxonomy.getDescendants();
    for (int i = 0; i < descendants.size( ); i++ )
    {
      ITaxonomyNode descendant = descendants.get( i );
      if (nodeMatcher == null || nodeMatcher.equals( userCredentials, descendant ))
      {
        QueryTree qTree = queryTreeBuilder.createQueryTree( descendant, recursive, (outputProperties == null) );
        qTreeSet.addQueryTree( qTree );
          
        if (outputProperties != null)
        {
          for (int  p = 0; p < outputProperties.size(); p++ )
          {
            OutputPropertyMapper opm = outputProperties.get( p );
            IProperty prop = descendant.getProperty( opm.propertyName );
            if (prop != null && (opm.propertyMatcher == null || opm.propertyMatcher.equals( userCredentials, prop )))
            {
              IProperty copy = prop.copy( );
              if (opm.mapTo != null)
              {
                copy.setName( opm.mapTo );
              }
                            
              qTree.addProperty( copy );
            }
          }
        }
      }
    }
    
    return qTreeSet;
  }
    
  // =======================================================================
  // Configuration
  // <DataList type="OutputPropertyMappers" >
  //   <Property input="[ property name ]" output="[map to ]" >
  //     <PropertyMatcher ... >
  //   </Property>
  // </DataList>
  // =======================================================================
  class OutputPropertyMapper
  {
    String propertyName;  // PARENT_NAME|NAME|PATH|[property name ]
    String mapTo;         // Name of output property
      
    IPropertyMatcher propertyMatcher; // determines if this property should be added
  }

}
