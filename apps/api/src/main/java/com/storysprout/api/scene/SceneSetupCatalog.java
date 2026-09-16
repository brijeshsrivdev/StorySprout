package com.storysprout.api.scene;

import java.util.List;
import java.util.Set;

public final class SceneSetupCatalog {
    private SceneSetupCatalog() {}
    public record Preset(String key, String name, String category) {}
    public static final List<Preset> BACKGROUNDS = List.of(
        new Preset("forest_day", "Sunny Forest", "Nature"),
        new Preset("garden_day", "Garden", "Nature"),
        new Preset("bedroom_day", "Cozy Bedroom", "Indoor"),
        new Preset("classroom_day", "Classroom", "Indoor"),
        new Preset("village_day", "Village", "Outdoor"),
        new Preset("beach_day", "Sunny Beach", "Outdoor")
    );
    public static final List<Preset> PROPS = List.of(
        new Preset("wooden_chair", "Wooden Chair", "Furniture"),
        new Preset("small_table", "Small Table", "Furniture"),
        new Preset("backpack", "Backpack", "Object"),
        new Preset("storybook", "Storybook", "Object"),
        new Preset("flower_pot", "Flower Pot", "Nature"),
        new Preset("ball", "Ball", "Toy")
    );
    public static final Set<String> ACTIONS = Set.of("IDLE","TALK","WALK","RUN","WAVE","SIT","JUMP");
    public static boolean backgroundExists(String key) { return key == null || BACKGROUNDS.stream().anyMatch(p -> p.key().equals(key)); }
    public static boolean propExists(String key) { return PROPS.stream().anyMatch(p -> p.key().equals(key)); }
    public static Preset background(String key) { return BACKGROUNDS.stream().filter(p -> p.key().equals(key)).findFirst().orElse(null); }
    public static Preset prop(String key) { return PROPS.stream().filter(p -> p.key().equals(key)).findFirst().orElse(null); }
}
