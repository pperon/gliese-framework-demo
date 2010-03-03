package org.glieseframework.demo.events;

import org.glieseframework.game.EventResponse;

final public class BasicResponse {

    public static final EventResponse ACCEPT = new EventResponse() {
        public boolean eventAccepted() {
            return true;
        }
    };

    public static final EventResponse DENY = new EventResponse() {
        public boolean eventAccepted() {
            return false;
        }
    };
}
