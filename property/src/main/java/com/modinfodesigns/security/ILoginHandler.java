package com.modinfodesigns.security;

import com.modinfodesigns.network.http.HttpRequestData;

public interface ILoginHandler
{
    public IUserCredentials getUserCredentials( HttpRequestData httpRequest );
    
}
