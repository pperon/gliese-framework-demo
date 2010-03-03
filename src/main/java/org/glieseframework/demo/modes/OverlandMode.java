package org.glieseframework.demo.modes;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import java.io.Serializable;

import org.glieseframework.core.*;
import org.glieseframework.game.*;
import org.glieseframework.internal.MessageManagerImpl;
import org.glieseframework.region.*;
import org.glieseframework.message.common.*;
import org.glieseframework.demo.events.BasicResponse;
import org.glieseframework.demo.region.OverlandRegion;
import org.glieseframework.game.Coordinate;
import org.glieseframework.message.Message;
import org.glieseframework.message.common.ChatMessage;
import org.glieseframework.message.common.MovementMessage;

/**
 *
 * @author pperon
 */
public class OverlandMode implements GameMode, Serializable, ManagedObject {

    private static final long serialVersionUID = 1;

    private final String name;
    private final int width;
    private final int height;
    private final ManagedReference<? extends Group> chatRef;
    private final ManagedReference<? extends Region> regionRef;

    private final static Coordinate START = new Coordinate(100, 100, 0);

    public OverlandMode(String name, int height, int width, RegionFactory regionFactory) {
    	System.out.println("Creating mode: " + name);
        this.name = name;
        this.width = width;
        this.height = height;
        this.chatRef = AppContext.getDataManager().createReference(
            new UnmoderatedGroup(name + "Chat", true)
        );
        this.regionRef = AppContext.getDataManager().createReference(
            new OverlandRegion(name + "Region", width, height)        
        );
    }

    public String getName() {
        return name;
    }

    public GameProxy join(UserPlayer player, LeaveNotificationHandle handle ) {
        chatRef.get().join(player);
        ModeChangeMessageSpec spec = new ModeChangeMessageSpec(getName());
        Message msg = AppContext.getManager(MessageManagerImpl.class).createMessage(spec);
        player.send(msg);
        return new OverlandModeProxy(this, player, regionRef.get(), handle);
    }

    private void notifyLeft(UserPlayer player) {
        chatRef.get().leave(player);
    }

    static class OverlandModeProxy implements GameProxy, Serializable {

        private static final long serialVersionUID = 1;

        private final ManagedReference<? extends OverlandMode> overlandRef;
        private final ManagedReference<? extends UserPlayer> playerRef;
        private final ObjectWrapper<? extends RegionProxy> wrappedRegionProxy;
        private final ObjectWrapper<? extends LeaveNotificationHandle>
                wrappedHandle;

        OverlandModeProxy(OverlandMode mode, UserPlayer player, Region region,
                LeaveNotificationHandle handle) {

            DataManager dm = AppContext.getDataManager();
            overlandRef = dm.createReference(mode);
            playerRef = dm.createReference(player);
            wrappedRegionProxy =
                new ObjectWrapper<RegionProxy>(region.join(player, START));
            wrappedHandle = new ObjectWrapper<LeaveNotificationHandle>(
                    handle);
        }

        public void handleMessage(Message message) {
            switch(message.getMessageId()) {
                case MovementMessage.STANDARD_ID:
                    System.out.println("Overland movement message received");
                    MovementMessage msg = (MovementMessage) message;
                    boolean moved =
                        wrappedRegionProxy.get().move(msg.getLocation(), msg.getSpeed());
                    if (! moved) {
                        // TODO: send back a message that the movement failed
                    }
                    break;
                case ChatMessage.STANDARD_ID:
                    System.out.println("Overland chat message received");
                    // TODO: Handle chat messages.
                    break;
                default:
                    System.out.println("unknown message!");
                    // TODO: Handle unknown messages. 
            }
        }

        public EventResponse handleEvent(Event event) {
            System.out.println("OverlandModeProxy.handleEvent called.");
            switch (event.getEventId()) {
            case CollisionEvent.ID:
                return BasicResponse.DENY;
            case LeftRegionEvent.ID:
                handleLeft(true);
            }
            return BasicResponse.ACCEPT;
        }

        public void loggedOut() {
            System.out.println("OverlandModeProxy.loggedOut called.");
            handleLeft(false);
        }

        private void handleLeft(boolean stillPlaying) {
            overlandRef.get().notifyLeft(playerRef.get());
            wrappedRegionProxy.get().release();            
            wrappedHandle.get().leave(stillPlaying);
        }
    }
}
