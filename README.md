# AeternumGenesis

[![PaperMC](https://img.shields.io/badge/PaperMC-26.1.2-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25%2B-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> A lightweight, YAML-driven custom mob and item plugin for **PaperMC 26.1.2**, inspired by MythicMobs. AeternumGenesis lets server owners create custom monsters, equipment, skills, spawn rules, and item sets — all without touching any code or NMS.

---

## Table of Contents

[Features](#features)
[Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
  - [Custom Items](#custom-items)
  - [Custom Mobs](#custom-mobs)
  - [Skills](#skills)
  - [Spawn Rules](#spawn-rules)
  - [Item Sets](#item-sets)
- [Commands & Permissions](#commands--permissions)
- [API for Developers](#api-for-developers)
- [Performance & Safety](#performance--safety)
- [Troubleshooting](#troubleshooting)
- [License](#license)
- [Credits](#credits)

---

## Features

- **Custom Items** — Define weapons, armor, and tools with custom names, lore, enchantments, attributes, CustomModelData, glow, passive effects, attack effects, and set bonuses.
- **Custom Mobs** — Create mobs based on vanilla entity types with custom health, attributes, equipment, particles, sounds, BossBars, immunities, AI behavior, and water behavior.
- **Skill System** — YAML-driven skill engine with 20+ built-in effects, conditions, target selectors, cooldowns, and on-cooldown fallback skills.
- **Natural Spawn Control** — REPLACE, ADD, and DENY spawn rules with time, weather, biome, light-level, Y-level, moon-phase, and density conditions.
- **Item Sets** — Grant bonus effects when players equip a complete set of custom items.
- **Public API** — Stable API with Bukkit events for other plugins to hook into, plus registry extensions for custom effects and conditions.
- **Hot Reload** — Reload all configs in-game with `/genesis reload`.
- **Zero NMS** — Built purely on Bukkit/Paper API for maximum compatibility and maintainability.

---

## Requirements

| Component | Version |
|-----------|---------|
| Server    | PaperMC **26.1.2** |
| Java      | **25** or newer |
| Build     | Maven 3.9+ |

> ⚠️ PaperMC 26.1.2 corresponds to Minecraft Java 26.1.2 (Mojang's year-based versioning). Java 21 and lower will **not** work.

---

## Installation

1. Download the latest `AeternumGenesis-*.jar` from [Releases](../../releases).
2. Place the jar in your server's `plugins/` folder.
3. Start the server once to generate the default config folders.
4. Edit the YAML files in `plugins/AeternumGenesis/` to customize items, mobs, skills, spawns, and sets.
5. Run `/genesis reload` or restart the server.

### Building from Source

```bash
# Clone the repository
git clone https://github.com/IYeaSakura/AeternumGenesis.git
cd AeternumGenesis

# Build with Maven
mvn clean package

# Output:
# target/AeternumGenesis-1.0.0-SNAPSHOT.jar
```

---

## Quick Start

### 1. Create a Custom Item

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
      - type: potion
        target: VICTIM
        potion_type: WITHER
        duration: 100
        amplifier: 1
```

Give it to yourself:

```
/genesis give flaming_sword
```

### 2. Create a Custom Mob

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

Spawn it:

```
/genesis spawn flame_zombie
```

### 3. Bind a Skill

See the [Skills](#skills) section and bind a skill to your mob:

```yaml
flame_zombie:
  # ... other config ...
  skills:
    - trigger: ON_HIT
      skill_id: fire_strike
      chance: 0.3
```

---

## Configuration

All configuration files live under `plugins/AeternumGenesis/`:

```
plugins/AeternumGenesis/
├── config.yml              # Main plugin settings
├── items/                  # Item templates (*.yml)
├── mobs/                   # Mob templates (*.yml)
├── skills/                 # Skill templates (*.yml)
├── spawns/                 # Spawn rules (*.yml)
└── sets/                   # Item set definitions (*.yml)
```

Subdirectories are fully supported.

---

### Custom Items

#### Basic Fields

| Field | Description |
|-------|-------------|
| `material` | Vanilla material name (required) |
| `name` | Display name with `&` color codes |
| `lore` | List of lore lines |
| `amount` | Stack size (default: 1) |
| `custom_model_data` | CustomModelData integer for resource packs |
| `glow` | Enchantment glow effect without actual enchantments |
| `enchantments` | Map of `ENCHANTMENT: level` |
| `hide_enchants` | Hide enchantment lore |
| `attributes` | List of attribute modifiers |
| `unbreakable` | Unbreakable flag |
| `item_flags` | List of item flags e.g. `HIDE_ATTRIBUTES` |

#### Passive Effects (`passive_effects`)

Triggered by `HOLD`, `WEAR`, or `BOTH`:

```yaml
passive_effects:
  - trigger: WEAR
    effects:
      - type: potion
        potion_type: NIGHT_VISION
        duration: -1      # -1 = permanent
        amplifier: 0
      - type: attribute
        attribute: MAX_HEALTH
        amount: 4.0
        operation: ADD_NUMBER
        slot: CHEST
```

#### Attack Effects (`attack_effects`)

Triggered when the item is used to damage an entity:

```yaml
attack_effects:
  chance: 0.3
  effects:
    - type: potion
      target: VICTIM      # VICTIM / SELF / AREA
      potion_type: POISON
      duration: 80
      amplifier: 0
    - type: particle
      target: VICTIM
      particle: WITCH
      count: 5
```

---

### Custom Mobs

#### Basic Fields

| Field | Description |
|-------|-------------|
| `type` | Vanilla entity type (required) |
| `display_name` | Name above the mob's head |
| `health` / `max_health` | Health values |
| `attributes` | Attribute modifiers (e.g. `ATTACK_DAMAGE`, `MOVEMENT_SPEED`) |
| `equipment` | Helmet, chestplate, leggings, boots, main_hand, off_hand |
| `equipment_effects` | Whether mob equipment affects attributes/enchants/special effects |
| `glowing` / `glowing_color` | Glow effect and team color |
| `size` | Size for Slime/Phantom |
| `baby` | Baby form for Ageable mobs |
| `bossbar` | BossBar configuration |
| `particles` | Continuous particle effects |
| `ambient_sound` | Periodic ambient sound |
| `potion_effects` | Permanent potion effects |
| `senses` | Vision/hearing/smell ranges |
| `water_behavior` | Floating, breathing, water speed |
| `immunities` | Damage cause immunities |
| `break_door` | Door-breaking behavior |
| `ai` | Custom AI goals and targeting strategy |
| `drops` | Drop table |
| `skills` | Skill bindings |

#### Equipment Example

```yaml
equipment:
  helmet:
    item: diamond_helmet
    drop_chance: 0.1
  main_hand:
    item: genesis:flaming_sword
    drop_chance: 0.05
```

Use `genesis:item_id` to equip a custom item.

#### BossBar Example

```yaml
bossbar:
  enabled: true
  title: "&cFlame Zombie"
  color: RED
  style: NOTCHED_20
  show_to_all: false
  range: 48
```

---

### Skills

Skills are reusable templates stored in `plugins/AeternumGenesis/skills/`.

```yaml
fire_strike:
  cooldown: 3.0
  on_cooldown_skill: weak_strike    # optional fallback
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
      offset_x: 1.0
      offset_y: 1.0
      offset_z: 1.0
```

#### Built-in Effect Types

| Effect | Description |
|--------|-------------|
| `damage` / `damage_percent` | Direct damage or max-health percentage damage |
| `heal` / `heal_percent` | Heal caster/target |
| `potion` / `potion_clear` | Apply or remove potion effects |
| `teleport` | Teleport target |
| `summon` | Summon other custom mobs |
| `particle` | Spawn particles |
| `sound` | Play sounds |
| `lightning` | Strike lightning |
| `explosion` | Create explosions |
| `ignite` / `extinguish` | Set on fire or put out fire |
| `knockback` | Knockback |
| `message` / `title` / `actionbar` | Send messages |
| `drop_item` | Drop items |
| `execute_command` | Run commands |
| `delay` | Delay between effects |

#### Built-in Conditions

| Condition | Description |
|-----------|-------------|
| `health_percent` / `health` | Caster health checks |
| `target_health_percent` | Target health check |
| `chance` | Extra probability |
| `target_type` | Target entity type |
| `target_distance` | Distance to target |
| `time_of_day` | day/night/dawn/dusk |
| `weather` | clear/raining/thundering |
| `world` / `biome` | World/biome checks |
| `light_level` / `y_above` / `y_below` | Location checks |
| `has_potion` / `is_on_ground` | State checks |
| `mobs_in_radius` | Nearby mob count |
| `and` / `or` / `not` | Composite conditions |

#### Target Selectors

Effects can target: `CASTER`, `TARGET`, `NEARBY`, `ALL_NEARBY`, `OWNER`, `RANDOM_NEARBY`.

---

### Spawn Rules

Spawn rules control how custom mobs enter the world.

#### Action Types

| Action | Description |
|--------|-------------|
| `REPLACE` | Replace vanilla natural spawns |
| `ADD` | Add extra spawns around players |
| `DENY` | Prevent vanilla spawns |

#### Example: Replace Zombies at Night

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

#### Example: Rare Full-Moon Boss

```yaml
zombie_warrior_fullmoon:
  action: ADD
  type: zombie_warrior
  chance: 0.01
  priority: 30
  level: 5
  worlds: world
  biomes: PLAINS,FOREST,SAVANNA
  position_type: LAND
  conditions:
    - night true
    - moon_phase full
    - outside true
    - y_above 60
  density_limits:
    max_per_radius:
      template: zombie_warrior
      amount: 1
      radius: 1000
```

#### Available Conditions

`night`, `day`, `raining`, `thundering`, `outside`, `inside`, `moon_phase`, `y_above`, `y_below`, `y_range`, `light_level`, `block_below`, `time_range`, `random_chance`, `biome`, `world`.

---

### Item Sets

Define set bonuses in `plugins/AeternumGenesis/sets/`:

```yaml
mirror_flower:
  name: "&bMirror Flower Set"
  base_bonus:
    required_count: 4
    required_slots:
      - slot: HEAD
        item: mirror_flower_helmet
      - slot: CHEST
        item: mirror_flower_chestplate
      - slot: LEGS
        item: mirror_flower_leggings
      - slot: FEET
        item: mirror_flower_boots
    effects:
      - type: potion
        potion_type: DAMAGE_RESISTANCE
        duration: -1
        amplifier: 1
  advanced_bonus:
    - name: "&bMirror Flower · Complete"
      extra_requirements:
        - slot: MAIN_HAND
          item: mirror_flower_sword
      effects:
        - type: potion
          potion_type: INCREASE_DAMAGE
          duration: -1
          amplifier: 1
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

---

## API for Developers

AeternumGenesis exposes a public API through Bukkit's `ServicesManager`.

### Getting the API

```java
if (AeternumGenesisAPI.isAvailable()) {
    AeternumGenesisAPI api = AeternumGenesisAPI.getInstance();
    api.getMobAPI().spawnMob("zombie_warrior", location, 5);
}
```

### Spawn a Mob

```java
Optional<LivingEntity> mob = api.getMobAPI().spawnMob("blood_zombie", location, 3);
mob.ifPresent(entity -> entity.setPersistent(true));
```

### Listen to Events

```java
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

### API Events

- `CustomItemSpawnEvent`
- `CustomItemBuildEvent`
- `CustomMobPreSpawnEvent`
- `CustomMobSpawnEvent`
- `CustomMobDeathEvent`
- `CustomMobDropEvent`
- `CustomMobSkillTriggerEvent`
- `CustomMobSkillExecuteEvent`
- `CustomMobDamageModifyEvent`

---

## Performance & Safety

- All YAML configs are loaded once at startup or `/reload` and cached in memory.
- Custom mobs are tracked via `PersistentDataContainer`, avoiding memory leaks.
- Skill cooldowns are stored in the mob's PDC, not in memory maps.
- Particle effects respect a configurable visibility range.
- ADD-mode spawns are throttled by player, distance, attempts, and density limits.
- All file I/O uses try-with-resources and per-file error handling.
- Attribute values are validated to prevent overflow.

---

## Troubleshooting

### "UnsupportedClassVersionError"
Your server is running Java older than 25. Upgrade to Java 25+.

### "Legacy Material Support" warning
Make sure `plugin.yml` contains `api-version: '26.1.2'` (already set by default).

### Mobs/items not loading
- Check the console for YAML parse errors.
- Verify file paths are under `plugins/AeternumGenesis/items/`, `mobs/`, `skills/`, `spawns/`, or `sets/`.
- Ensure template IDs are unique across all files of the same type.

### Spawn rules not working
- For `REPLACE`/`DENY`, vanilla mob spawning (`doMobSpawning`) must be enabled.
- Check that `worlds`, `biomes`, and `conditions` match your target environment.
- Verify density limits are not already exceeded.

---

## License

This project is licensed under the [MIT License](LICENSE).

```
Copyright (c) 2026 Yuyang.Wang
```

---

## Credits

- **Author:** Yuyang.Wang
- **Repository:** [IYeaSakura/AeternumGenesis](https://github.com/IYeaSakura/AeternumGenesis)
- **Powered by:** [PaperMC](https://papermc.io/)

If you find bugs or have feature requests, please open an [issue](../../issues) or submit a pull request.
