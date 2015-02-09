//keeping it for reference purposes in case we would like to decouple the listeners
//from the notifiers again
package com.kaltura.playersdk.notifiers;

import com.google.gson.Gson;
import com.kaltura.playersdk.events.OnErrorListener;

///**
// * Created by itayi on 2/5/15.
// */
public class ErrorBuilder {
    String errorMessage;
    int errorId;

    public ErrorBuilder()
    {

    }

    public ErrorBuilder(OnErrorListener.ErrorInputObject inputObject){
        errorMessage = inputObject.errorMessage;
        errorId = inputObject.errorId;
    }

    public ErrorBuilder setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
        return this;
    }


    public ErrorBuilder setErrorId(int errorId){
        this.errorId = errorId;
        return this;
    }

    public ErrorObject build(){
        return new ErrorObject(this);
    }

    public static class ErrorObject{
        String errorMessage;
        int errorId;
        private ErrorObject(ErrorBuilder builder){
            errorMessage = builder.errorMessage;
            errorId = builder.errorId;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}