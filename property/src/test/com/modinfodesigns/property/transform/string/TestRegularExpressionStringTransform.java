package com.modinfodesigns.property.transform.string;

import junit.framework.TestCase;

public class TestRegularExpressionStringTransform extends TestCase
{

  public void testRegularExpressionStringTransform( ) throws StringTransformException
  {
    String foobar = "foo is always related to bar.";
        
    RegularExpressionStringTransform rest = new RegularExpressionStringTransform( );
    // change 'foo' to 'fu'
    rest.setRegularExpression( "foo" );
    rest.setOutputPattern( "fu" );
        
    String result = rest.transformString( foobar );
    System.out.println( result );
        
    String someHtml = "This is a <a href=\"foobar.com\">a hyperlink</a> Remove the hyperlink. "
                    + " Here is another hyperlink <a href=\"www.doofus.com\">not me either</a> Removed it too.";
    rest.setRegularExpression( "<[aA]\\shref=[^<]*</[aA]>" );
    rest.setOutputPattern( "aha" );
        
    result = rest.transformString( someHtml );
    System.out.println( result );
  }

}
