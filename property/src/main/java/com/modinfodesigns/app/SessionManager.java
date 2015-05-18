package com.modinfodesigns.app;

import com.modinfodesigns.security.IUserCredentials;
import com.modinfodesigns.security.ISecurityManager;
import com.modinfodesigns.security.ILoginHandler;

import com.modinfodesigns.network.http.HttpRequestData;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import java.util.HashMap;

public class SessionManager implements HttpSessionListener
{
    private static SessionManager sessManager;
    
    private HashMap<String,UserSession> currentUsers = new HashMap<String,UserSession>( );
    
    
    public static SessionManager getInstance( )
    {
    	if (sessManager != null) return sessManager;
    	
    	sessManager = new SessionManager( );
    	return sessManager;
    }
    
    public UserSession getUserSession( HttpRequestData httpRequest, ILoginHandler loginHandler, String securityManagerName )
    {
    	UserSession cachedSession = getUserSession( httpRequest.getSessionID( ) );
    	if (cachedSession != null) return cachedSession;
    	
    	IUserCredentials userCreds = (loginHandler != null) ? loginHandler.getUserCredentials( httpRequest ) : null;
    	UserSession userSess = null;
    	boolean validUser = true;
    	
    	// Try to login etc...
    	if (securityManagerName != null)
    	{
    		ISecurityManager securityManager = getSecurityManager( securityManagerName );
    		validUser = (securityManager != null && securityManager.login( userCreds ));
    	}

        if ( validUser )
    	{
    		userSess = new UserSession( userCreds );
    		currentUsers.put( httpRequest.getSessionID(), userSess );
    	}
    	

    	return userSess;
    }
    
    public UserSession getUserSession( String sessionID )
    {
    	return currentUsers.get( sessionID );
    }
    
    public void cacheSessionObject( String sessionID, String name, Object value )
    {
    	
    }
    
    public Object getSessionObject( String sessionID, String name )
    {
    	return null;
    }

	@Override
	public void sessionCreated( HttpSessionEvent sessionEvent )
	{

	}

	@Override
	public void sessionDestroyed( HttpSessionEvent sessionEvent )
	{
		String sessionID = sessionEvent.getSession().getId();
		if (sessionID != null)
		{
			currentUsers.remove( sessionID );
		}
	}
	
	
	private ISecurityManager getSecurityManager( String securityManagerName )
	{
		ApplicationManager appMan = ApplicationManager.getInstance( );
		return (ISecurityManager)appMan.getApplicationObject( securityManagerName,  "SecurityManager" );
	}

}
