package com.jgross.xbot.eventsystem;

public interface Executor {
    public void execute( Listener listen, Event event ) throws Exception;
}

