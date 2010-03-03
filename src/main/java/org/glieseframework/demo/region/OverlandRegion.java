package org.glieseframework.demo.region;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;

import java.io.Serializable;
import java.util.Collection;

import org.glieseframework.demo.region.Overland.OverlandEntry;
import org.glieseframework.core.Item;
import org.glieseframework.core.Player;
import org.glieseframework.core.SingleSenderGroup;
import org.glieseframework.region.Region;
import org.glieseframework.region.RegionProxy;
import org.glieseframework.game.Coordinate;
import org.glieseframework.message.Message;

public class OverlandRegion implements Region, Serializable {

    private static final long serialVersionUID = 1;

    final long height;
    final long width;

    final ManagedReference<SingleSenderGroup> groupRef;
    
    private final ManagedReference<Overland> overlandRef;

    public OverlandRegion(String regionName, long regionWidth, long regionHeight) {
        this.height = regionHeight;
        this.width = regionWidth;
        groupRef = AppContext.getDataManager().
            createReference(new SingleSenderGroup(regionName, true));
        overlandRef = AppContext.getDataManager().createReference(new Overland(width, height));
    }

    public RegionProxy join(Player player, Coordinate location) {
        OverlandEntry entry = overlandRef.get().add(player, (long) location.x, (long) location.y);
        if (entry == null) {
            return null;
        }
        groupRef.get().join(player);
        return new OverlandRegionProxy(this, player, entry);
    }

    public RegionProxy join(Region region, Coordinate location) {
        throw new UnsupportedOperationException("Cannot use sub-regions");
    }

    public RegionProxy join(Item item, Coordinate location) {
        // TODO: define addItem message.
        return null;
    }

    private static class OverlandRegionProxy implements RegionProxy, Serializable {
        private final ManagedReference<OverlandRegion> regionRef;
        private final ManagedReference<Player> playerRef;
        private ManagedReference<? extends OverlandEntry> currentPointRef;
        private long currentX;
        private long currentY;
        
        OverlandRegionProxy(OverlandRegion region, Player player,
                        OverlandEntry startingPoint) {
            DataManager dataManager = AppContext.getDataManager();
            regionRef = dataManager.createReference(region);
            playerRef = dataManager.createReference(player);
            currentPointRef = dataManager.createReference(startingPoint);
            currentX = startingPoint.x;
            currentY = startingPoint.y;
        }

        public void release() {
            currentPointRef.get().remove(playerRef);
            regionRef.get().groupRef.get().leave(playerRef.get());
        }

        public Coordinate getLocation() {
            return new Coordinate(currentX, currentY, 0);
        }

        public boolean move(Coordinate location) {
            // TODO: Implement move behavior.
            return true;
        }

        public boolean move(Coordinate location, float speed) {
            if (speed != 0) {
                return false;
            }
            return move(location);
        }

        public void sendMessage(Message message, float radius) {
            OverlandRegion region = regionRef.get();

            // TODO: Implement scoping logic.

            region.groupRef.get().send(message);
        }

        public void sendToVisible(Message message) {
            regionRef.get().groupRef.get().send(message);
        }

        public Collection<? extends Player> getPlayers(float radius) {
            return currentPointRef.get().getPlayers((long)radius);
        }

        public Collection<? extends Item> getItems(float radius) {
            return currentPointRef.get().getItems((long)radius);
        }

        public Region getRegion() {
            return regionRef.get();
        }
    }
}
