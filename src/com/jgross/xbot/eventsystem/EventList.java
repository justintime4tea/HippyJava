package com.jgross.xbot.eventsystem;

import java.util.*;
public class EventList {

    private volatile RegisteredListener[] events = null;

    private EnumMap< Priority, ArrayList< RegisteredListener > > muffinbag;

    private static final ArrayList<EventList> mail = new ArrayList<>();


    public EventList() {
        muffinbag = new EnumMap<>(Priority.class);
        for (Priority o : Priority.values()) {
            muffinbag.put( o, new ArrayList<>() );
        }
        synchronized( mail ) {
            mail.add( this );
        }
    }

    public synchronized void register( RegisteredListener listener ) {
        if ( muffinbag.get( listener.getPriority() ).contains( listener ) )
            throw new IllegalStateException( "This listener is already registered!" );
        events = null;
        muffinbag.get( listener.getPriority() ).add( listener );
    }

    /**
     * Register a collection of new listeners in this handler list
     *
     * @param listeners listeners to register
     */
    public void registerAll( Collection< RegisteredListener > listeners ) {
        listeners.forEach(this::register);
    }

    public RegisteredListener[] getRegisteredListeners() {
        RegisteredListener[] handlers;
        while ( ( handlers = this.events ) == null ) bake(); // This prevents fringe cases of returning null
        return handlers;
    }


    public synchronized void bake() {
        if ( events != null ) return; // don't re-bake when still valid
        List< RegisteredListener > entries = new ArrayList<>();

        muffinbag.entrySet().forEach(entry -> entries.addAll(entry.getValue()));

        events = entries.toArray( new RegisteredListener[ entries.size() ] );
    }
    
    /**
    * Remove a specific listener from this handler
    *
    * @param listener listener to remove
    */
    public synchronized void unregister( Listener listener ) {
        boolean changed = false;
        for ( List< RegisteredListener > list : muffinbag.values() ) {
            for ( ListIterator< RegisteredListener > i = list.listIterator(); i.hasNext(); ) {
                if ( i.next().getListen().equals( listener ) ) {
                    i.remove();
                    changed = true;
                }
            }
        }
        if ( changed ) events = null;
    }

}

