package com.modinfodesigns.ontology;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;

import java.util.List;

import junit.framework.TestCase;

/**
 * Test TaxonomyNode class and Tree construction methods.
 * 
 * @author Ted Sullivan
 */
public class TestTaxonomyMethods extends TestCase
{

  public void testTaxonomyMethods( ) throws TaxonomyException
  {
    TaxonomyNode rootNode = new TaxonomyNode( "root" );
    rootNode.addChildNode( new TaxonomyNode( "child1" ));
    TaxonomyNode child2 = new TaxonomyNode( "child2" );
    rootNode.addChildNode( child2 );
            
    TaxonomyNode child3 = new TaxonomyNode( "child3" );
    rootNode.addChildNode( child3 );
    System.out.println( rootNode.getValue( ) );
            
    System.out.println( "\n ------------ getDescendants( ) --------------------- " );
    List<ITaxonomyNode> descendants = rootNode.getDescendants();
    for (int i = 0; i < descendants.size(); i++)
    {
      ITaxonomyNode descend = descendants.get( i );
      System.out.println( descend.getValue() );
    }
            
    System.out.println( "\n ------------- getParents( ) ------------------------ " );
    List<ITaxonomyNode> parents = child3.getParents( );
    for (int i = 0; i < parents.size(); i++)
    {
      ITaxonomyNode parent = parents.get( i );
      System.out.println( parent.getValue() );
    }
            
    TaxonomyNode child4 = new TaxonomyNode( "child4" );
    child3.addChildNode( child4 );
    System.out.println( rootNode.getValue( ) );
            
    System.out.println( "\n ------------- getAncestors( ) ----------------------- " );
    List<ITaxonomyNode> ancestors = child4.getAncestors( );
    for (int i = 0; i < ancestors.size(); i++)
    {
      ITaxonomyNode ancestor = ancestors.get( i );
      System.out.println( ancestor.getValue() );
    }
            
    System.out.println( "\n ------------- getPaths( ) --------------------------- " );
    List<String> paths = child4.getPaths( );
    if (paths != null)
    {
      for (int i = 0; i < paths.size(); i++)
      {
        System.out.println( paths.get( i ) );
      }
    }
            
    System.out.println( "\n -------------- getRootNode( ) ----------------------- " );
    ITaxonomyNode root = child4.getRootNode( );
    System.out.println( root.getName( ) );
      
    System.out.println( "\n ------------- isAncestor( ) --------------------------- " );
    boolean rootIsAncestor = child4.isAncestor( root );
    System.out.println( "rootIsAncestor is of child4 = " + rootIsAncestor );
            
    System.out.println( "\n ------------- isDescendant( ) --------------------------- " );
    boolean child4IsDescendant = root.isDescendant( child4 );
    System.out.println( "child4 Is Descendant is of root = " + child4IsDescendant );
    System.out.println( "child4 Is Descendant is of child2 = " + child2.isDescendant( child4 ) );
  }

}
