package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.app.SessionManager;
import com.modinfodesigns.app.UserSession;

/**
 *  Transforms file, ftp or http URL into a String representation.
 */

import com.modinfodesigns.network.http.HttpClientWrapper;
import com.modinfodesigns.utils.FileMethods;

import com.modinfodesigns.network.ftp.FTPUtilities;

public class URLStringTransform implements IStringTransform
{
  private String securityManagerName;

  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    return transformString( "", inputString );
  }

  @Override
  public String transformString( String sessionID, String inputString ) throws StringTransformException
  {
    if (inputString == null) return null;
		
    if (inputString.startsWith( "file:///" ))
    {
      String fileName = new String( inputString.substring( "file:///".length() ));
      String file = FileMethods.readFile( fileName );
			
      if (file == null)
      {
        throw new StringTransformException( "File not found!" );
      }
			
      return file;
    }
    else if ( inputString.startsWith( "http:") || inputString.startsWith( "https:"))
    {
      return HttpClientWrapper.executeGet( inputString );
    }
    else if (inputString.startsWith( "ftp:" ))
    {
      return FTPUtilities.getFTPFile( inputString, getUserCredentials( sessionID ) );
    }
    else
    {
      throw new StringTransformException( "Cannot transform " + inputString );
    }
  }
	
  private IUserCredentials getUserCredentials( String sessionID )
  {
    UserSession userSession = SessionManager.getInstance().getUserSession( sessionID );
    return (userSession != null) ? userSession.getUserCredentials( ) : null;
  }

}
