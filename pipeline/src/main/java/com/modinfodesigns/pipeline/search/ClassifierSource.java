package com.modinfodesigns.pipeline.search;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Simple Bean object that contains the references to a classification or
 * tagging operation.  Used by the SearchIndexPipeline to build Classifier
 * taggers from IndexMatcher(s) or Entity Extractors.  Used by the Search
 * Application UI to acquire the IndexMatcher(s) or EntityExtractor used to
 * mark up a text object for visualization of the tagging activities.
 * 
 * @author Ted Sullivan
 *
 */

public class ClassifierSource
{
  ArrayList<String> inputFields;  // fields to be used by Classifier for analysis
  HashSet<String> outputFields;
	
  String entityTaggerField;
	
  String taggerName; // name of IndexMatcherFactory or EntityExtractor
	
  String taggerType; // IndexMatcher | EntityExtractor
	
  public void addOutputField( String outputField )
  {
    if (outputFields == null) outputFields = new HashSet<String>( );
    outputFields.add( outputField );
  }
	
  public Set<String> getOutputFields(  )
  {
    return this.outputFields;
  }
	
  public void setEntityTaggerField( String entityTaggerField )
  {
    this.entityTaggerField = entityTaggerField;
  }
	
  public void setTaggerName( String taggerName )
  {
    this.taggerName = taggerName;
  }
	
  public void setTaggerType( String taggerType )
  {
    this.taggerType = taggerType;
  }
	
  public void addInputField( String inputField )
  {
    if (inputFields == null) inputFields = new ArrayList<String>( );
    inputFields.add( inputField );
  }
	
  public String[] getInputFields( )
  {
    if (inputFields == null) return new String[0];
		
    String[] fieldList = new String[ inputFields.size( ) ];
    inputFields.toArray( fieldList );
    return fieldList;
  }

}
