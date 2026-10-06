package GreyMeridianBetterTowTrucks;

import me.zed_0xff.zombie_buddy.Patch;
import org.joml.Vector3f;
import zombie.characters.IsoPlayer;
import zombie.core.physics.Bullet;
import zombie.debug.DebugType;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.BaseVehicle.Authorization;
import zombie.vehicles.VehicleManager;

import java.lang.reflect.Field;

/**
 * ZombieBuddy compatibility patch for Project Zomboid's vehicle towing constraint path.
 *
 * <p>Restores Harry's Tow Truck hook-specific towing behavior while allowing Better Car Physics
 * to remain the active vehicle physics base. Only tow-truck hook requests are intercepted;
 * all other towing requests fall through to the original game method.</p>
 *
 * @author Grey Meridian
 * @created 2026-05-13
 */
public final class Patch_BaseVehicle {

    private Patch_BaseVehicle() {
    }

    /**
     * ZombieBuddy advice for {@code zombie.vehicles.BaseVehicle.addPointConstraint}.
     *
     * <p>Handles tow-truck hook constraints using the recovered working lifecycle:
     * clears previous constraints, resolves towing attachment positions, creates the rope
     * constraint client-side, updates vehicle towing state, notifies constraint changes,
     * and mirrors server-side authorization/network towing behavior when applicable.</p>
     */
    @Patch(className = "zombie.vehicles.BaseVehicle", methodName = "addPointConstraint")
    public static final class Patch_addPointConstraint {

        /**
         * Runs before {@code BaseVehicle.addPointConstraint}.
         *
         * @param self            the towing vehicle
         * @param player          the player initiating the tow action, when available
         * @param towedVehicle    the vehicle being attached
         * @param attachmentSelf  the attachment point on the towing vehicle
         * @param attachmentOther the attachment point on the towed vehicle
         * @param remote          whether the request came from a remote/server-forwarded towing action
         * @return {@code true} when this patch handles the hook case; {@code false} for vanilla handling
         */
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(
                @Patch.This BaseVehicle self,
                @Patch.Argument(0) IsoPlayer player,
                @Patch.Argument(1) BaseVehicle towedVehicle,
                @Patch.Argument(2) String attachmentSelf,
                @Patch.Argument(3) String attachmentOther,
                @Patch.Argument(4) Boolean remote
        ) {
            if (!isHookCase(towedVehicle, attachmentSelf, attachmentOther)) {
                return false;
            }

            boolean remoteFlag = Boolean.TRUE.equals(remote);

            self.setPreviouslyMoved(true);

            if (player != null
                    && (IsoUtils.DistanceToSquared(player.getX(), player.getY(), self.getX(), self.getY()) > 100.0f
                    || IsoUtils.DistanceToSquared(player.getX(), player.getY(), towedVehicle.getX(), towedVehicle.getY()) > 100.0f)) {
                DebugType.Vehicle.warn("The " + player.getUsername() + " user attached vehicles at a long distance");
            }

            self.breakConstraint(true, remoteFlag);
            towedVehicle.breakConstraint(true, remoteFlag);

            Vector3f selfPos = self.getTowingLocalPos(attachmentSelf, new Vector3f());
            Vector3f otherPos = towedVehicle.getTowedByLocalPos(attachmentOther, new Vector3f());

            if (selfPos == null || otherPos == null) {
                return true;
            }

            int constraintId = -1;

            if (!GameServer.server) {
                constraintId = Bullet.addRopeConstraint(
                        getIntField(self, "vehicleId"),
                        getIntField(towedVehicle, "vehicleId"),
                        selfPos.x, selfPos.y, selfPos.z,
                        otherPos.x, otherPos.y, otherPos.z,
                        0.05f
                );

                setIntField(self, "constraintTowing", constraintId);
            }

            setIntField(towedVehicle, "constraintTowing", constraintId);

            self.setVehicleTowing(towedVehicle, attachmentSelf, attachmentOther);
            towedVehicle.setVehicleTowedBy(self, attachmentSelf, attachmentOther);

            self.constraintChanged();
            towedVehicle.constraintChanged();

            if (GameServer.server
                    && player != null
                    && self.isNetPlayerAuthorization(Authorization.Server)
                    && towedVehicle.isNetPlayerAuthorization(Authorization.Server)) {
                self.setNetPlayerAuthorization(Authorization.LocalCollide, player.onlineId);
                setLongField(self, "authSimulationTime", System.currentTimeMillis());

                towedVehicle.setNetPlayerAuthorization(Authorization.LocalCollide, player.onlineId);
                setLongField(towedVehicle, "authSimulationTime", System.currentTimeMillis());
            }

            if (GameServer.server && !remoteFlag) {
                VehicleManager.instance.attachTowing(self, towedVehicle, attachmentSelf, attachmentOther);
            }

            return true;
        }

        /**
         * Checks whether the current towing request should use the tow-truck hook compatibility path.
         *
         * @param towedVehicle    the vehicle being attached
         * @param attachmentSelf  the attachment point on the towing vehicle
         * @param attachmentOther the attachment point on the towed vehicle
         * @return {@code true} only for valid non-trailer tow-truck hook requests
         */
        public static boolean isHookCase(BaseVehicle towedVehicle, String attachmentSelf, String attachmentOther) {
            return towedVehicle != null
                    && attachmentSelf != null
                    && attachmentOther != null
                    && attachmentSelf.equalsIgnoreCase("hook")
                    && !attachmentOther.equalsIgnoreCase("flipNode")
                    && !towedVehicle.getScriptName().contains("Trailer");
        }

        /**
         * Reads a private integer field from {@link BaseVehicle}.
         *
         * @param vehicle   the vehicle instance
         * @param fieldName the declared field name
         * @return the field value
         */
        public static int getIntField(BaseVehicle vehicle, String fieldName) {
            try {
                Field field = BaseVehicle.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getInt(vehicle);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("TowTruck_BCP_Compat failed to read BaseVehicle." + fieldName, e);
            }
        }

        /**
         * Writes a private integer field on {@link BaseVehicle}.
         *
         * @param vehicle   the vehicle instance
         * @param fieldName the declared field name
         * @param value     the value to assign
         */
        public static void setIntField(BaseVehicle vehicle, String fieldName, int value) {
            try {
                Field field = BaseVehicle.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.setInt(vehicle, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("TowTruck_BCP_Compat failed to write BaseVehicle." + fieldName, e);
            }
        }

        /**
         * Writes a private long field on {@link BaseVehicle}.
         *
         * @param vehicle   the vehicle instance
         * @param fieldName the declared field name
         * @param value     the value to assign
         */
        public static void setLongField(BaseVehicle vehicle, String fieldName, long value) {
            try {
                Field field = BaseVehicle.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.setLong(vehicle, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("TowTruck_BCP_Compat failed to write BaseVehicle." + fieldName, e);
            }
        }
    }
}
