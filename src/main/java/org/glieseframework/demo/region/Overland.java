package org.glieseframework.demo.region;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;

import org.glieseframework.core.Item;
import org.glieseframework.core.Player;

import java.io.Serializable;
import java.util.HashSet;

public class Overland implements ManagedObject, Serializable {

    private final long width;
    private final long height;

    //private final ManagedReference<? extends ShopEntry> shopRef;
    
    Overland(long width, long height) {
        this.width = width;
        this.height = height;
        //ShopEntry shop = new ShopEntry();
        //shopRef = AppContext.getDataManager().createReference(shop);
    }

    OverlandEntry add(Player player, long x, long y) {
        if (((x < 0) || (x >= width)) || ((y < 0) || (y >= height))) {
            throw new IllegalArgumentException("illegal starting location");
        }

        OverlandEntry entry = null;
        return entry;
    }

    abstract static class OverlandEntry implements ManagedObject, Serializable {
        long x;
        long y;

        protected OverlandEntry(long x, long y) {
            this.x = x;
            this.y = y;
        }

        protected void remove(ManagedReference<? extends Player> playerRef) {
            /*
            AppContext.getDataManager().markForUpdate(this);
            players.remove(playerRef);
            if (isEmpty()) {
                rightRef.getForUpdate().leftRef = leftRef;
                leftRef.getForUpdate().rightRef = rightRef;
                AppContext.getDataManager().removeObject(this);
            }
            */
        }

        void remove(Item item) {
            // TODO
        }

        protected HashSet<? extends Player> getPlayers(long radius) {
            HashSet<Player> set = new HashSet<Player>();
            return set;
        }

        protected HashSet<? extends Item> getItems(long radius) {
            // TODO
            return null;
        }
    }
}
