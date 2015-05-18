package com.modinfodesigns.classify.subject;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.ThesaurusIndexMatcherFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a Taxonomy to create a Thesaurus Subject-specific matcher. 
 * 
 * Requires:
 *    The Root Path to the Taxonomy node that will be the Thesaurus root.
 *    The Semantic Type of the subject
 * 
 * @author Ted Sullivan
 */

// Use ThesaurusIndexMatcherFactory but filtered on Semantic Type 
public class ThesaurusSubjectMatcherFactory implements ISubjectMatcherFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ThesaurusSubjectMatcherFactory.class );

  private ThesaurusIndexMatcherFactory thesaurusMatcherFactory;
	
  private String semanticType;
	
  @Override
  public ISubjectMatcher createSubjectMatcher()
  {
    if (thesaurusMatcherFactory == null)
    {
      LOG.error( "Can't create SubjectMatcher: ThesaurusIndexMatcherFactory is NULL!" );
      return null;
    }
		
    ThesaurusSubjectMatcher thesSubjectMatcher = new ThesaurusSubjectMatcher( );
		
    // Get the composite Matcher from the ThesaurusIndexMatcherFactory
    // Filtered by semantic type ...
    IIndexMatcher[] indexMatchers = thesaurusMatcherFactory.createIndexMatchers( );
    if (indexMatchers == null || indexMatchers.length == 0)
    {
      LOG.error( "Got 0 Index Matchers!" );
      return null;
    }
		
    thesSubjectMatcher.setSubjectMatchers( indexMatchers );
		
    return thesSubjectMatcher;
  }

}
