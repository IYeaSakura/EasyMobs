# AeternumGenesis

[![PaperMC](https://img.shields.io/badge/PaperMC-26.1.2-000000?logo=paper-minecraft)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25%2B-007396?logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apache-maven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A lightweight, YAML-driven custom mob and item plugin for **PaperMC 26.1.2**, inspired by MythicMobs. AeternumGenesis lets server owners create custom monsters, equipment, skills, spawn rules, and item sets without touching code or NMS.

[Features](#features) | [Tech Stack](#tech-stack) | [Project Structure](#project-structure) | [Getting Started](#getting-started) | [Development](#development) | [Build & Deployment](#build--deployment) | [Configuration Examples](#configuration-examples) | [Commands & Permissions](#commands--permissions) | [API Reference](#api-reference) | [Troubleshooting](#troubleshooting) | [Contributing](#contributing) | [License](#license) | [Acknowledgments](#acknowledgments) | [Contact](#contact)

---

## Features

### Custom Items
- Define weapons, armor, and tools with custom names, lore, enchantments, attributes, CustomModelData, and glow effects.
- Add passive effects triggered by holding or wearing items.
- Configure attack effects that trigger on hit with chance-based probability.
- Link items into item sets for powerful set bonuses.

### Custom Mobs
- Create mobs based on any vanilla entity type with custom health, attributes, equipment, and scaling levels.
- Add continuous particles, ambient sounds, BossBars, glow effects, and potion effects.
- Configure immunities, water behavior, door breaking, and custom AI goals.
- Define drop tables that override or extend vanilla loot.

### Skill System
- Build reusable skill templates in YAML with cooldowns, conditions, and chained effects.
- Use 20+ built-in effect types including damage, heal, potion, teleport, summon, particle, sound, lightning, explosion, ignite, knockback, message, title, actionbar, drop_item, execute_command, and delay.
- Combine conditions with `and`, `or`, and `not` logic.
- Bind skills to mobs with triggers such as on-spawn, on-hit, on-death, and timed intervals.

### Natural Spawn Control
- Use `REPLACE`, `ADD`, and `DENY` actions to control how custom mobs enter the world.
- Apply conditions based on time of day, weather, biome, world, light level, Y-level, moon phase, block below, and random chance.
- Enforce density limits to prevent overcrowding.

### Public Developer API
- Access item, mob, skill, spawn, set, block, and registry APIs through Bukkit's ServicesManager.
- Listen to Bukkit events for custom item builds, mob spawns, mob deaths, skill execution, and more.
- Register custom skill effects, skill conditions, and spawn conditions from external plugins.

### World Systems
- **Atmosphere Engine** — Apply layered regional atmospheres with weather, potion effects, particles, sounds, UI layers, entity modifiers, and environment rules.
- **Ecosystems** — Bind custom mobs to biomes with weighted spawn rules, group sizes, density limits, ambient particles, and ambient sounds.
- **World Rules** — Control global game rules, death behavior, PvP, and damage/hunger multipliers per world.

### Operational Features
- Hot-reload all YAML configs in-game with `/genesis reload`.
- Zero NMS dependency, built purely on Bukkit/Paper API for maximum compatibility.
- Persistent data storage via Bukkit PersistentDataContainer.

---

## Tech Stack

### Core Technologies

| Category | Technology | Version |
|----------|------------|---------|
| Server Platform | PaperMC | 26.1.2 |
| Language | Java | 25+ |
| Build Tool | Maven | 3.9+ |
| API | Bukkit/Paper API | 26.1.2 |

### Additional Libraries

- **Adventure API**: Component-based chat and text serialization.
- **js-yaml**: Used by documentation tooling for YAML validation examples.

---

## Project Structure

```
AeternumGenesis/
├── src/main/java/net/sakurain/mc/aeternumgenesis/   # Main plugin source
│   ├── AeternumGenesisPlugin.java                   # Plugin entry point
│   ├── api/                                         # Public API interfaces and events
│   │   ├── AeternumGenesisAPI.java                  # Central API accessor
│   │   ├── ItemAPI.java                             # Item query/build API
│   │   ├── MobAPI.java                              # Mob spawn/query API
│   │   ├── SkillAPI.java                            # Skill registration API
│   │   ├── SpawnAPI.java                            # Spawn rule API
│   │   ├── SetAPI.java                              # Item set API
│   │   ├── BlockAPI.java                            # Custom block API
│   │   ├── RegistryAPI.java                         # Effect/condition registry API
│   │   ├── event/                                   # Bukkit events
│   │   ├── exception/                               # Custom exceptions
│   │   └── impl/                                    # API implementations
│   ├── command/                                     # Command executor and subcommands
│   ├── config/                                      # Configuration loading and caching
│   ├── item/                                        # Custom item system
│   ├── item/set/                                    # Item set system
│   ├── item/effect/                                 # Passive and attack effect handlers
│   ├── mob/                                         # Custom mob system
│   ├── skill/                                       # Skill engine
│   ├── skill/effect/                                # Built-in skill effects
│   ├── skill/condition/                             # Built-in skill conditions
│   ├── spawn/                                       # Natural spawn control
│   ├── spawn/condition/                             # Built-in spawn conditions
│   ├── block/                                       # Custom block system
│   ├── listener/                                    # Event listeners
│   ├── ai/                                          # Custom AI goals
│   ├── util/                                        # Utility classes
│   ├── atmosphere/                                  # Atmosphere engine
│   └── world/                                       # World rule manager
├── src/main/resources/                              # Default configuration templates
│   ├── plugin.yml                                   # Plugin descriptor
│   ├── config.yml                                   # Main configuration
│   ├── items/example_items.yml                      # Example item templates
│   ├── mobs/example_mobs.yml                        # Example mob templates
│   ├── skills/example_skills.yml                    # Example skill templates
│   ├── spawns/example_spawns.yml                    # Example spawn rules
│   ├── sets/example_set.yml                         # Example item set
│   ├── blocks/example_blocks.yml                    # Example custom blocks
│   ├── atmospheres/example_atmosphere.yml           # Example atmosphere
│   ├── ecosystems/example_ecosystem.yml             # Example ecosystem
│   └── worlds/world_rules.yml                       # Example world rules
├── test/                                            # Test server configuration templates
├── examples/rpg-integration/                        # Example external plugin
├── .doc/                                            # Internal documentation and skills
├── pom.xml                                          # Maven build configuration
├── package.json                                     # Node tooling for docs
├── README.md                                        # English documentation
└── README_zh.md                                     # Chinese documentation
```

---

## Getting Started

### Prerequisites

- **Server**: PaperMC 26.1.2 or compatible build.
- **Java**: 25 or newer. Java 21 and lower will fail with `UnsupportedClassVersionError`.
- **Build Tool**: Maven 3.9 or newer if building from source.

### Installation

1. Download the latest `AeternumGenesis-*.jar` from the [Releases](../../releases) page.
2. Place the JAR file in your server's `plugins/` directory.
3. Start the server once to generate the default configuration folder.
4. Edit the YAML files in `plugins/AeternumGenesis/` to customize content.
5. Run `/genesis reload` in-game or restart the server.

### Building from Source

```bash
# Clone the repository
git clone https://github.com/IYeaSakura/AeternumGenesis.git
cd AeternumGenesis

# Build the plugin JAR
mvn clean package -DskipTests

# The output artifact is located at:
# target/AeternumGenesis-1.0.0-SNAPSHOT.jar
```

### Environment Variables

AeternumGenesis does not require environment variables for normal server operation. All behavior is controlled through YAML files under `plugins/AeternumGenesis/`.

For documentation tooling, Node.js dependencies can be installed with:

```bash
npm install
```

---

## Development

### Available Scripts

| Command | Description |
|---------|-------------|
| `mvn clean package` | Build the plugin JAR |
| `mvn clean package -DskipTests` | Build without running tests |
| `mvn clean verify` | Build and run quality checks |
| `npm install` | Install Node tooling for documentation |

### Code Style

#### Naming Conventions

| Item | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `AeternumGenesisPlugin.java`, `CustomMobManager.java` |
| Methods | camelCase | `spawnMob`, `registerTemplate` |
| Variables | camelCase | `templateId`, `itemStack` |
| Constants | SCREAMING_SNAKE_CASE | `ITEM_ID_KEY`, `DEFAULT_COOLDOWN` |
| Packages | lowercase | `net.sakurain.mc.aeternumgenesis.mob` |

#### Architecture Patterns

- **Plugin Entry**: `AeternumGenesisPlugin` initializes managers, registers listeners, commands, and the public API service.
- **Managers**: Each subsystem (`CustomItemManager`, `CustomMobManager`, `SkillManager`, `SpawnManager`) owns loading, caching, and runtime behavior for its templates.
- **API Layer**: `AeternumGenesisAPI` exposes stable interfaces and is registered with Bukkit's `ServicesManager` for cross-plugin access.
- **Events**: All significant actions emit Bukkit events that external plugins can listen to.
- **PDC Storage**: Custom identity, levels, cooldowns, and effects are stored via `PersistentDataContainer` for persistence across chunk unloads and server restarts.

---

## Build & Deployment

### Production Build

```bash
mvn clean package -DskipTests
```

The build produces `target/AeternumGenesis-1.0.0-SNAPSHOT.jar`.

### Build Stages

| Stage | Description |
|-------|-------------|
| 1. Resources | Copy and filter YAML resources into `target/classes` |
| 2. Compile | Compile 152 Java source files with Java 25 target |
| 3. Test | Run unit and integration tests (skipped with `-DskipTests`) |
| 4. Package | Assemble the final plugin JAR |

### Deployment Options

#### Manual Deployment

```bash
# Copy the built JAR to your test server
cp target/AeternumGenesis-1.0.0-SNAPSHOT.jar /path/to/server/plugins/

# Start or restart the server
```

#### Example External Plugin

The `examples/rpg-integration` module demonstrates how to build an external plugin that depends on AeternumGenesis. Build it with:

```bash
cd examples/rpg-integration
mvn clean package -DskipTests
```

This produces `examples/rpg-integration/target/rpg-integration-1.0.0-SNAPSHOT.jar`.

---

## API Reference

### Accessing the API

External plugins can access AeternumGenesis through Bukkit's `ServicesManager`:

```java
import net.sakurain.mc.aeternumgenesis.api.AeternumGenesisAPI;

if (AeternumGenesisAPI.isAvailable()) {
    AeternumGenesisAPI api = AeternumGenesisAPI.getInstance();
    api.getMobAPI().spawnMob("zombie_warrior", location, 5);
}
```

### API Overview

| API | Description |
|-----|-------------|
| `AeternumGenesisAPI` | Central accessor and availability check |
| `ItemAPI` | Build, give, query, and register custom items |
| `MobAPI` | Spawn, query, and register custom mobs |
| `SkillAPI` | Register and trigger skills |
| `SpawnAPI` | Register and unregister spawn rules |
| `SetAPI` | Query item sets and their bonuses |
| `BlockAPI` | Query and register custom blocks |
| `RegistryAPI` | Register custom effects and conditions |
| `AtmosphereAPI` | Apply, remove, and query active atmospheres |

### Spawn a Mob

```java
Optional<LivingEntity> mob = api.getMobAPI().spawnMob("blood_zombie", location, 3);
mob.ifPresent(entity -> entity.setPersistent(true));
```

### Give an Item

```java
Optional<ItemStack> item = api.getItemAPI().buildItem("flaming_sword");
item.ifPresent(stack -> target.getInventory().addItem(stack));
```

### Listen to Events

```java
import net.sakurain.mc.aeternumgenesis.api.event.CustomMobDeathEvent;

@EventHandler
public void onCustomMobDeath(CustomMobDeathEvent event) {
    String id = event.getTemplateId();
    Player killer = event.getKiller();
    if (killer != null) {
        api.getItemAPI().giveItem(killer, "berserker_trophy", 1);
    }
}
```

### Register Custom Effects

```java
api.getRegistryAPI().registerEffect("freeze", () -> new FreezeEffect());
api.getRegistryAPI().registerCondition("has_permission", () -> new HasPermissionCondition());
```

### Apply an Atmosphere

```java
UUID instance = api.getAtmosphereAPI().applyAtmosphere(location, 50.0, "blood_moon_active", 1200L);
```

### Available Events

- `CustomItemBuildEvent`
- `CustomItemSpawnEvent`
- `CustomMobPreSpawnEvent`
- `CustomMobSpawnEvent`
- `CustomMobDeathEvent`
- `CustomMobDropEvent`
- `CustomMobSkillTriggerEvent`
- `CustomMobSkillExecuteEvent`
- `CustomMobDamageModifyEvent`
- `CustomBlockPlaceEvent`
- `CustomBlockBreakEvent`

---

## Configuration Examples

### Custom Item

Create `plugins/AeternumGenesis/items/my_items.yml`:

```yaml
flaming_sword:
  material: DIAMOND_SWORD
  name: "&6Flaming Sword"
  lore:
    - "&7Ignites enemies on hit"
  enchantments:
    FIRE_ASPECT: 2
  attributes:
    - type: ATTACK_DAMAGE
      amount: 8.0
      operation: ADD_NUMBER
      slot: MAIN_HAND
  attack_effects:
    chance: 0.5
    effects:
      - type: ignite
        target: VICTIM
        duration: 60
```

### Custom Mob

Create `plugins/AeternumGenesis/mobs/my_mobs.yml`:

```yaml
flame_zombie:
  type: ZOMBIE
  display_name: "&6Flame Zombie"
  health: 40
  max_health: 40
  glowing: true
  particles:
    - type: FLAME
      location: FEET
      count: 2
      interval: 10
  drops:
    override_vanilla: true
    items:
      - item: rotten_flesh
        amount: "2-4"
        chance: 1.0
    experience: 10
```

### Skill

Create `plugins/AeternumGenesis/skills/my_skills.yml`:

```yaml
fire_strike:
  cooldown: 3.0
  conditions:
    - type: health_percent
      min: 30
      max: 100
  effects:
    - type: damage
      target: TARGET
      amount: 12.0
    - type: ignite
      target: TARGET
      duration: 60
    - type: particle
      particle: FLAME
      location: TARGET
      count: 20
```

### Spawn Rule

Create `plugins/AeternumGenesis/spawns/my_spawns.yml`:

```yaml
blood_zombie_night:
  action: REPLACE
  type: blood_zombie
  replace_types: ZOMBIE
  chance: 0.3
  priority: 20
  worlds: world
  conditions:
    - night true
    - outside true
    - light_level "0-7"
```

### Atmosphere

Create `plugins/AeternumGenesis/atmospheres/my_atmospheres.yml`:

```yaml
blood_moon_active:
  priority: 10
  stackable: false
  layers:
    weather:
      type: "THUNDER"
    potion_effects:
      - type: "DARKNESS"
        duration: 200
        amplifier: 0
        show_particles: false
        show_icon: false
    particles:
      - type: "ASH"
        density: "high"
        pattern: "sphere"
        radius: 30
        offset: "0,3,0"
        interval: 5
    sounds:
      - type: "ambient"
        id: "AMBIENT_CAVE"
        volume: 0.4
        interval: 60
    ui:
      action_bar: "&4Blood Moon erosion: {progress}%"
      boss_bar:
        text: "&c&lBlood Moon Power"
        color: "RED"
        style: "SOLID"
```

Apply in-game:

```
/genesis atmosphere apply blood_moon_active 50 60
```

### Ecosystem

Create `plugins/AeternumGenesis/ecosystems/my_ecosystems.yml`:

```yaml
corrupted_forest:
  biomes:
    - "dark_forest"
    - "old_growth_pine_taiga"
  spawn_rules:
    blood_zombie:
      weight: 40
      group_size: "2-5"
      max_per_chunk: 8
  ambient_particles:
    - type: "ASH"
      density: "medium"
      height: "ground+2"
      interval: 40
```

### World Rules

Create `plugins/AeternumGenesis/worlds/world_rules.yml`:

```yaml
world_rules:
  global:
    weather_cycle: true
    natural_regeneration: false
    mob_griefing: false
  death:
    keep_inventory: false
    death_message_format: "&c{player} &7fell in {world}..."
  player:
    pvp: true
    fall_damage_multiplier: 1.0
    fire_damage_multiplier: 1.0
```

---

## Commands & Permissions

### Player Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/genesis` | `genesis.use` | Base command |
| `/genesis give <item-id> [player] [amount]` | `genesis.give` | Give a custom item |
| `/genesis spawn <mob-id> [player\|x y z] [level]` | `genesis.spawn` | Spawn a custom mob |
| `/genesis reload` | `genesis.reload` | Reload all configs |
| `/genesis list <items\|mobs\|skills\|spawns> [page]` | `genesis.list` | List loaded templates |

### Permission Nodes

```yaml
genesis.use:      default: true
genesis.list:     default: true
genesis.give:     default: op
genesis.spawn:    default: op
genesis.reload:   default: op
genesis.admin:    default: op   # grants all above
```

The base command alias `/gs` is also available.

---

## Troubleshooting

### Build Failures

**Problem**: Maven build fails with dependency resolution errors.

**Solution**:

```bash
# Clear the local Maven cache for this project
rm -rf ~/.m2/repository/net/sakurain/mc/aeternumgenesis
mvn clean package -DskipTests
```

### UnsupportedClassVersionError

**Problem**: Server fails to load the plugin with `UnsupportedClassVersionError`.

**Solution**: Upgrade the server runtime to Java 25 or newer. AeternumGenesis is compiled for Java 25.

### Legacy Material Support Warning

**Problem**: Console shows legacy material support warnings.

**Solution**: Ensure `plugin.yml` contains `api-version: '1.21.1'`. This is already set by default.

### Items or Mobs Not Loading

**Problem**: Custom items or mobs do not appear in-game.

**Solution**:

- Check the console for YAML parse errors.
- Verify file paths are under `plugins/AeternumGenesis/items/`, `mobs/`, `skills/`, `spawns/`, or `sets/`.
- Ensure template IDs are unique across all files of the same type.
- Run `/genesis reload` after making changes.

### Spawn Rules Not Working

**Problem**: Custom mobs do not spawn naturally.

**Solution**:

- For `REPLACE` and `DENY` actions, vanilla mob spawning (`doMobSpawning`) must be enabled.
- Check that `worlds`, `biomes`, and `conditions` match the target environment.
- Verify density limits are not already exceeded.
- Increase `priority` if multiple rules target the same vanilla mob.

### Skills Not Triggering

**Problem**: Mob skills do not execute.

**Solution**:

- Verify the skill ID in the mob's `skills` list matches a loaded skill template.
- Check skill conditions such as health percent, time of day, or cooldown state.
- Ensure the skill is bound to a supported trigger.

---

## Contributing

Contributions are welcome. Please follow this workflow:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`.
3. Make changes following the code style guidelines.
4. Build and test locally: `mvn clean package`.
5. Commit using clear messages: `git commit -m 'feat: add new feature'`.
6. Push to your fork: `git push origin feature/your-feature`.
7. Open a Pull Request against the main branch.

### Code Quality Requirements

Before submitting a Pull Request:

- [ ] The project builds successfully with `mvn clean package -DskipTests`.
- [ ] Code follows project naming conventions.
- [ ] New features include example YAML or documentation updates.
- [ ] API changes include updates to the API reference in the README.
- [ ] No new compiler warnings are introduced.

---

## License

This project is licensed under the MIT License.

```
MIT License

Copyright (c) 2026 Yuyang.Wang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Acknowledgments

This project is built with the help of the following open-source projects and communities:

- [PaperMC](https://papermc.io/) - High-performance Minecraft server software
- [OpenJDK](https://openjdk.org/) - Java development kit
- [Apache Maven](https://maven.apache.org/) - Build and dependency management
- [Adventure](https://docs.advntr.dev/) - User-interface library for Minecraft

---

## Contact

- **Author**: Yuyang.Wang
- **Repository**: [IYeaSakura/AeternumGenesis](https://github.com/IYeaSakura/AeternumGenesis)
- **GitHub**: [https://github.com/IYeaSakura](https://github.com/IYeaSakura)

If you find bugs or have feature requests, please open an [issue](../../issues) or submit a pull request.
