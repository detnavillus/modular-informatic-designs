package com.modinfodesigns.app.spring;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.BaseObjectFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;

public class SpringObjectFactory extends BaseObjectFactory implements ServletContextListener
{
	private String springObjectFactoryName = "SpringObjectFactory";
	
    public void initialize( String config )
    {
    	
    }


	@Override
	public void contextInitialized( ServletContextEvent scEvent )
	{
        ServletContext sc = scEvent.getServletContext( );
        
        String springConfig = sc.getInitParameter( "SpringConfigurationFile" );
        // configure Spring...
        
        ApplicationManager appManager = ApplicationManager.getInstance( );
        appManager.addObjectFactory( springObjectFactoryName, this );
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
}
