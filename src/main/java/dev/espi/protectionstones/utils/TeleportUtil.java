/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class TeleportUtil {

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        if (entity == null || location == null) return CompletableFuture.completedFuture(false);

        Method teleportAsync = findTeleportAsyncMethod(entity);
        if (teleportAsync != null) {
            try {
                Object result = teleportAsync.invoke(entity, location);
                if (result instanceof CompletableFuture) {
                    return (CompletableFuture<Boolean>) result;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                ProtectionStones.getPluginLogger().warning("Unable to teleport entity asynchronously: " + e.getMessage());
                return CompletableFuture.completedFuture(false);
            }
        }

        if (UniversalScheduler.isFolia) {
            ProtectionStones.getPluginLogger().warning("Folia is running, but teleportAsync(Location) is not available.");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(entity.teleport(location));
    }

    private static Method findTeleportAsyncMethod(Entity entity) {
        try {
            return entity.getClass().getMethod("teleportAsync", Location.class);
        } catch (NoSuchMethodException ignored) {
            try {
                return Entity.class.getMethod("teleportAsync", Location.class);
            } catch (NoSuchMethodException ignoredAgain) {
                return null;
            }
        }
    }
}
