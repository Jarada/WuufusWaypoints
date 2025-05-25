package com.github.jarada.waypoints.data;

import com.github.jarada.waypoints.util.Util;
import org.bukkit.*;

import java.util.Arrays;

public enum WarpEffect {

    BLAZE {
        @Override
        public void playTickEffectAtLocation(Location location, int tick, boolean to) {
            if (location == null) return;
            if (!to) {
                Util.playSound(location, Sound.ENTITY_BLAZE_BURN);
                Util.playEffect(location, Effect.MOBSPAWNER_FLAMES);
                if (Arrays.stream(Particle.values()).anyMatch(t -> t.name().equals("CAMPFIRE_SIGNAL_SMOKE"))) {
                    Util.playParticle(location, Particle.CAMPFIRE_SIGNAL_SMOKE, 50);
                }
            }
            super.playTickEffectAtLocation(location, tick, to);
        }

        @Override
        public void playWarpingEffectAtLocation(Location location, boolean to) {
            if (location == null) return;
            Util.playSound(location, Sound.ENTITY_BLAZE_AMBIENT);
        }
    },
    BUBBLE {
        @Override
        public void playTickEffectAtLocation(Location location, int tick, boolean to) {
            if (location == null) return;
            if (!to) {
                Util.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
                Util.playParticle(location, Particle.NAUTILUS, 50);
            } else if (tick == 4) {
                Util.playParticle(location, Particle.NAUTILUS, 50);
            }
            super.playTickEffectAtLocation(location, tick, to);
        }

        @Override
        public void playWarpingEffectAtLocation(Location location, boolean to) {
            if (location == null) return;
            Util.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        }
    },
    ENDER {
        @Override
        public void playLoadingEffectAtLocation(Location location) {
            if (location == null) return;
            Util.playSound(location, Sound.BLOCK_PORTAL_TRIGGER);
        }

        @Override
        public void playWarpingEffectAtLocation(Location location, boolean to) {
            if (location == null) return;
            Util.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT);
        }
    },
    NETHER {
        @Override
        public void playLoadingEffectAtLocation(Location location) {
            if (location == null) return;
            Util.playSound(location, Sound.BLOCK_PORTAL_TRIGGER);
        }

        @Override
        public void playTickEffectAtLocation(Location location, int tick, boolean to) {
            if (location == null) return;
            if (!to || tick == 4) {
                Util.playParticle(location, Particle.PORTAL, 50);
            }
            super.playTickEffectAtLocation(location, tick, to);
        }

        @Override
        public void playWarpingEffectAtLocation(Location location, boolean to) {
            if (location == null) return;
            Util.playSound(location, Sound.ENTITY_GHAST_SHOOT);
        }
    },
    QUIET,
    THUNDER {
        @Override
        public void playLoadingEffectAtLocation(Location location) {
            if (location == null) return;
            Util.playSound(location, Sound.BLOCK_PORTAL_TRIGGER);
        }

        @Override
        public void playWarpingEffectAtLocation(Location location, boolean to) {
            if (location == null) return;
            location.getWorld().strikeLightningEffect(location);
        }
    };

    public void playLoadingEffectAtLocation(Location location) {}

    public void playTickEffectAtLocation(Location location, int tick, boolean to) {
        if (location == null) return;
        if (!to) {
            Util.playEffect(location, Effect.ENDER_SIGNAL);
        }
    }

    public void playWarpingEffectAtLocation(Location location, boolean to) {}

}
