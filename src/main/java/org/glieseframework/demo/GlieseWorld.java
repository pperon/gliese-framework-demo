package org.glieseframework.demo;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import org.glieseframework.game.*;

import org.glieseframework.region.RegionFactory;

import java.io.Serializable;
import java.util.Properties;

import org.glieseframework.core.UserPlayer;

import org.glieseframework.demo.modes.OverlandMode;

/**
 *
 * @author pperon
 */
public class GlieseWorld implements Game, Serializable {

    private static final long serialVersionUID = 1;

    private static final String OVERLAND_NAME = "glieseworld:mode:overland";

    public GlieseWorld(Properties p, RegionFactory factory) {
        
        DataManager dm = AppContext.getDataManager();

        OverlandMode overlandMode = new OverlandMode(
                "overland", 500, 400, factory);
        dm.setBinding(OVERLAND_NAME, overlandMode);

        // TODO: Add Shop mode.
    }

    public UserPlayer getUserPlayer(String name) {
        UserPlayer player = NameMappingUtil.getUserPlayer(name);
        if(player == null) {
            player = new GWUserPlayer(name);
            NameMappingUtil.addUserPlayer(player);
        }
        
        return player;
    }

    public GameProxy join(UserPlayer player) {
        return ((OverlandMode)
            (AppContext.getDataManager().getBinding(OVERLAND_NAME))).
            join(player, new GWLeaveNotificationHandle((GWUserPlayer)player));
    }

    /**
     * 
     */
    static class GWLeaveNotificationHandle implements LeaveNotificationHandle,
            ManagedObject, Serializable {

        private static final long serialVersionUID = 1;

        private final ManagedReference<GWUserPlayer> playerRef;

        GWLeaveNotificationHandle(GWUserPlayer player) {
            playerRef = AppContext.getDataManager().createReference(player);
        }

        public void leave(boolean joinNewMode) {
            AppContext.getDataManager().removeObject(this);
            // for now, just disconnect the client
            playerRef.get().disconnect();
        }

        public void leave(String newModeName) {
            AppContext.getDataManager().removeObject(this);
            // the only place you could go would be the shop but
            // that's not ready yet so...
            playerRef.get().disconnect();
        }
    }

    /**
     * 
     */
    static class GWUserPlayer extends BasicUserPlayer
            implements Serializable {

        private static final long serialVersionUID = 1;

        GWUserPlayer(String name) {
            super(name);
        }

        void changeMode(GameMode mode) {
            setGameProxy(mode.join(this, new GWLeaveNotificationHandle(this)));
        }
    }
}
