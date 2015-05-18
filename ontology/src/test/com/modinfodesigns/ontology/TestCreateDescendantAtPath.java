package com.modinfodesigns.ontology;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;

import java.util.List;

import junit.framework.TestCase;

public class TestCreateDescendantAtPath extends TestCase
{
  public void testCreateDescendantAtPath( ) throws TaxonomyException
  {
    TaxonomyNode rootNode = new TaxonomyNode( "root" );

    TaxonomyNode nodeCalledSue = rootNode.createDescendantAtPath( "/should/create/node/called/Sue" );
    System.out.println( nodeCalledSue.getName( ) );

    TaxonomyNode calledNode = (TaxonomyNode)rootNode.getDescendantNode( "/root/should/create/node/called" );
    System.out.println( calledNode.getName( ) );
      
    calledNode = (TaxonomyNode)rootNode.getDescendantNode( "/should/create/node/called" );
    System.out.println( calledNode.getName( ) );
            
    List<ITaxonomyNode> sueParents = nodeCalledSue.getParents();
    System.out.println( sueParents.size( ) );
  }

}
