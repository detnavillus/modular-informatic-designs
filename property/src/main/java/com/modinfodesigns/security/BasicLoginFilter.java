package com.modinfodesigns.security;

import com.modinfodesigns.app.SessionManager;
import com.modinfodesigns.app.UserSession;

import com.modinfodesigns.network.http.HttpRequestData;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;

public class BasicLoginFilter implements Filter
{
    private ILoginHandler    loginHandler;
    private String           securityManager;
    

	@Override
	public void doFilter( ServletRequest request, ServletResponse response,
			              FilterChain arg2) throws IOException, ServletException 
	{

		HttpRequestData httpData = getHttpRequestData( request );
		
		SessionManager sessMan = SessionManager.getInstance( );
		
		UserSession userSess = sessMan.getUserSession( httpData, loginHandler, securityManager );
		if (userSess != null)
		{
			
		}

	}

	@Override
	public void init( FilterConfig arg ) throws ServletException
	{
		// get the security manager name
		

		// get the Login Handler
	}

	
	@Override
	public void destroy()
	{

	}
	
	private HttpRequestData getHttpRequestData( ServletRequest request )
	{
		if (request instanceof HttpServletRequest)
		{
			return new HttpRequestData( (HttpServletRequest)request );
		}
		
		return null;
	}

}
