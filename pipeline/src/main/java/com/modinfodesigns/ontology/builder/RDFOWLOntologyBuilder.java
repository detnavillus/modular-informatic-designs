package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.IProperty;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFOWLOntologyBuilder extends XMLTaxonomyBuilder
{
  private transient static final Logger Log = LoggerFactory.getLogger( RDFOWLOntologyBuilder.class );
    
  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    Log.debug( "buildTaxonomy: " + context.getValue( IProperty.JSON_FORMAT ) );
      
    // pass 1 set ID = rdf:about
    HashMap<String,TaxonomyNode> nodeMap = new HashMap<String,TaxonomyNode>( );
      
    TaxonomyNode rootNode = new TaxonomyNode( );
    rootNode.setName( context.getName( ) );
      
    // get owl:Class children
    PropertyList owlClasses = (PropertyList)context.getProperty( "owl:Class" );
    addObjects( owlClasses, nodeMap );
      
    // get owl:NamedIndividual
    PropertyList owlIndividuals = (PropertyList)context.getProperty( "owl:NamedIndividual" );
    addObjects( owlIndividuals, nodeMap );
      
    // pass 2: build hierarchy with rdfs:subclassof and rdf:type
    // resolve links of rdf:resource
    for (TaxonomyNode node : nodeMap.values( ) )
    {
      // get all dataObjects of rdfs:subClassOf, find the Parent Node - call addChild on Parent Node
      IProperty subClassOfs = node.getProperty( "rdfs:subClassOf" );
      if (subClassOfs != null) addParents( subClassOfs, node, nodeMap );
        
      // for all data objects name rdf:type - find the Parent Node
      IProperty typeOfs = node.getProperty( "rdf:type" );
      if (typeOfs != null) addParents( typeOfs, node, nodeMap );

      // for data objects that have rdf:resource properties
      // lookup the DataObject set it as the DataObjectDelegate object for this property
      // rdf:resource
      Iterator<IProperty> childPropIt = node.getProperties( );
      while (childPropIt != null && childPropIt.hasNext( ) )
      {
        IProperty childProp = childPropIt.next( );
        if (childProp instanceof DataObject)
        {
          DataObject childOb = (DataObject)childProp;
          if (!childOb.getName().equals( "rdfs:subClassOf" ) && !childOb.getName().equals( "rdf:type" ))
          {
            IProperty rdfResource = childOb.getProperty( "rdf:resource" );
            if ( rdfResource != null )
            {
              System.out.println( "resolving " + childOb.getName( ) );
              resolveResource( childOb, rdfResource, node, nodeMap );
            }
          }
        }
      }
    }
      
    for (TaxonomyNode node : nodeMap.values( ) )
    {
      if (node.isRootNode( ) )
      {
        try
        {
          rootNode.addChildNode( node );
        }
        catch (TaxonomyException te )
        {
          Log.error( "Error: can't add " + node.getName( ) + "!!!" );
        }
      }
    }
      
    return rootNode;
  }
    
  private void addObjects( PropertyList objects, HashMap<String,TaxonomyNode> nodeMap )
  {

    Iterator<IProperty> props = objects.getProperties( );
    while (props.hasNext( ) )
    {
      DataObject dobj = (DataObject)props.next( );
      Log.debug( "\n\n" + dobj.getValue( )  );
      TaxonomyNode taxoNode = TaxonomyNode.createTaxonomyNode( dobj );
        
      IProperty idProp = dobj.getProperty( "rdf:about" );
      if (idProp != null)
      {
        taxoNode.setID( idProp.getValue( ) );
        nodeMap.put( taxoNode.getID( ), taxoNode );
                
        IProperty nameProp = dobj.getProperty( "rdfs:label" );
        if (nameProp != null)
        {
          // get the StringListProperty "text"
          DataObject nameObj = (DataObject)nameProp;
          IProperty name = nameObj.getProperty( "text" );
          taxoNode.setName( name.getValue( ) );
        }
      }
    }
      
    Log.debug( "addObjects - DONE!" );
  }
    
  private void addParents( IProperty objects, TaxonomyNode childNode, HashMap<String,TaxonomyNode> nodeMap )
  {
    Log.debug( "addParents " + objects + ", " + childNode + "," + nodeMap );
    try
    {
      if (objects instanceof PropertyList)
      {
        // System.out.println( "Its a PropertyList!" );
        PropertyList pList = (PropertyList)objects;
        Iterator<IProperty> propIt = pList.getProperties( );
        while ( propIt != null && propIt.hasNext( ) )
        {
          DataObject prop = (DataObject)propIt.next( );
          String propID = prop.getProperty( "rdf:resource" ).getValue( );
          if (propID != null )
          {
            TaxonomyNode parentNode = nodeMap.get( propID );
            Log.debug( "propID = " + propID + " parentNode = " + parentNode );
            parentNode.addChildNode( childNode );
          }
        }
      }
      else if (objects instanceof DataObject )
      {
        // System.out.println( "Its a DataObject : " + objects.getValue( ) );
        String propID = ((DataObject)objects).getProperty( "rdf:resource" ).getValue( );
        Log.debug( "propID = " + propID );
        if (propID != null)
        {
          TaxonomyNode parentNode = nodeMap.get( propID );
          parentNode.addChildNode( childNode );
        }
      }
    }
    catch ( TaxonomyException te )
    {
      Log.error( "Error: can't add " + objects.getName( ) + "!!!" );
    }
  }

  private void resolveResource( DataObject obj, IProperty resourceProp, TaxonomyNode taxoNode, HashMap<String,TaxonomyNode> nodeMap )
  {
    String propID = resourceProp.getValue( );
    TaxonomyNode refNode = nodeMap.get( propID );
    taxoNode.removeDataObject( refNode.getID( ) );
    DataObjectDelegate delegate = new DataObjectDelegate( refNode );
    delegate.setName( obj.getName( ) );
    // need to set its ID to ref.getID( ) ???
    taxoNode.addProperty( delegate );
  }
        
}
