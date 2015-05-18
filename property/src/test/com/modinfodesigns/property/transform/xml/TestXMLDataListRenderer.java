package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import junit.framework.TestCase;

public class TestXMLDataListRenderer extends TestCase
{

  public void testXMLDataListRenderer( )
  {
    DataList dList = new DataList( );
		
    DataObject dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "cats", "meow" ));
    dobj.addProperty( new StringProperty( "dogs", "bark" ));
    dList.addDataObject( dobj );
        
    dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "fish", "swim" ));
    dobj.addProperty( new IntegerProperty( "age", 23 ));
    dList.addDataObject( dobj );
        
    XMLDataListRenderer xmlDLR = new XMLDataListRenderer( );
    System.out.println( xmlDLR.renderDataList( dList ) );
        
    xmlDLR.setOutputType( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE );
    System.out.println( xmlDLR.renderDataList( dList ) );
    
    xmlDLR.setOutputType( XMLConstants.TAGNAME_IS_FIELD_STYLE );
    System.out.println( xmlDLR.renderDataList( dList ) );
  }

}
