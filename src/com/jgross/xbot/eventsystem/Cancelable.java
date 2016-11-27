package com.jgross.xbot.eventsystem;

public interface Cancelable {
    
    /**
     * @return whether or not the event is canceled
     */
    boolean isCancelled();
    
    /**
     * Cancel the event.
     * This should be used when you want to stop the server from
     * doing the default action it would normally do.
     * @param cancel If set to true, the event will cancel. If set to false, the event will not cancel
     */
    void setCancel(boolean cancel);

}

