package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Translates an IPropertyHolder Object into a QueryTree object ...
 */
public class QueryTreeBuilder implements IPropertyTransform, IQueryFilter
{
  private String defaultOperator = "OR";
	
  private IQueryTreeParser queryTreeParser;
	
  private String queryField;  // field that contains an advanced query string
	
  private String queryTextField;
	
  private String operatorField;
	
  private Set<String> modifierFields;
	
  private String queryTreeName = "QueryTree";
	
  private String nodeRuleField;   // picks up node rules --
	
  public static final String NODE_RULE       = "NODE";       // just build a query from this node, do not get child nodes
  public static final String CHILDREN_RULE   = "CHILDREN";   // use just the level below (children)
  public static final String DESCENDANT_RULE = "DESCENDANTS";
  public static final String NONE_RULE       = "NONE";
	
  private HashMap<String,IQueryTreeRenderer> queryRenderers;

	
  public void setQueryTreeParser( IQueryTreeParser queryTreeParser )
  {
    this.queryTreeParser = queryTreeParser;
  }
	
  public void setQueryField( String queryField )
  {
    this.queryField = queryField;
  }
	
  public void setQueryTextField( String queryTextField )
  {
    this.queryTextField = queryTextField;
  }
	
  public void setOperatorField( String operatorField )
  {
    this.operatorField = operatorField;
  }
	
  public void addModifierField( String modifierField )
  {
    if (modifierFields == null) modifierFields = new HashSet<String>( );
    modifierFields.add( modifierField );
  }
	
  public void addQueryTreeRenderer( IQueryTreeRenderer queryTreeRenderer )
  {
    if (queryTreeRenderer == null) return;
    
    if (queryRenderers == null) queryRenderers = new HashMap<String,IQueryTreeRenderer>( );
    queryRenderers.put( queryTreeRenderer.getFormat(), queryTreeRenderer );
  }
	
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return createQuery( input );
  }
	
  public IQuery filterQuery( IQuery input )
  {
    return input;
  }
 
    
  public IQuery createQuery( IProperty input )
  {
    if (input instanceof IPropertyHolder)
    {
      return createQuery( (IPropertyHolder)input );
    }
    	
    return doCreateQuery( input );
  }
 
    
  public IQuery createQuery( IPropertyHolder input )
  {
    return createQueryTree( input, true, true );
  }
    
  public QueryTree createQueryTree( IPropertyHolder input, boolean recursive, boolean addProperties )
  {
    boolean addChildren = recursive;
    boolean addDescendants = recursive;
    	
    // =======================================================
    // check to see if the input is a DataList ---
    // create an AND or OR query from that ...
    // =======================================================
    if (nodeRuleField != null && input.getProperty( nodeRuleField ) != null)
    {
      IProperty nodeRule = input.getProperty( nodeRuleField );
      if (nodeRule.getValue().equals( NONE_RULE ))
      {
        return null;
      }
      else if (nodeRule.getValue().equals( CHILDREN_RULE ))
      {
        addChildren = true;
        addDescendants = false;
      }
      else if (nodeRule.getValue().equals( DESCENDANT_RULE ))
      {
        addChildren = true;
        addDescendants = true;
      }
      else if (nodeRule.getValue().equals( NODE_RULE ))
      {
        addChildren = false;
        addDescendants = false;
      }
    }
    	
    QueryTree qTree = new QueryTree( );
    qTree.setOperator( defaultOperator );
    if (queryRenderers != null)
    {
      qTree.setQueryRenderers( queryRenderers );
    }
    	
    if (queryField != null) qTree.setQueryField( queryField );
    for (Iterator<IProperty> propIt = input.getProperties(); propIt.hasNext(); )
    {
      IProperty prop = propIt.next();
      if (prop instanceof QueryTree )
      {
        qTree.addChild( (QueryTree)prop );
      }
      else if (prop instanceof IPropertyHolder && addChildren)
      {
        IPropertyHolder child = (IPropertyHolder)prop;
        QueryTree subTree = createQueryTree( child, addDescendants, addProperties );
        if (subTree != null)
        {
          qTree.addChild( subTree );
        }
      }
      else
      {
        boolean isQTreeField = false;
          
        if (queryTextField != null && prop.getName().equals( queryTextField ))
        {
          qTree.setQueryText( prop.getValue() );
          isQTreeField = true;
        }
        
        if (operatorField != null && prop.getName().equals( operatorField ))
        {
          qTree.setOperator( prop.getValue() );
          isQTreeField = true;
        }
    		
        if (modifierFields != null && modifierFields.contains( prop.getName() ))
        {
          qTree.addQueryModifier( prop );
          isQTreeField = true;
        }
        		
        if (addProperties && !isQTreeField)
        {
          qTree.addProperty( prop.copy( ) );
        }
      }
    }
    	
    return qTree; // for now ...
  }
    
    
  private IQuery doCreateQuery( IProperty input )
  {
    if (queryTreeParser != null )
    {
      return queryTreeParser.createQueryTree( queryTreeName, input.getValue( ) );
    }
    	
    Query query = new Query( );
    query.addProperty( input );
    
    return query;
  }

  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener )
                              throws PropertyTransformException
  {

  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    return (IPropertyHolder)createQuery( input );
  }

}
