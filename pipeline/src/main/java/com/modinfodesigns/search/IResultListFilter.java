package com.modinfodesigns.search;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;

/**
 * Base Interface for IResultList filtering. Used with FilteredResultListFinder to provide
 * post-processing of search results.
 * 
 * @author Ted Sullivan
 */
public interface IResultListFilter extends IDataObjectProcessor
{
  public IResultList processResultList( IResultList data );
}
