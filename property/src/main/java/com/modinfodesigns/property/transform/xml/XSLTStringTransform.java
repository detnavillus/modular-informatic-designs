package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.transform.string.IStringTransform;
import com.modinfodesigns.property.transform.string.StringTransformException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.StringWriter;
import java.io.StringReader;

import java.util.HashMap;

/**
 * Transforms an XML String using an XSLT transform.
 * 
 * @author Ted Sullivan
 *
 */
public class XSLTStringTransform implements IStringTransform
{
  private Templates xsltTemplate;
	
  private String xsltFile;
	
  private static HashMap<String,Templates> templatesMap = new HashMap<String,Templates>( );
	
  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    Templates theTemplate = getTemplate( );
    if (theTemplate == null)
    {
      return inputString;
    }

    try
    {
      Transformer xFormer = theTemplate.newTransformer( );
      StringWriter strWriter = new StringWriter( );
        
      StreamSource source = new StreamSource(new StringReader( inputString ));
      StreamResult result = new StreamResult( strWriter );

      xFormer.transform( source, result );

      return strWriter.toString( );
    }
    catch (Exception e)
    {
        	
    }
        
    return null;
  }

  @Override
  public String transformString(String sessionID, String inputString) throws StringTransformException
  {
    return transformString( inputString );
  }
	
  public void setXSLTString( String xsltString )
  {
    try
    {
      TransformerFactory tFac = TransformerFactory.newInstance( );
      StreamSource ss = new StreamSource( new StringReader( xsltString ) );
			
      this.xsltTemplate = tFac.newTemplates( ss );
    }
    catch ( Exception e )
    {
			
    }
  }
	
  public void setXSLTFile( String xsltFile )
  {
    this.xsltFile = xsltFile;
		
    Templates theTemplate = templatesMap.get( xsltFile );
    if (theTemplate == null)
    {
      try
      {
        TransformerFactory tFac = TransformerFactory.newInstance( );
        StreamSource ss = new StreamSource( new File( xsltFile ) );

        theTemplate = tFac.newTemplates( ss );
        templatesMap.put( xsltFile, theTemplate );
      }
      catch (Exception e )
      {
      }
    }
  }
	
  private Templates getTemplate( )
  {
    if (xsltTemplate != null) return xsltTemplate;
		
    if (xsltFile != null)
    {
      return templatesMap.get( xsltFile );
    }
		
    return null;
  }

}
