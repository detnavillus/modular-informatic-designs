package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.LookupTransform;
import com.modinfodesigns.property.transform.PropertyTemplateTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.Query;
import com.modinfodesigns.search.QueryTree;

/**
 * LookupProperty - performs an external search to provide a dynamic property
 * to a Property Holder. The query used can be fixed or derived from properties
 * of the PropertyHolder on whose behalf the lookup function property operates.
 * 
 * @author Ted Sullivan
 */

public class LookupProperty implements IFunctionProperty
{
  private String name;
  private IPropertyHolder propHolder;
  private String function;
    
  private String queryType;  // Query or QueryTree
  private String queryTemplate;
  private IQuery query;
  private String finderName;
    
  public LookupProperty(  ) {   }
    
  public LookupProperty( String jsonQuery )
  {
    setFunction( jsonQuery );
  }
    
  public LookupProperty( IQuery query )
  {
    setQuery( query );
  }
    
  public void setQuery( IQuery query )
  {
    this.query = query;
  }
    
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getType( ) : null;
  }

  @Override
  public String getValue()
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getValue( format ) : null;
  }

  @Override
  public void setValue( String value, String format)
                        throws PropertyValidationException
  {

  }

  @Override
  public String getDefaultFormat()
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getDefaultFormat( ) : null;
  }

  @Override
  public IProperty copy()
  {
    LookupProperty copy = new LookupProperty( );
    copy.name = this.name;
    copy.propHolder = this.propHolder;
    copy.setFunction( this.function );
    
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getValueObject(  ) : null;
  }

  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    this.propHolder = propHolder;
  }

  // =========================================================================
  // function should be in the form of a
  // JSON string that contains the following properties:
  // { finderName:[finder name],
  //   queryType:[Query|QueryTree],
  //   queryTemplate:"{query template in which the place holders are property names
  //                  that can be acquired from the wrapped PropertyHolder. The
  //                  template should be placed within quotes so that it is not parsed
  //                   as an object}"
  //   Or
  //   query: {JSON representation of a Query or QueryTree}
  // }
  //
  // use JSON parser to get a DataObject if DataObject has an IQuery property
  // called "query" set the Query.
  // else if it has String property called "queryTemplate" set the queryTemplate
  // =========================================================================
  @Override
  public void setFunction(String function)
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    DataObject parserOut = jsonParser.createDataObject( function );
    if (parserOut != null)
    {
      String finder = (String)parserOut.get( "finderName" );
      if (finder != null)
      {
        this.finderName = finder;
      }
			
      String queryType = (String)parserOut.get( "queryType" );
      if (queryType != null)
      {
        this.queryType = queryType;
      }
			
      IProperty queryTemp = parserOut.getProperty( "queryTemplate" );
      if (queryTemp != null)
      {
        if (queryTemp instanceof StringProperty )
        {
          this.queryTemplate = queryTemp.getValue( );
        }
        else if (queryTemp instanceof IQuery)
        {
          this.query = (IQuery)queryTemp;
        }
        else
        {
          this.queryTemplate =  queryTemp.getValue( IProperty.JSON_FORMAT );
        }
      }
			
      IProperty queryProp = parserOut.getProperty( "query" );
      if (queryProp != null)
      {
        if (queryProp instanceof StringProperty )
        {
          String queryStr = queryProp.getValue( );
          this.query = createQuery( queryStr );
        }
        else if (queryProp instanceof DataObject )
        {
          String queryStr = queryProp.getValue( IProperty.JSON_FORMAT );
          this.query = createQuery( queryStr );
        }
        else if (queryProp instanceof IQuery)
        {
          this.query = (IQuery)queryProp;
        }
      }
    }
  }

  @Override
  public String getFunction()
  {
    return this.function;
  }

  @Override
  public IProperty execute()
  {
    IQuery theQuery = getQuery( );
        
    LookupTransform lookupTransform = new LookupTransform( );
    lookupTransform.setQuery( theQuery );
    lookupTransform.setFinderRef( this.finderName );
        
    try
    {
      return lookupTransform.transform( this.propHolder );
    }
    catch ( PropertyTransformException pte )
    {
        
    }
        
    return null;
  }
	
  private IQuery getQuery( )
  {
    // either return the fixed query or create one from
    // the template given the current PropertyHolder parent
    if (this.query != null)
    {
      return this.query;
    }
		
    if (this.queryTemplate != null)
    {
      String queryObjStr = null;
        
      // determine if the queryTemplate has placeholders or not ...
			
      PropertyTemplateTransform ptt = new PropertyTemplateTransform( );
      ptt.setOutputProperty( "_lookup_output_" );
			
      // if queryTemplate starts with '{' and ends with '}'
      // may want to strip them to avoid being confused as template
      // parameters...
        
			
      try
      {
        ptt.transformPropertyHolder( this.propHolder );
        IProperty output = propHolder.getProperty( "_lookup_output_" );
        if (output != null)
        {
          queryObjStr = output.getValue( );
          propHolder.removeProperty( "_lookup_output_" );
        }
      }
      catch ( Exception e )
      {
          
      }
        
      this.query = createQuery( queryObjStr );
    }
		
    return this.query;
  }
	
  private IQuery createQuery( String queryTemplate )
  {
    if (queryTemplate == null) return null;
		
    if (queryType != null && queryType.equals( "QueryTree" ))
    {
      QueryTree qt = new QueryTree( );
      qt.setValue( queryTemplate, IProperty.JSON_FORMAT );
      return qt;
    }
    else
    {
      Query qu = new Query( );
      qu.setValue( queryTemplate, IProperty.JSON_FORMAT );
      return qu;
    }
  }

	
  @Override
  public boolean isMultiValue()
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty lookupProp = execute( );
    return (lookupProp != null) ? lookupProp.getValues( format ) : null;
  }

}
