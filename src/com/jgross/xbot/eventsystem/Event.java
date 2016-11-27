package com.jgross.xbot.eventsystem;
public abstract class Event {
    
    private String name;
    
    public Event() { }
    
    /**
     * @return list of registered listeners
     */
    public abstract EventList getEvents();
    
    /**
     * @return the name of the event
     */
    public String getEventName() {
        return ( name == null || name.equals( "" ) ) ? getClass().getSimpleName() : name;
    }
}

