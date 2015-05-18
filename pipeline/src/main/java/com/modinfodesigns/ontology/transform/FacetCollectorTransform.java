package com.modinfodesigns.ontology.transform;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.builder.ITaxonomyBuilder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetCollectorTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FacetCollectorTransform.class );

  private ITaxonomyBuilder taxoBuilder;
  private String taxoBuilderName;
	
  private boolean initialized = false;
	
  private String inputProperty = "Path";
	
  private String isFacetProperty = "isFacet";
	
  private String isFacetTrue = "true";
	
  private String prependPath = "/root";
	
  private HashMap<String,List<String>> facetToPathsMap = new HashMap<String,List<String>>( );
	
  private ArrayList<String> facetPaths = new ArrayList<String>( );
	
  public void addTaxonomyBuilderRef( String taxoBuilderName )
  {
    setTaxonomyBuilderRef( taxoBuilderName );
  }
	
  public void setTaxonomyBuilderRef( String taxoBuilderName )
  {
    this.taxoBuilderName = taxoBuilderName;
  }
	
  public void setTaxonomyBuilder( ITaxonomyBuilder taxoBuilder )
  {
    this.taxoBuilder = taxoBuilder;
  }
	
  private void init( )
  {
    LOG.debug( "init" );
		
    if (initialized)
    {
      LOG.debug( "already initialized" );
      return;
    }
		
    synchronized( this )
    {
      if (initialized) return;
		    
      ITaxonomyBuilder taxoBuilder = getTaxonomyBuilder( );
		
      if (taxoBuilder != null)
      {
        ITaxonomyNode taxo = taxoBuilder.buildTaxonomy( );
        List<ITaxonomyNode> descendants = taxo.getDescendants( );
        if (descendants != null)
        {
          for (int i = 0; i < descendants.size( ); i++ )
          {
            ITaxonomyNode node = descendants.get( i );
            IProperty isFacetProp = node.getProperty( isFacetProperty );
            if (isFacetProp != null && isFacetProp.getValue().equals( isFacetTrue ))
            {
              LOG.debug( "Found Facet Prop: " + node.getName( ) );
						
              List<String> paths = node.getPaths( );
              if (paths != null)
              {
                facetPaths.addAll( paths );
							
                // Add to facet to paths map
                String facetName = node.getName( );
                List<String> mappedPaths = facetToPathsMap.get( facetName );
                if (mappedPaths == null)
                {
                  facetToPathsMap.put( facetName, paths );
                }
                else
                {
                  mappedPaths.addAll( paths );
                }
              }
            }
          }
        }
      }
	    	
      initialized = true;
    }
		
    LOG.debug( "init DONE." );
  }
	
  private ITaxonomyBuilder getTaxonomyBuilder(  )
  {
    if (taxoBuilder != null) return taxoBuilder;
		
    if (taxoBuilderName == null) return null;
		
    synchronized ( this )
    {
      if (taxoBuilder != null) return taxoBuilder;
        
      ApplicationManager appMan = ApplicationManager.getInstance( );
      this.taxoBuilder = (ITaxonomyBuilder)appMan.getApplicationObject( taxoBuilderName, "TaxonomyBuilder" );

      return taxoBuilder;
    }
  }

  @Override
  public IProperty transform( IProperty input )
                              throws PropertyTransformException
  {
    LOG.debug( "transform( )..." );
		
    init( );
		
    // return a PropertyList of facets ...
    if (input == null) return null;
		
    IProperty outputProp = null;
    // facet paths should not end in "/"
    String[] paths = getPaths( input );
    if (paths == null) return input;
		
    for (int p = 0; p < paths.length; p++)
    {
      String nodePath = prependPath + paths[p];
      LOG.debug( "Looking for facet for " + nodePath );
      for ( int fp = 0; fp < facetPaths.size( ); fp++)
      {
        String facetPath = facetPaths.get( fp );
        LOG.debug( "Checking facetPath " + facetPath );
			
        if ( nodePath.startsWith( facetPath ) && nodePath.length() > ( facetPath.length() + 1 ) )
        {
          LOG.debug( "Found Facet!" );
          String facetValue = new String( nodePath.substring( facetPath.length() + 1 ) );
          String facetField = new String( facetPath.substring( facetPath.lastIndexOf( "/" ) + 1 ) );
				
          StringProperty facetProp = new StringProperty( facetField, facetValue );
          LOG.debug( "Output prop is now: " + outputProp );
          if (outputProp == null)
          {
            outputProp = facetProp;
          }
          else if (outputProp instanceof StringProperty )
          {
            LOG.debug( "Creating DataObject" );
            DataObject dobj = new DataObject( );
            dobj.setName( "Facets" );
            dobj.addProperty( outputProp );
            dobj.addProperty( facetProp );
            outputProp = dobj;
          }
          else if (outputProp instanceof DataObject )
          {
            LOG.debug( "Adding to PropertyList " );
            ((DataObject)outputProp).addProperty( facetProp );
          }
        }
      }
    }
		
    return outputProp;
  }
	
  private String[] getPaths( IProperty input )
  {
    if (input == null) return null;
		
    if (input instanceof PropertyList)
    {
      PropertyList pList = (PropertyList)input;
			
      String[] paths = new String[ pList.size( ) ];
      for ( int i = 0; i < pList.size(); i++ )
      {
        IProperty prop = pList.getProperty( i );
        LOG.debug( "got prop = " + prop.getValue( ) );
        paths[i] = prop.getValue( );
      }
			
      return paths;
    }
    else
    {
      String[] paths = new String[1];
      paths[0] = input.getValue( );
      return paths;
    }
  }
	
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder( )..." );
		
    if (input == null) return null;
		
    init( );
    
    IProperty pathProp = input.getProperty( inputProperty );

    String[] paths = getPaths( pathProp );
    if (paths == null) return input;
		
    for (int p = 0; p < paths.length; p++)
    {
      String nodePath = prependPath + paths[p];
      LOG.debug( "Looking for facet for " + nodePath );
    		
      for ( int fp = 0; fp < facetPaths.size( ); fp++)
      {
        String facetPath = facetPaths.get( fp );

        LOG.debug( "Checking facetPath " + facetPath );
			
        if (nodePath.startsWith( facetPath ) && nodePath.length() > (facetPath.length() + 1) )
        {
          String facetValue = new String( nodePath.substring( facetPath.length() + 1 ) );
          String facetField = new String( facetPath.substring( facetPath.lastIndexOf( "/" ) + 1 ) );
				
          facetField = StringTransform.replaceSubstring( facetField, " ", "_" );
          LOG.debug( "Adding facet '" + facetField + "' = '" + facetValue + "'" );
          StringProperty facetProp = new StringProperty( facetField, facetValue );
          input.addProperty( facetProp );
        }
      }
    }
		
    return input;
  }

  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener )
                              throws PropertyTransformException
  {

  }

}
