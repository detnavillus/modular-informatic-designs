package com.modinfodesigns.classify;

// ------------------------------------------------
// <PatternMap name="name" >
//   <IndexMatcher ... >
// </PatternMap>
// ------------------------------------------------
public class PatternMap
{
  private String name;
	
  private IIndexMatcher indexMatcher;
  private IIndexMatcherFactory indexMatcherFactory;
	
  public void setName( String name )
  {
    this.name = name;
  }
	
  public String getName( )
  {
    return this.name;
  }
	
  public void addIndexMatcher( IIndexMatcher indexMatcher )
  {
    setIndexMatcher( indexMatcher );
  }
	
  public void setIndexMatcher( IIndexMatcher indexMatcher )
  {
    this.indexMatcher = indexMatcher;
  }
	
  public void addIndexMatcherFactory( IIndexMatcherFactory indexMatcherFactory )
  {
    setIndexMatcherFactory( indexMatcherFactory );
  }
	
  public void setIndexMatcherFactory( IIndexMatcherFactory indexMatcherFactory )
  {
    this.indexMatcherFactory = indexMatcherFactory;
  }
	
  public IIndexMatcher getIndexMatcher( )
 {
    if (indexMatcher != null) return indexMatcher;
		
    if (indexMatcherFactory != null)
    {
      synchronized( this )
      {
        IIndexMatcher[] indexMatchers = indexMatcherFactory.createIndexMatchers();
        if (indexMatchers != null && indexMatchers.length == 1)
        {
          this.indexMatcher = indexMatchers[0];
        }
        else
        {
          OrIndexMatcher orMatcher = new OrIndexMatcher( );
          for (int i = 0; i < indexMatchers.length; i++)
          {
            orMatcher.addIndexMatcher( indexMatchers[i] );
          }
            
          this.indexMatcher = orMatcher;
        }
      }
    }
		
    return this.indexMatcher;
  }
}
