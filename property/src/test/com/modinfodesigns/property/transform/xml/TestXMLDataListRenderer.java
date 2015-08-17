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
    assertEquals( xmlDLR.renderDataList( dList ), rendered );
        
    xmlDLR.setOutputType( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE );
    assertEquals( xmlDLR.renderDataList( dList ), field_type_rendered );
      
    xmlDLR.setOutputType( XMLConstants.TAGNAME_IS_FIELD_STYLE );
    assertEquals( xmlDLR.renderDataList( dList ), tag_is_field_rendered );
  }
    
  private final String rendered = "<ResultSet><Result><Field><Name>dogs.Length</Name><Value>4</Value></Field><Field><Name>cats</Name><Value>meow</Value></Field><Field><Name>cats.Length</Name><Value>4</Value></Field><Field><Name>dogs</Name><Value>bark</Value></Field><Field><Name>cats.MaximumLength</Name><Value></Value></Field><Field><Name>dogs.MaximumLength</Name><Value></Value></Field></Result><Result><Field><Name>fish.MaximumLength</Name><Value></Value></Field><Field><Name>fish.Length</Name><Value>4</Value></Field><Field><Name>fish</Name><Value>swim</Value></Field><Field><Name>age</Name><Value>23</Value></Field></Result></ResultSet>";
    
  private final String field_type_rendered = "<ResultSet><Result><Field name=\"dogs.Length\" type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</Field><Field name=\"cats\" type=\"com.modinfodesigns.property.string.StringProperty\">meow</Field><Field name=\"cats.Length\" type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</Field><Field name=\"dogs\" type=\"com.modinfodesigns.property.string.StringProperty\">bark</Field><Field name=\"cats.MaximumLength\" type=\"null\"></Field><Field name=\"dogs.MaximumLength\" type=\"null\"></Field></Result><Result><Field name=\"fish.MaximumLength\" type=\"null\"></Field><Field name=\"fish.Length\" type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</Field><Field name=\"fish\" type=\"com.modinfodesigns.property.string.StringProperty\">swim</Field><Field name=\"age\" type=\"com.modinfodesigns.property.quantity.IntegerProperty\">23</Field></Result></ResultSet>";
    
  private final String tag_is_field_rendered = "<ResultSet><Result><dogs.Length type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</dogs.Length><cats type=\"com.modinfodesigns.property.string.StringProperty\">meow</cats><cats.Length type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</cats.Length><dogs type=\"com.modinfodesigns.property.string.StringProperty\">bark</dogs><cats.MaximumLength type=\"null\"></cats.MaximumLength><dogs.MaximumLength type=\"null\"></dogs.MaximumLength></Result><Result><fish.MaximumLength type=\"null\"></fish.MaximumLength><fish.Length type=\"com.modinfodesigns.property.quantity.IntegerProperty\">4</fish.Length><fish type=\"com.modinfodesigns.property.string.StringProperty\">swim</fish><age type=\"com.modinfodesigns.property.quantity.IntegerProperty\">23</age></Result></ResultSet>";

}
