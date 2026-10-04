package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.Create;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Debug aid: when started with {@code -Dcreate.mixin.audit=true}, forces every registered mixin to be applied once
 * so that injection failures show up in the log right away instead of on first use of the target class.
 */
public final class MixinAudit {
    private static final boolean ENABLED = Boolean.getBoolean("create.mixin.audit");
    private static boolean done;

    private MixinAudit() {
    }

    public static void runOnce() {
        if (!ENABLED || done) {
            return;
        }
        done = true;
        try {
            MixinEnvironment.getCurrentEnvironment().audit();
            Create.LOGGER.info("[MixinAudit] audit finished");
        } catch (Throwable t) {
            Create.LOGGER.error("[MixinAudit] audit failed", t);
        }
    }
}
