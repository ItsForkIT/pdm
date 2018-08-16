package com.disarm.surakshit.pdm.Merging.api;


/**
 * An interned version of a string, with provinance information    
 *
 */

public interface SourcedToken extends Token
{
    public String getSource();
}
