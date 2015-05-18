package com.modinfodesigns.app;

import com.modinfodesigns.security.IUserCredentials;

public class UserSession
{
    private IUserCredentials userCredentials;

    public UserSession( IUserCredentials userCredentials )
    {
    	this.userCredentials = userCredentials;
    }

    public IUserCredentials getUserCredentials(  )
    {
    	return this.userCredentials;
    }
}
