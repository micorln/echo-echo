package com.micorln.echoecho.core;

public class ScheduledEchoFuture<T> {

    private EchoFuture<T> echoFuture;

    public ScheduledEchoFuture() {
        
    }

    void setEchoFuture(EchoFuture<T> echoFuture) {
        this.echoFuture = echoFuture;
    }

    public EchoFuture<T> getEchoFuture() {
        return echoFuture;
    }

    public void cancel() {
        echoFuture.cancel();
    }

    public T get() {
        T res = echoFuture.get();
        return res;
    }


    
}
