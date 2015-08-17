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
    assertEquals( rootNode.getValue( ), "{\"children\":[{\"name\":\"child1\",\"path\":[\"/root/child1\"]},{\"name\":\"child2\",\"path\":[\"/root/child2\"]},{\"name\":\"child3\",\"path\":[\"/root/child3\"]}]}");

    List<ITaxonomyNode> descendants = rootNode.getDescendants();
    assertEquals( descendants.size( ), 3 );

    assertEquals( descendants.get( 0 ).getValue( ), "{\"name\":\"child1\",\"path\":[\"/root/child1\"]}" );
    assertEquals( descendants.get( 1 ).getValue( ), "{\"name\":\"child2\",\"path\":[\"/root/child2\"]}" );
    assertEquals( descendants.get( 2 ).getValue( ), "{\"name\":\"child3\",\"path\":[\"/root/child3\"]}" );
      
    List<ITaxonomyNode> parents = child3.getParents( );
    assertEquals( parents.size( ), 1 );
    assertEquals( parents.get( 0 ).getName( ), "root" );
            
    TaxonomyNode child4 = new TaxonomyNode( "child4" );
    child3.addChildNode( child4 );
    assertEquals( rootNode.getValue( ), "{\"children\":[{\"name\":\"child1\",\"path\":[\"/root/child1\"]},{\"name\":\"child2\",\"path\":[\"/root/child2\"]},{\"name\":\"child3\",\"path\":[\"/root/child3\"],\"children\":[{\"name\":\"child4\",\"path\":[\"/root/child3/child4\"]}]}]}" );
            

    List<ITaxonomyNode> ancestors = child4.getAncestors( );
    assertEquals( ancestors.size( ), 2 );
    assertEquals( ancestors.get( 0 ).getName( ), "child3" );
    assertEquals( ancestors.get( 1 ).getName( ), "root" );

    List<String> paths = child4.getPaths( );
    assertEquals( paths.size( ), 1 );
    assertEquals( paths.get( 0 ), "/root/child3/child4" );

    ITaxonomyNode root = child4.getRootNode( );
    assertEquals( root.getName( ), "root" );
      
    boolean rootIsAncestor = child4.isAncestor( root );
    assertTrue( rootIsAncestor );
      
    boolean child4IsDescendant = root.isDescendant( child4 );
    assertTrue( child4IsDescendant );
    assertFalse( child2.isDescendant( child4 ) );
  }

}
