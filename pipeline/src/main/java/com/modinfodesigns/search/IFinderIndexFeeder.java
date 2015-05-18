package com.modinfodesigns.search;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.app.search.model.ContentSource;

/**
 * Base Interface for DataObject Processors that feed content to an IFinder index. Enables the
 * index feeders to get access to the Finder Schema (FinderFields) so that the indexing and 
 * searching processes are properly coordinated.
 * 
 * @author Ted Sullivan
 */

public interface IFinderIndexFeeder extends IDataObjectProcessor 
{
    public void setContentSource( String contentSourceName );
    
    public ContentSource getContentSource( );

}
