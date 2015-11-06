package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.transform.json.JSONParserTransform;

import junit.framework.TestCase;

public class TestNameValueObjectTransform extends TestCase
{
  public void testNameValueTransform( ) throws PropertyTransformException
  {
    DataObject dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "name", "foobar" ));
    dobj.addProperty( new StringProperty( "value", "bazbat" ));
      
    NameValueObjectTransform nvot = new NameValueObjectTransform( "name", "value"  );
    IProperty output = nvot.transform( dobj );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>foobar</Name><Value>bazbat</Value></Property>" );
  }
    
  public void testNameValueTransformProps(  ) throws PropertyTransformException
  {
    DataObject parent = new DataObject( );
      
    DataObject child = new DataObject( );
    child.setName( "child" );
    child.setProperty( new StringProperty( "name", "foobar" ));
    child.addProperty( new StringProperty( "value", "bazbat" ));
    
    parent.addProperty( child );
      
    child = new DataObject( );
    child.setName( "child" );
    child.setProperty( new StringProperty( "name", "metatag" ));
    child.setProperty( new StringProperty( "value", "whatziz" ));
    parent.addProperty( child );
      
    //assertEquals( parent.getValue( IProperty.JSON_FORMAT ), "{\"child\":\"[{\"name\":\"foobar\",\"value\":\"bazbat\"},{\"name\":\"metatag\",\"value\":\"whatziz\"}]\"}" );
      
    SequentialPropertyTransform spt = new SequentialPropertyTransform( );
    spt.addPropertyTransform( new NameValueObjectTransform( "name", "value"  ) );
    spt.setInputProperty( "child" );
      
    spt.transformPropertyHolder( parent );
    //assertEquals( parent.getValue( IProperty.JSON_FORMAT ), "{\"child\":{\"foobar\":\"bazbat\",\"metatag\":\"whatziz\"}}" );

  }
    
  public void testPropertyListProps(  ) throws PropertyTransformException {
        
    PropertyList pl = new PropertyList( );
    DataObject dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "Category", "ORG" ));
    dobj.addProperty( new StringProperty( "Name", "Microsoft" ));
    pl.addProperty( dobj );
    
    dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "Category", "PER" ));
    dobj.addProperty( new StringProperty( "Name", "Andrew" ));
    pl.addProperty( dobj );
      
    dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "Category", "LOC" ));
    dobj.addProperty( new StringProperty( "Name", "New York" ));
    pl.addProperty( dobj );
      
    NameValueObjectTransform nvot = new NameValueObjectTransform( "Category", "Name" );
    IProperty output = nvot.transform( pl );
    //assertEquals( output.getValue( ), "{\"LOC\":\"New York\",\"ORG\":\"Microsoft\",\"PER\":\"Andrew\"}" );
  }
    
  public void testNestedPropertyList( ) throws PropertyTransformException {
    PropertyList pl = new PropertyList( );
    pl.setName( "NE" );
    DataObject dobj = new DataObject( );
    dobj.setName( "NE" );
    dobj.addProperty( new StringProperty( "Category", "ORG" ));
    dobj.addProperty( new StringProperty( "Name", "Microsoft" ));
    pl.addProperty( dobj );
        
    dobj = new DataObject( );
    dobj.setName( "NE" );
    dobj.addProperty( new StringProperty( "Category", "PER" ));
    dobj.addProperty( new StringProperty( "Name", "Andrew" ));
    pl.addProperty( dobj );
        
    dobj = new DataObject( );
    dobj.setName( "NE" );
    dobj.addProperty( new StringProperty( "Category", "LOC" ));
    dobj.addProperty( new StringProperty( "Name", "New York" ));
    pl.addProperty( dobj );

    DataObject parent = new DataObject( );
    parent.setName( "parent" );
    parent.addProperty( pl );
    NestedPropertyTransform npt = new NestedPropertyTransform( );
    npt.setNestedProperty( "NE" );
    npt.addPropertyTransform( new NameValueObjectTransform( "Category", "Name" ) );
    npt.transformPropertyHolder( parent );
    
    //assertEquals( parent.getValue( ), "{\"NE\":{\"LOC\":\"New York\",\"ORG\":\"Microsoft\",\"PER\":\"Andrew\"}}" );
      
    CopyPropertyTransform cpt = new CopyPropertyTransform( );
    cpt.setFrom( "NE/LOC" );
    cpt.setTo( "NE_LOC" );
    cpt.transformPropertyHolder( parent );
      
    cpt = new CopyPropertyTransform( );
    cpt.setFrom( "NE/ORG" );
    cpt.setTo( "NE_ORG" );
    cpt.transformPropertyHolder( parent );
      
    cpt = new CopyPropertyTransform( );
    cpt.setFrom( "NE/PER" );
    cpt.setTo( "NE_PER" );
    cpt.transformPropertyHolder( parent );
    parent.removeProperty( "NE" );
      
    //assertEquals( parent.getValue( ), "{\"NE_PER\":\"Andrew\",\"NE_LOC\":\"New York\",\"NE_ORG\":\"Microsoft\"}" );
  }
    
 /* public void testWJSONParser( ) throws PropertyTransformException {
    DataObject dobj = new JSONParserTransform( ).createDataObject( nested_objects );
    //assertEquals( dobj.getValue( IProperty.JSON_FORMAT ), "{\"Content\":\"[{\"Type\":\"Subject\",\"Paragraphs\":\"[{\"Type\":\"[\"text\",\"Subject\"]\",\"Content\":\"RE: Hello Ben\",\"html\":\"RE: Hello <span class=\\\"NE PER\\\" title=\\\"NE PER\\\">BEN</span>\"}]\"},{\"Type\":\"Body\",\"Paragraphs\":\"[{\"Type\":\"[\"text\",\"default\",\"text\",0.6317063746746246]\",\"Content\":\"I am doing well.\",\"html\":\"I am doing well.\"}]\"}]\"}" );
    
    NestedPropertyTransform npt = new NestedPropertyTransform( );
    npt.setNestedProperty( "Content" );
    npt.addPropertyTransform( new NameValueObjectTransform( "Type", "Paragraphs" ) );
    npt.transformPropertyHolder( dobj );

    //assertEquals( dobj.getValue( IProperty.JSON_FORMAT ), "{\"Content\":{\"Body\":\"[{\"Type\":\"[\"text\",\"default\",\"text\",0.6317063746746246]\",\"Content\":\"I am doing well.\",\"html\":\"I am doing well.\"}]\",\"Subject\":\"[{\"Type\":\"[\"text\",\"Subject\"]\",\"Content\":\"RE: Hello Ben\",\"html\":\"RE: Hello <span class=\\\"NE PER\\\" title=\\\"NE PER\\\">BEN</span>\"}]\"}}" );
  } */
    
    
  private static String nested_objects = "{\"Content\": ["
                                       + "    {"
                                       + "      \"Type\": \"Subject\","
                                       + "      \"Paragraphs\": ["
                                       + "        {"
                                       + "          \"Content\": \"RE: Hello Ben\","
                                       + "          \"html\": \"RE: Hello <span class=\\\"NE PER\\\" title=\\\"NE PER\\\">BEN</span>\","
                                       + "          \"Type\": [\"text\", \"Subject\"]"
                                       + "        }"
                                       + "      ]"
                                       + "    },"
                                       + "    {"
                                       + "      \"Type\": \"Body\","
                                       + "      \"Paragraphs\": ["
                                       + "        {"
                                       + "          \"Content\": \"I am doing well.\","
                                       + "          \"html\": \"I am doing well.\","
                                       + "          \"Type\": [\"text\", \"default\", \"text\", 0.6317063746746246]"
                                       + "        }"
                                       + "      ]"
                                       + "    }"
                                       + "  ]"
                                       + "}";
}