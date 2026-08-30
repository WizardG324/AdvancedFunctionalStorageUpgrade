package com.wizardg.func_au;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue REQUIRE_VOID_UPGRADE = BUILDER
            .comment("Whether per-slot voiding also needs a Void Upgrade in the drawer.")
            .define("requireVoidUpgrade", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean requireVoidUpgrade() {
        return !SPEC.isLoaded() || REQUIRE_VOID_UPGRADE.get();
    }
}
