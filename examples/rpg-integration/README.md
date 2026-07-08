# AeternumGenesis RPG Integration Example

This directory contains a minimal, runnable Paper plugin that shows how an **external plugin** can customize every part of AeternumGenesis through the public `AeternumGenesisAPI` — no YAML files inside `plugins/AeternumGenesis/` required.

## What it demonstrates

| AeternumGenesis feature | API used | What the example does |
|---|---|---|
| **Items** | `ItemAPI#registerTemplate` / `unregisterTemplate` | Registers `rpg_fiery_longsword` and `rpg_guardian_shield` at runtime |
| **Mobs** | `MobAPI#registerTemplate` / `unregisterTemplate` | Registers `rpg_fallen_knight` and `rpg_necromancer` with factions, AI targets, equipment, and boss bars |
| **Skills** | `SkillAPI#registerSkill` / `unregisterSkill` | Registers `rpg_necrotic_blast` and `rpg_fireworks` |
| **Spawn rules** | `SpawnAPI#registerRule` / `unregisterRule` | Replaces Skeletons/Wither Skeletons with `rpg_fallen_knight` in dark, underground areas |
| **Custom effects** | `SkillAPI#registerEffect` | Adds a brand-new `launch_firework` effect type |
| **Commands** | `ItemAPI#buildItem`, `MobAPI#spawnMob`, `SkillAPI#triggerSkill` | `/rpgitem`, `/rpgmob`, `/rpgskill` |

## Project layout

```
examples/rpg-integration/
├── pom.xml
├── README.md
└── src/main/
    ├── java/net/sakurain/mc/genesis/examples/rpg/
    │   ├── RpgIntegrationPlugin.java   # main plugin + command handlers
    │   └── LaunchFireworkEffect.java   # custom skill effect
    └── resources/plugin.yml
```

## Build

1. Build AeternumGenesis first so the local JAR exists:

   ```bash
   cd ../..
   mvn clean package -DskipTests
   ```

2. Build the example plugin:

   ```bash
   cd examples/rpg-integration
   mvn clean package -DskipTests
   ```

The output is `target/rpg-integration-1.0.0-SNAPSHOT.jar`.

## Install

Copy both JARs into your Paper server:

```
server/plugins/
├── AeternumGenesis-1.0.0-SNAPSHOT.jar
└── rpg-integration-1.0.0-SNAPSHOT.jar
```

`AeternumGenesisRpgIntegration` declares `depend: [AeternumGenesis]`, so Paper will load AeternumGenesis first.

## In-game usage

```
/rpgitem <player> rpg_fiery_longsword
/rpgitem <player> rpg_guardian_shield
/rpgmob rpg_fallen_knight
/rpgmob rpg_necromancer
/rpgskill rpg_fireworks
```

Fallen Knights also spawn naturally when Skeletons/Wither Skeletons spawn in low light below Y=64 in `world` or `world_nether`.

## How the API is accessed

```java
AeternumGenesisAPI api = AeternumGenesisAPI.getInstance();
ItemAPI  itemAPI  = api.getItemAPI();
MobAPI   mobAPI   = api.getMobAPI();
SkillAPI skillAPI = api.getSkillAPI();
SpawnAPI spawnAPI = api.getSpawnAPI();
```

`AeternumGenesisAPI#getInstance()` reads the service registered by AeternumGenesis through Bukkit's `ServicesManager`.

## Runtime registration pattern

Every template is built from a Bukkit `MemoryConfiguration` and registered at enable time. For example, a mob:

```java
MemoryConfiguration config = new MemoryConfiguration();
config.set("type", "SKELETON");
config.set("display_name", "&cFallen Knight");
config.set("health", 60.0);
config.set("faction", "undead_legion");
config.set("attributes", List.of(
    Map.of("type", "ATTACK_DAMAGE", "amount", 6.0, "operation", "ADD_NUMBER"),
    Map.of("type", "MOVEMENT_SPEED", "amount", 0.12, "operation", "SET_VALUE")
));

boolean ok = api.getMobAPI().registerTemplate("rpg_fallen_knight", config);
```

The keys and value formats are the same ones used in AeternumGenesis YAML configs (items, mobs, skills, spawn rules).

## Custom skill effects

To add an effect type AeternumGenesis does not know about, implement the public `SkillEffect` interface and register a factory:

```java
api.getSkillAPI().registerEffect("launch_firework", LaunchFireworkEffect::new);
```

The effect can then be used in any skill config:

```yaml
effects:
  - type: launch_firework
    params:
      color: PURPLE
      fade: GREEN
      power: 2
```

## Cleanup

`onDisable()` unregisters every runtime template. This ensures that a plugin reload does not leave stale content behind.

## Notes

- The example depends on `target/aeternumgenesis-1.0.0-SNAPSHOT.jar` via a Maven `system` scope. In a real project you would install AeternumGenesis into your local/remote Maven repository or use a multi-module build.
- Attribute `operation: SET_VALUE` makes the configured number match the vanilla tooltip value (e.g., `1.6` attack speed).
- Faction `undead_legion` prevents the example mobs from attacking each other.
