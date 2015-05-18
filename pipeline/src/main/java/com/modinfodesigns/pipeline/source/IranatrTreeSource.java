package com.modinfodesigns.pipeline.source;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.utils.DOMMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IranatrTreeSource extends XMLDataSource 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IranatrTreeSource.class );

  private String parentPropName = "Parent";
  private String name;
	
  public void setName( String name )
  {
    this.name = name;
  }
	
  public String getName( )
  {
    return this.name;
  }
	
  public void setParentPropertyName( String parentPropName )
  {
    this.parentPropName = parentPropName;
  }
    
  public void processElement( IDataList dList, Element dataEl )
  {
    LOG.debug( "processElement( )..." );
    	
    NodeList treeNodeLst = dataEl.getElementsByTagName( "TreeNode" );
    if ( treeNodeLst != null && treeNodeLst.getLength() > 0 )
    {
      for ( int i = 0; i < treeNodeLst.getLength(); i++ )
      {
        Element treeNodeEl = (Element)treeNodeLst.item( i );
        DataObject dobj = new DataObject( );
        dList.addDataObject( dobj );
    			
        dobj.setName( treeNodeEl.getAttribute( "name" ) );
        dobj.setID( treeNodeEl.getAttribute( "ID" ));
        LOG.debug( "Got TreeNode: " + dobj.getName( ) );
    			
        NodeList parentLst = treeNodeEl.getElementsByTagName( "Parent" );
        if (parentLst != null && parentLst.getLength() > 0)
        {
          Element parentEl = (Element)parentLst.item( 0 );
          String parentID = parentEl.getAttribute( "ID" );
          if ( parentID.equals( "Root" ) == false )
          {
            StringProperty parentProp = new StringProperty( parentPropName, parentID );
            dobj.addProperty( parentProp );
          }
        }
    			
        NodeList attrLst = treeNodeEl.getElementsByTagName( "Attribute" );
        if ( attrLst != null && attrLst.getLength() > 0)
        {
          for ( int j = 0; j < attrLst.getLength(); j++ )
          {
            Element attrEl = (Element)attrLst.item( j );
            String name = attrEl.getAttribute( "name" );
    					
            NodeList valueLst = attrEl.getElementsByTagName( "Value" );
            if (valueLst != null && valueLst.getLength() > 0)
            {
              for (int v = 0; v < valueLst.getLength( ); v++)
              {
                Element valueEl = (Element)valueLst.item( v );
                String value = DOMMethods.getText( valueEl );
                StringProperty prop = new StringProperty( name, value );
                dobj.addProperty( prop );
              }
            }
          }
        }
      }
    }
  }

}
