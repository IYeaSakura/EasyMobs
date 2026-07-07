# EasyMobs

[![PaperMC](https://img.shields.io/badge/PaperMC-26.1.2-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25%2B-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 一款受 MythicMobs 启发的、基于 YAML 配置的轻量级 **PaperMC 26.1.2** 自定义怪物与物品插件。EasyMobs 让服主无需编写代码或接触 NMS，即可创建自定义怪物、装备、技能、生成规则以及物品套装。

---

## 目录

- [功能特性](#功能特性)
- [环境要求](#环境要求)
- [安装](#安装)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
  - [自定义物品](#自定义物品)
  - [自定义怪物](#自定义怪物)
  - [技能](#技能)
  - [生成规则](#生成规则)
  - [物品套装](#物品套装)
- [指令与权限](#指令与权限)
- [开发者 API](#开发者-api)
- [性能与安全](#性能与安全)
- [常见问题](#常见问题)
- [许可证](#许可证)
- [致谢](#致谢)

---

<a id="功能特性"></a>
## 功能特性

- **自定义物品** —— 定义带有自定义名称、Lore、附魔、属性、CustomModelData、发光效果、被动效果、攻击效果和套装加成的武器、护甲与工具。
- **自定义怪物** —— 基于原版实体类型创建怪物，可自定义生命值、属性、装备、粒子、音效、BossBar、免疫效果、AI 行为以及水中行为。
- **技能系统** —— 基于 YAML 的技能引擎，内置 20 余种效果、条件、目标选择器、冷却机制以及冷却中回退技能。
- **自然生成控制** —— REPLACE（替换）、ADD（追加）、DENY（阻止）三种生成规则，支持时间、天气、生物群系、光照、Y 轴、月相和密度条件。
- **物品套装** —— 当玩家装备完整套装时触发额外效果。
- **公开 API** —— 稳定的 API，通过 Bukkit 事件供其他插件接入，并支持注册自定义效果与条件。
- **热重载** —— 游戏内使用 `/ezmobs reload` 即可重载所有配置。
- **零 NMS** —— 完全基于 Bukkit/Paper API 构建，兼容性高、易于维护。

---

<a id="环境要求"></a>
## 环境要求

| 组件 | 版本 |
|-----------|---------|
| 服务端 | PaperMC **26.1.2** |
| Java | **25** 或更高 |
| 构建工具 | Maven 3.9+ |

> ⚠️ PaperMC 26.1.2 对应 Minecraft Java 版 26.1.2（Mojang 的年度版本号）。Java 21 及更低版本**无法**运行。

---

<a id="安装"></a>
## 安装

1. 从 [Releases](../../releases) 下载最新的 `EasyMobs-*.jar`。
2. 将 jar 文件放入服务器的 `plugins/` 文件夹。
3. 启动服务器一次，插件会自动生成默认配置文件夹。
4. 编辑 `plugins/EasyMobs/` 下的 YAML 文件，自定义物品、怪物、技能、生成规则和套装。
5. 执行 `/ezmobs reload` 或重启服务器。

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/IYeaSakura/EasyMobs.git
cd EasyMobs

# 使用 Maven 构建
mvn clean package

# 输出文件：
# target/EasyMobs-1.0.0-SNAPSHOT.jar
```

---

<a id="快速开始"></a>
## 快速开始

### 1. 创建自定义物品

创建文件 `plugins/EasyMobs/items/my_items.yml`：

```yaml
flaming_sword:
  material: DIAMOND_SWORD
  name: "&6烈焰之剑"
  lore:
    - "&7击中敌人时点燃目标"
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

给自己发放该物品：

```
/ezmobs give flaming_sword
```

### 2. 创建自定义怪物

创建文件 `plugins/EasyMobs/mobs/my_mobs.yml`：

```yaml
flame_zombie:
  type: ZOMBIE
  display_name: "&6烈焰僵尸"
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

召唤它：

```
/ezmobs spawn flame_zombie
```

### 3. 绑定技能

参考 [技能](#技能) 章节，将技能绑定到怪物：

```yaml
flame_zombie:
  # ... 其他配置 ...
  skills:
    - trigger: ON_HIT
      skill_id: fire_strike
      chance: 0.3
```

---

<a id="配置说明"></a>
## 配置说明

所有配置文件位于 `plugins/EasyMobs/`：

```
plugins/EasyMobs/
├── config.yml              # 主插件配置
├── items/                  # 物品模板 (*.yml)
├── mobs/                   # 怪物模板 (*.yml)
├── skills/                 # 技能模板 (*.yml)
├── spawns/                 # 生成规则 (*.yml)
└── sets/                   # 物品套装定义 (*.yml)
```

支持任意子目录。

---

<a id="自定义物品"></a>
### 自定义物品

#### 基础字段

| 字段 | 说明 |
|-------|-------------|
| `material` | 原版材料名称（必填） |
| `name` | 显示名称，支持 `&` 颜色代码 |
| `lore` | Lore 行列表 |
| `amount` | 堆叠数量（默认：1） |
| `custom_model_data` | 用于资源包的 CustomModelData 整数值 |
| `glow` | 仅显示附魔光效，不带实际附魔 |
| `enchantments` | `ENCHANTMENT: 等级` 的映射 |
| `hide_enchants` | 隐藏附魔 Lore |
| `attributes` | 属性修饰符列表 |
| `unbreakable` | 无法破坏标记 |
| `item_flags` | 物品标签列表，例如 `HIDE_ATTRIBUTES` |

#### 被动效果（`passive_effects`）

由 `HOLD`（手持）、`WEAR`（穿戴）或 `BOTH`（两者）触发：

```yaml
passive_effects:
  - trigger: WEAR
    effects:
      - type: potion
        potion_type: NIGHT_VISION
        duration: -1      # -1 = 永久
        amplifier: 0
      - type: attribute
        attribute: MAX_HEALTH
        amount: 4.0
        operation: ADD_NUMBER
        slot: CHEST
```

#### 攻击效果（`attack_effects`）

当该物品用于攻击实体时触发：

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

<a id="自定义怪物"></a>
### 自定义怪物

#### 基础字段

| 字段 | 说明 |
|-------|-------------|
| `type` | 原版实体类型（必填） |
| `display_name` | 怪物头顶显示的名称 |
| `health` / `max_health` | 生命值 |
| `attributes` | 属性修饰符（例如 `ATTACK_DAMAGE`、`MOVEMENT_SPEED`） |
| `equipment` | 头盔、胸甲、护腿、靴子、主手、副手 |
| `equipment_effects` | 怪物装备是否生效（属性/附魔/特殊效果） |
| `glowing` / `glowing_color` | 发光效果与队伍颜色 |
| `size` | 史莱姆/幻翼的尺寸 |
| `baby` | 可成长怪物的幼年形态 |
| `bossbar` | BossBar 配置 |
| `particles` | 持续播放的粒子效果 |
| `ambient_sound` | 周期性环境音效 |
| `potion_effects` | 永久药水效果 |
| `senses` | 视觉/听觉/嗅觉范围 |
| `water_behavior` | 漂浮、呼吸、水中移动速度 |
| `immunities` | 伤害来源免疫 |
| `break_door` | 破门行为 |
| `ai` | 自定义 AI 目标与寻路策略 |
| `drops` | 掉落表 |
| `skills` | 技能绑定 |

#### 装备示例

```yaml
equipment:
  helmet:
    item: diamond_helmet
    drop_chance: 0.1
  main_hand:
    item: ezmobs:flaming_sword
    drop_chance: 0.05
```

使用 `ezmobs:item_id` 为怪物装备自定义物品。

#### BossBar 示例

```yaml
bossbar:
  enabled: true
  title: "&c烈焰僵尸"
  color: RED
  style: NOTCHED_20
  show_to_all: false
  range: 48
```

---

<a id="技能"></a>
### 技能

技能是可复用的模板，存放在 `plugins/EasyMobs/skills/`。

```yaml
fire_strike:
  cooldown: 3.0
  on_cooldown_skill: weak_strike    # 可选的回退技能
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

#### 内置效果类型

| 效果 | 说明 |
|--------|-------------|
| `damage` / `damage_percent` | 直接伤害或基于最大生命值的百分比伤害 |
| `heal` / `heal_percent` | 治疗施法者/目标 |
| `potion` / `potion_clear` | 施加或移除药水效果 |
| `teleport` | 传送目标 |
| `summon` | 召唤其他自定义怪物 |
| `particle` | 生成粒子 |
| `sound` | 播放音效 |
| `lightning` | 召唤闪电 |
| `explosion` | 创建爆炸 |
| `ignite` / `extinguish` | 点燃或熄灭火焰 |
| `knockback` | 击退 |
| `message` / `title` / `actionbar` | 发送消息 |
| `drop_item` | 掉落物品 |
| `execute_command` | 执行指令 |
| `delay` | 效果之间的延迟 |

#### 内置条件

| 条件 | 说明 |
|-----------|-------------|
| `health_percent` / `health` | 施法者生命值检查 |
| `target_health_percent` | 目标生命值检查 |
| `chance` | 额外概率 |
| `target_type` | 目标实体类型 |
| `target_distance` | 与目标的距离 |
| `time_of_day` | day/night/dawn/dusk |
| `weather` | clear/raining/thundering |
| `world` / `biome` | 世界/生物群系检查 |
| `light_level` / `y_above` / `y_below` | 位置检查 |
| `has_potion` / `is_on_ground` | 状态检查 |
| `mobs_in_radius` | 附近怪物数量 |
| `and` / `or` / `not` | 复合条件 |

#### 目标选择器

效果可指向：`CASTER`、`TARGET`、`NEARBY`、`ALL_NEARBY`、`OWNER`、`RANDOM_NEARBY`。

---

<a id="生成规则"></a>
### 生成规则

生成规则控制自定义怪物如何进入世界。

#### 动作类型

| 动作 | 说明 |
|--------|-------------|
| `REPLACE` | 替换原版自然生成 |
| `ADD` | 在玩家附近追加额外生成 |
| `DENY` | 阻止原版生成 |

#### 示例：夜间替换僵尸

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

#### 示例：满月稀有 Boss

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

#### 可用条件

`night`、`day`、`raining`、`thundering`、`outside`、`inside`、`moon_phase`、`y_above`、`y_below`、`y_range`、`light_level`、`block_below`、`time_range`、`random_chance`、`biome`、`world`。

---

<a id="物品套装"></a>
### 物品套装

在 `plugins/EasyMobs/sets/` 定义套装奖励：

```yaml
mirror_flower:
  name: "&b镜花套装"
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
    - name: "&b镜花 · 圆满"
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

<a id="指令与权限"></a>
## 指令与权限

### 玩家指令

| 指令 | 权限 | 说明 |
|---------|------------|-------------|
| `/ezmobs` | `easymobs.use` | 基础指令 |
| `/ezmobs give <item-id> [player] [amount]` | `easymobs.give` | 发放自定义物品 |
| `/ezmobs spawn <mob-id> [player\|x y z] [level]` | `easymobs.spawn` | 召唤自定义怪物 |
| `/ezmobs reload` | `easymobs.reload` | 重载所有配置 |
| `/ezmobs list <items\|mobs\|skills\|spawns> [page]` | `easymobs.list` | 列出已加载的模板 |

### 权限节点

```yaml
easymobs.use:      default: true
easymobs.list:     default: true
easymobs.give:     default: op
easymobs.spawn:    default: op
easymobs.reload:   default: op
easymobs.admin:    default: op   # 授予以上所有权限
```

---

<a id="开发者-api"></a>
## 开发者 API

EasyMobs 通过 Bukkit 的 `ServicesManager` 暴露公开 API。

### 获取 API

```java
if (EasyMobsAPI.isAvailable()) {
    EasyMobsAPI api = EasyMobsAPI.getInstance();
    api.getMobAPI().spawnMob("zombie_warrior", location, 5);
}
```

### 召唤怪物

```java
Optional<LivingEntity> mob = api.getMobAPI().spawnMob("blood_zombie", location, 3);
mob.ifPresent(entity -> entity.setPersistent(true));
```

### 监听事件

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

### 注册自定义效果

```java
api.getRegistryAPI().registerEffect("freeze", () -> new FreezeEffect());
api.getRegistryAPI().registerCondition("has_permission", () -> new HasPermissionCondition());
```

### API 事件

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

<a id="性能与安全"></a>
## 性能与安全

- 所有 YAML 配置在启动或 `/reload` 时一次性加载并缓存到内存。
- 自定义怪物通过 `PersistentDataContainer` 追踪，避免内存泄漏。
- 技能冷却存储在怪物 PDC 中，而非内存映射。
- 粒子效果遵守可配置的可视距离。
- ADD 模式生成受玩家、距离、尝试次数和密度限制的多重节流。
- 所有文件 I/O 使用 try-with-resources，并针对单个文件进行错误处理。
- 属性值经过校验，防止溢出。

---

<a id="常见问题"></a>
## 常见问题

### "UnsupportedClassVersionError"
你的服务端 Java 版本低于 25。请升级到 Java 25+。

### "Legacy Material Support" 警告
请确保 `plugin.yml` 包含 `api-version: '26.1.2'`（默认已设置）。

### 怪物/物品未加载
- 查看控制台是否有 YAML 解析错误。
- 确认文件路径位于 `plugins/EasyMobs/items/`、`mobs/`、`skills/`、`spawns/` 或 `sets/` 下。
- 确保同类型模板 ID 在所有文件中唯一。

### 生成规则不生效
- 对于 `REPLACE`/`DENY`，必须开启原版生物生成（`doMobSpawning`）。
- 检查 `worlds`、`biomes` 和 `conditions` 是否匹配目标环境。
- 确认密度限制未被触发。

---

<a id="许可证"></a>
## 许可证

本项目基于 [MIT License](LICENSE) 开源。

```
Copyright (c) 2026 Yuyang.Wang
```

---

<a id="致谢"></a>
## 致谢

- **作者：** Yuyang.Wang
- **仓库：** [IYeaSakura/EasyMobs](https://github.com/IYeaSakura/EasyMobs)
- **技术支持：** [PaperMC](https://papermc.io/)

如果发现 Bug 或有功能建议，欢迎提交 [issue](../../issues) 或 pull request。
