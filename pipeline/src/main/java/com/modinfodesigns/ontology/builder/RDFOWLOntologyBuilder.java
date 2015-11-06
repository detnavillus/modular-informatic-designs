package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFOWLOntologyBuilder extends XMLTaxonomyBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( RDFOWLOntologyBuilder.class );
    
  private ArrayList<String> copyParentPropertyList;
    
  public void addParentPropertyToCopy( String parentPropertyToCopy )
  {
    System.out.println( "addParentPropertyToCopy = " + parentPropertyToCopy );
    if (copyParentPropertyList == null) copyParentPropertyList = new ArrayList<String>( );
    copyParentPropertyList.add( parentPropertyToCopy );
  }
    
  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    LOG.info( "buildTaxonomy: " );
      
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
          resolveChildObject( (DataObject)childProp, node, nodeMap );
        }
        else if (childProp instanceof PropertyList )
        {
          Iterator<IProperty> plprops = ((PropertyList)childProp).getProperties( );
          ArrayList<DataObject> plDobjs = new ArrayList<DataObject>( );
          while (plprops.hasNext( ) )
          {
            IProperty listProp = plprops.next( );
            if (listProp instanceof DataObject )
            {
                plDobjs.add( (DataObject)listProp );
            }
          }
          for (DataObject childobj : plDobjs )
          {
            resolveChildObject( childobj, node, nodeMap );
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
          LOG.error( "Error: can't add " + node.getName( ) + "!!!" );
        }
      }
    }
      
    System.out.println( "buildTaxonomy DONE!" );
    return rootNode;
  }
    
  private void resolveChildObject( DataObject childOb, TaxonomyNode node, HashMap<String,TaxonomyNode> nodeMap )
  {
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
    
  private void addObjects( PropertyList objects, HashMap<String,TaxonomyNode> nodeMap )
  {
    LOG.debug( "addObjects: " + objects );
    Iterator<IProperty> props = objects.getProperties( );
    while (props.hasNext( ) )
    {
      IProperty theProp = props.next( );
      if (theProp instanceof PropertyList ) {
        LOG.debug( "addObjects got a Property List!" );
      }
      else if (theProp instanceof DataObject ) {
        DataObject dobj = (DataObject)theProp;
        LOG.debug( "\n\n" + dobj.getValue( )  );
        TaxonomyNode taxoNode = TaxonomyNode.createTaxonomyNode( dobj );
        
        IProperty idProp = dobj.getProperty( "rdf:about" );
        if (idProp != null)
        {
          LOG.info( "Adding " + idProp.getValue( ) );
          taxoNode.setID( idProp.getValue( ) );
          nodeMap.put( taxoNode.getID( ), taxoNode );
                
          IProperty nameProp = dobj.getProperty( "rdfs:label" );
          if (nameProp != null)
          {
            if (nameProp instanceof PropertyList) {
                LOG.debug( " Name is a List!" );
                PropertyList nameL = (PropertyList)nameProp;
                Iterator<IProperty> nameLit = nameL.getProperties( );
                while (nameLit.hasNext( ) ) {
                    IProperty aName = nameLit.next( );
                    System.out.println( "  has name" + aName.getValue( ) );
                }
            }
            else if (nameProp instanceof DataObject ){
              LOG.debug( "Adding nameProp: " + nameProp );
              // get the StringListProperty "text"
              DataObject nameObj = (DataObject)nameProp;
              IProperty name = nameObj.getProperty( "text" );
              LOG.debug( "setting name = " + name.getValue( ) );
              taxoNode.setName( name.getValue( ) );
            }
          }
        }
      }
    }
      
    LOG.debug( "addObjects - DONE!" );
  }
    
  private void addParents( IProperty objects, TaxonomyNode childNode, HashMap<String,TaxonomyNode> nodeMap )
  {
    LOG.debug( "addParents " + objects + ", " + childNode + "," + nodeMap );
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
            LOG.debug( "propID = " + propID + " parentNode = " + parentNode );
            parentNode.addChildNode( childNode );
            copyParentProperties( parentNode, childNode );
          }
        }
      }
      else if (objects instanceof DataObject )
      {
        // System.out.println( "Its a DataObject : " + objects.getValue( ) );
        String propID = ((DataObject)objects).getProperty( "rdf:resource" ).getValue( );
        LOG.debug( "propID = " + propID );
        if (propID != null)
        {
          TaxonomyNode parentNode = nodeMap.get( propID );
          parentNode.addChildNode( childNode );
          copyParentProperties( parentNode, childNode );
        }
      }
    }
    catch ( TaxonomyException te )
    {
      LOG.error( "Error: can't add " + objects.getName( ) + "!!!" );
    }
  }
    
  private void copyParentProperties( TaxonomyNode parentNode, TaxonomyNode childNode )
  {
    if ( copyParentPropertyList != null )
    {
      for ( String propToCopy : copyParentPropertyList )
      {
        String parentProp = propToCopy;
        String childProp = parentProp;
        if (propToCopy.indexOf( ":" ) > 0)
        {
            parentProp = propToCopy.substring( 0, propToCopy.indexOf( ":" ));
            childProp  = propToCopy.substring( propToCopy.indexOf( ":" ) + 1 );
        }

        IProperty pProp = parentNode.getProperty( parentProp );
        if ( pProp != null )
        {
          if (pProp instanceof PropertyList)
          {
            PropertyList pl = (PropertyList)pProp;
            Iterator<IProperty> plit = pl.getProperties( );
            while (plit != null && plit.hasNext( ) )
            {
              IProperty pp = plit.next( );
 
              if (pp instanceof DataObject && childProp.indexOf( ":" ) > 0)
              {
                String chPropName = childProp.substring( 0, childProp.indexOf( ":" ));
                String innerProp = childProp.substring( childProp.indexOf(":") + 1 );
                DataObject innerOb = (DataObject)pp;
                IProperty targetProp = innerOb.getProperty( innerProp );
                System.out.println( "Adding child property " + chPropName + " = " + targetProp.getValue( ) );
                childNode.addProperty( new StringProperty( chPropName, targetProp.getValue( ) ) );
              }
              else if (pp instanceof DataObject && ((DataObject)pp).getProperty( "text" ) != null )
              {
                DataObject innerOb = (DataObject)pp;
                IProperty targetProp = innerOb.getProperty( "text" );
                System.out.println( "Adding child property " + childProp + " = " + targetProp.getValue( ) );
                childNode.addProperty( new StringProperty( childProp, targetProp.getValue( ) ) );
              }
              else
              {
                IProperty cp = pp.copy( );
                cp.setName( childProp );
                childNode.addProperty( cp );
                System.out.println( "Adding child property " + cp.getName() + " = " + cp.getValue( ) );
              }
            }
          }
          else
          {
            IProperty cProp = pProp.copy( );
            cProp.setName( childProp );
            childNode.setProperty( cProp );
            System.out.println( "Setting child property " + cProp.getName( ) + " = " + cProp.getValue( ) );
          }
        }
      }
    }
  }

  private void resolveResource( DataObject obj, IProperty resourceProp, TaxonomyNode taxoNode, HashMap<String,TaxonomyNode> nodeMap )
  {
    if (resourceProp == null) {
      LOG.error( "resourceProp is NULL!" );
      return;
    }
    String propID = (resourceProp instanceof StringProperty) ? resourceProp.getValue( ) : null;
    if (propID == null) return;
    
    LOG.debug( taxoNode + ": resolveResource: " + propID + " object name = " + obj.getName( ) );
    TaxonomyNode refNode = nodeMap.get( propID );
    if (refNode != null && refNode.getID( ) != null ) {
      taxoNode.removeDataObject( refNode.getID( ) );
      LOG.debug( "adding delegate" );
      DataObjectDelegate delegate = new DataObjectDelegate( refNode );
      delegate.setName( obj.getName( ) );
      // need to set its ID to ref.getID( ) ???
        System.out.println( "taxo node " + taxoNode.getName( ) + " add delegate " + delegate.getName( )  + " = " + obj.getName( ) );
      taxoNode.addProperty( delegate );
    }
    else {
      System.out.println( "Cannot resolve resource: " + propID );
    }
  }
        
}
