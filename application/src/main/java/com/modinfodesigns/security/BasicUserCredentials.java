package com.modinfodesigns.security;


public class BasicUserCredentials implements IUserCredentials
{
    private String username;
    private String password;
    private String searchApplication;
    
    public BasicUserCredentials( String username, String password )
    {
        this.username = username;
        this.password = password;
    }
    
    public BasicUserCredentials( String username, String password, String searchApplication )
    {
        this.username = username;
        this.password = password;
        this.searchApplication = searchApplication;
    }
    
    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getPassword()
    {
        return password;
    }
    
    
    @Override
    public void setSearchApplication( String searchApplicationName )
    {
    	this.searchApplication = searchApplicationName;
    }

    @Override
    public String getSearchApplication()
    {
        return this.searchApplication;
    }
}
