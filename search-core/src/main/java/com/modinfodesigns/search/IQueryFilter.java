package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;

/**
 * Base Interface for Query Filtering. Used with FilteredQueryFinder to provide 
 * query pre-processing.
 * 
 * @author Ted Sullivan
 */

public interface IQueryFilter extends IPropertyHolderTransform
{
  public IQuery filterQuery( IQuery input );
    
  public IQuery createQuery( IProperty input );
}
