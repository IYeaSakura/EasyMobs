# AeternumGenesis

[![PaperMC](https://img.shields.io/badge/PaperMC-26.1.2-000000?logo=paper-minecraft)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25%2B-007396?logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apache-maven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一款受 MythicMobs 启发的、基于 YAML 配置的轻量级 **PaperMC 26.1.2** 自定义怪物与物品插件。AeternumGenesis 让服主无需编写代码或接触 NMS，即可创建自定义怪物、装备、技能、生成规则以及物品套装。

[功能特性](#功能特性) | [技术栈](#技术栈) | [项目结构](#项目结构) | [快速开始](#快速开始) | [开发指南](#开发指南) | [构建与部署](#构建与部署) | [配置示例](#配置示例) | [命令与权限](#命令与权限) | [API 参考](#api-参考) | [故障排查](#故障排查) | [贡献指南](#贡献指南) | [许可证](#许可证) | [致谢](#致谢) | [联系方式](#联系方式)

---

## 功能特性

### 自定义物品
- 定义带有自定义名称、Lore、附魔、属性、CustomModelData 和发光效果的武器、护甲与工具。
- 添加由手持或穿戴触发的被动效果。
- 配置攻击时按概率触发的攻击效果。
- 将多件物品关联为套装，激活强大的套装奖励。

### 自定义怪物
- 基于任意原版实体类型创建怪物，支持自定义生命值、属性、装备和等级缩放。
- 添加持续粒子、环境音效、BossBar、发光效果和药水效果。
- 配置免疫、水中行为、破门行为、自定义 AI 目标和爆炸地形破坏。
- 定义覆盖或扩展原版战利品的掉落表。
- 全局或按怪物禁用爆炸类生物（如苦力怕）对方块的破坏。

### 技能系统
- 通过 YAML 构建可复用的技能模板，包含冷却、条件与链式效果。
- 使用 23 余种内置效果类型：伤害、百分比伤害、治疗、百分比治疗、药水、清除药水、传送、召唤、粒子、音效、闪电、爆炸、点燃、灭火、击退、消息、标题、ActionBar、掉落物品、执行命令、延迟、**突进**、**范围效果**和**氛围领域**。
- 使用 `and`、`or`、`not` 组合条件。
- 将技能绑定到怪物，支持出生时、命中时、死亡时和定时触发等触发器。

### 自然生成控制
- 使用 `REPLACE`、`ADD`、`DENY` 动作控制自定义怪物如何进入世界。
- 根据昼夜、天气、群系、世界、光照等级、Y 轴高度、月相、脚下方块和随机概率应用条件。
- 通过密度限制防止过度生成。

### 公开开发者 API
- 通过 Bukkit 的 ServicesManager 访问物品、怪物、技能、生成、套装、方块和注册表 API。
- 监听自定义物品构建、怪物生成、怪物死亡、技能执行等 Bukkit 事件。
- 从外部插件注册自定义技能效果、技能条件和生成条件。

### 世界系统
- **氛围引擎** —— 应用分层的区域化氛围，包含天气、药水效果、粒子、音效、UI 层、实体修饰与环境规则。
- **生态系统** —— 按生物群系绑定自定义怪物，支持权重化生成规则、群体大小、密度限制、环境粒子与环境音效。
- **世界规则** —— 按世界控制全局游戏规则、死亡行为、PVP、伤害与饥饿倍率。
- **事件链** —— 编排多阶段世界事件，支持延迟动作、条件判断与成功/失败分支。
- **自定义方块** —— 放置带伪装状态、自定义硬度（基于击打次数并同步客户端破坏动画）、自定义掉落与右键交互的方块。

### 运维特性
- 通过 `/genesis reload` 在游戏内热重载所有 YAML 配置。
- 零 NMS 依赖，完全基于 Bukkit/Paper API 构建，兼容性最佳。
- 通过 Bukkit PersistentDataContainer 持久化存储数据。
- 核心工具类与解析器使用 JUnit 5 进行单元测试。

---

## 技术栈

### 核心技术

| 类别 | 技术 | 版本 |
|------|------|------|
| 服务端平台 | PaperMC | 26.1.2 |
| 语言 | Java | 25+ |
| 构建工具 | Maven | 3.9+ |
| API | Bukkit/Paper API | 26.1.2 |

### 额外库

- **Adventure API**：基于组件的聊天与文本序列化。
- **js-yaml**：文档工具中用于 YAML 验证示例。

---

## 项目结构

```
AeternumGenesis/
├── src/main/java/net/sakurain/mc/aeternumgenesis/   # 主插件源码
│   ├── AeternumGenesisPlugin.java                   # 插件入口
│   ├── api/                                         # 公开 API 接口与事件
│   │   ├── AeternumGenesisAPI.java                  # 中央 API 访问器
│   │   ├── ItemAPI.java                             # 物品查询/构建 API
│   │   ├── MobAPI.java                              # 怪物生成/查询 API
│   │   ├── SkillAPI.java                            # 技能注册 API
│   │   ├── SpawnAPI.java                            # 生成规则 API
│   │   ├── SetAPI.java                              # 物品套装 API
│   │   ├── BlockAPI.java                            # 自定义方块 API
│   │   ├── RegistryAPI.java                         # 效果/条件注册 API
│   │   ├── event/                                   # Bukkit 事件
│   │   ├── exception/                               # 自定义异常
│   │   └── impl/                                    # API 实现
│   ├── command/                                     # 命令执行器与子命令
│   ├── config/                                      # 配置加载与缓存
│   ├── item/                                        # 自定义物品系统
│   ├── item/set/                                    # 物品套装系统
│   ├── item/effect/                                 # 被动与攻击效果处理器
│   ├── mob/                                         # 自定义怪物系统
│   ├── skill/                                       # 技能引擎
│   ├── skill/effect/                                # 内置技能效果
│   ├── skill/condition/                             # 内置技能条件
│   ├── spawn/                                       # 自然生成控制
│   ├── spawn/condition/                             # 内置生成条件
│   ├── block/                                       # 自定义方块系统
│   ├── listener/                                    # 事件监听器
│   ├── ai/                                          # 自定义 AI 目标
│   ├── util/                                        # 工具类
│   ├── atmosphere/                                  # 氛围引擎
│   ├── world/                                       # 世界规则管理器
│   └── eventchain/                                  # 动态事件链系统
├── src/main/resources/                              # 默认配置模板
│   ├── plugin.yml                                   # 插件描述文件
│   ├── config.yml                                   # 主配置
│   ├── items/example_items.yml                      # 物品模板示例
│   ├── mobs/example_mobs.yml                        # 怪物模板示例
│   ├── skills/example_skills.yml                    # 技能模板示例
│   ├── spawns/example_spawns.yml                    # 生成规则示例
│   ├── sets/example_set.yml                         # 物品套装示例
│   ├── blocks/example_blocks.yml                    # 自定义方块示例
│   ├── atmospheres/example_atmosphere.yml           # 氛围示例
│   ├── ecosystems/example_ecosystem.yml             # 生态示例
│   ├── worlds/world_rules.yml                       # 世界规则示例
│   └── events/blood_moon.yml                        # 事件链示例
├── src/test/java/                                   # 单元测试（JUnit 5）
├── test/                                            # 测试服配置模板
├── examples/rpg-integration/                        # 外部插件示例
├── .doc/                                            # 内部文档与技能说明
├── pom.xml                                          # Maven 构建配置
├── package.json                                     # 文档工具 Node 配置
├── README.md                                        # 英文文档
└── README_zh.md                                     # 中文文档
```

---

## 快速开始

### 前置要求

- **服务端**：PaperMC 26.1.2 或兼容构建版本。
- **Java**：25 或更新版本。Java 21 及以下会报 `UnsupportedClassVersionError`。
- **构建工具**：若从源码构建，需要 Maven 3.9 或更新版本。

### 安装

1. 从 [Releases](../../releases) 页面下载最新的 `AeternumGenesis-*.jar`。
2. 将 JAR 文件放入服务端的 `plugins/` 目录。
3. 启动服务端一次，生成默认配置文件夹。
4. 编辑 `plugins/AeternumGenesis/` 下的 YAML 文件以自定义内容。
5. 在游戏内执行 `/genesis reload` 或重启服务端。

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/IYeaSakura/AeternumGenesis.git
cd AeternumGenesis

# 构建插件 JAR
mvn clean package -DskipTests

# 构建产物位于：
# target/AeternumGenesis-1.0.0-SNAPSHOT.jar
```

### 环境变量

AeternumGenesis 正常运行时不需要环境变量，所有行为都通过 `plugins/AeternumGenesis/` 下的 YAML 文件控制。

文档工具所需的 Node.js 依赖可通过以下命令安装：

```bash
npm install
```

---

## 开发指南

### 可用脚本

| 命令 | 说明 |
|------|------|
| `mvn clean package` | 构建插件 JAR |
| `mvn clean package -DskipTests` | 跳过测试构建 |
| `mvn clean verify` | 构建并运行质量检查 |
| `npm install` | 安装文档工具依赖 |

### 测试

运行 JUnit 5 测试套件：

```bash
mvn test
```

测试覆盖纯逻辑组件，包括配置解析器、持续时间解析、ID 校验、颜色解析和事件链条件求值。依赖 Bukkit 的集成测试需要运行中的 Paper 服务端，或兼容 Paper 26.1 的 MockBukkit 构建。

### 代码风格

#### 命名规范

| 项目 | 规范 | 示例 |
|------|------|------|
| 类 | PascalCase | `AeternumGenesisPlugin.java`, `CustomMobManager.java` |
| 方法 | camelCase | `spawnMob`, `registerTemplate` |
| 变量 | camelCase | `templateId`, `itemStack` |
| 常量 | SCREAMING_SNAKE_CASE | `ITEM_ID_KEY`, `DEFAULT_COOLDOWN` |
| 包 | lowercase | `net.sakurain.mc.aeternumgenesis.mob` |

#### 架构模式

- **插件入口**：`AeternumGenesisPlugin` 负责初始化管理器、注册监听器、命令和公开 API 服务。
- **管理器**：每个子系统（`CustomItemManager`、`CustomMobManager`、`SkillManager`、`SpawnManager`）负责各自模板的加载、缓存和运行时行为。
- **API 层**：`AeternumGenesisAPI` 暴露稳定接口，并通过 Bukkit 的 `ServicesManager` 注册，供跨插件访问。
- **事件**：所有重要操作都会发出 Bukkit 事件，外部插件可以监听。
- **PDC 存储**：自定义标识、等级、冷却和效果通过 `PersistentDataContainer` 存储，保证区块卸载和服务器重启后数据不丢失。

---

## 构建与部署

### 生产构建

```bash
mvn clean package -DskipTests
```

构建产物为 `target/AeternumGenesis-1.0.0-SNAPSHOT.jar`。

### 构建阶段

| 阶段 | 说明 |
|------|------|
| 1. 资源 | 将 YAML 资源复制并过滤到 `target/classes` |
| 2. 编译 | 使用 Java 25 目标编译 152 个 Java 源文件 |
| 3. 测试 | 运行单元与集成测试（使用 `-DskipTests` 可跳过） |
| 4. 打包 | 组装最终插件 JAR |

### 部署方式

#### 手动部署

```bash
# 将构建好的 JAR 复制到测试服务器
cp target/AeternumGenesis-1.0.0-SNAPSHOT.jar /path/to/server/plugins/

# 启动或重启服务端
```

#### 外部插件示例

`examples/rpg-integration` 模块演示了如何构建依赖 AeternumGenesis 的外部插件：

```bash
cd examples/rpg-integration
mvn clean package -DskipTests
```

构建产物为 `examples/rpg-integration/target/rpg-integration-1.0.0-SNAPSHOT.jar`。

---

## API 参考

### 访问 API

外部插件可通过 Bukkit 的 `ServicesManager` 访问 AeternumGenesis：

```java
import net.sakurain.mc.aeternumgenesis.api.AeternumGenesisAPI;

if (AeternumGenesisAPI.isAvailable()) {
    AeternumGenesisAPI api = AeternumGenesisAPI.getInstance();
    api.getMobAPI().spawnMob("zombie_warrior", location, 5);
}
```

### API 概览

| API | 说明 |
|-----|------|
| `AeternumGenesisAPI` | 中央访问器与可用性检查 |
| `ItemAPI` | 构建、发放、查询和注册自定义物品 |
| `MobAPI` | 生成、查询和注册自定义怪物 |
| `SkillAPI` | 注册和触发技能 |
| `SpawnAPI` | 注册和注销生成规则 |
| `SetAPI` | 查询物品套装及其奖励 |
| `BlockAPI` | 查询和注册自定义方块 |
| `RegistryAPI` | 注册自定义效果和条件 |
| `AtmosphereAPI` | 应用、移除和查询活跃氛围 |

### 生成怪物

```java
Optional<LivingEntity> mob = api.getMobAPI().spawnMob("blood_zombie", location, 3);
mob.ifPresent(entity -> entity.setPersistent(true));
```

### 发放物品

```java
Optional<ItemStack> item = api.getItemAPI().buildItem("flaming_sword");
item.ifPresent(stack -> target.getInventory().addItem(stack));
```

### 监听事件

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

### 注册自定义效果

```java
api.getRegistryAPI().registerEffect("freeze", () -> new FreezeEffect());
api.getRegistryAPI().registerCondition("has_permission", () -> new HasPermissionCondition());
```

### 应用氛围

```java
UUID instance = api.getAtmosphereAPI().applyAtmosphere(location, 50.0, "blood_moon_active", 1200L);
```

### 可用事件

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

## 配置示例

### 自定义物品

创建 `plugins/AeternumGenesis/items/my_items.yml`：

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
      - type: ignite
        target: VICTIM
        duration: 60
```

### 自定义怪物

创建 `plugins/AeternumGenesis/mobs/my_mobs.yml`：

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

### 技能

创建 `plugins/AeternumGenesis/skills/my_skills.yml`：

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

### 生成规则

创建 `plugins/AeternumGenesis/spawns/my_spawns.yml`：

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

### 氛围

创建 `plugins/AeternumGenesis/atmospheres/my_atmospheres.yml`：

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
      action_bar: "&4血月侵蚀度: {progress}%"
      boss_bar:
        text: "&c&l血月之力"
        color: "RED"
        style: "SOLID"
```

游戏内应用：

```
/genesis atmosphere apply blood_moon_active 50 60
```

### 生态

创建 `plugins/AeternumGenesis/ecosystems/my_ecosystems.yml`：

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

### 自定义方块

创建 `plugins/AeternumGenesis/blocks/example_blocks.yml`：

```yaml
blood_altar:
  material: STONE
  display_name: "&cBlood Altar"
  show_name_tag: true
  disguise:
    type: note_block
    instrument: piano
    note: 12
  hardness: 8
  cancel_physics: true
  cancel_piston: true
  can_break:
    tool_type: PICKAXE
    required_tool_material: IRON
  drops:
    - item: blood_shard
      amount: "2-4"
      chance: 100
  on_interact:
    - type: message
      message: "&cThe altar pulses with dark energy..."
    - type: sound
      sound: BLOCK_BEACON_AMBIENT
      volume: 1.0
      pitch: 0.8
```

**硬度与破坏动画**

- `hardness` 设置破坏方块所需的击打次数。
- 每次击打都会为附近所有玩家更新原版方块裂纹动画。
- 破坏时会产生与材料匹配的击打音效与方块粒子。
- 进度在所有击打该方块的玩家之间共享，多名玩家可协作破坏。
- 当玩家停止挖掘（`BlockDamageAbortEvent`）时，裂纹动画会重置。
- 计数器在方块被破坏或移除时自动重置。

### 世界规则

创建 `plugins/AeternumGenesis/worlds/world_rules.yml`：

```yaml
world_rules:
  global:
    weather_cycle: true
    natural_regeneration: false
    mob_griefing: false
  death:
    keep_inventory: false
    death_message_format: "&c{player} &7在 {world} 陨落了..."
  player:
    pvp: true
    fall_damage_multiplier: 1.0
    fire_damage_multiplier: 1.0
```

### 事件链

创建 `plugins/AeternumGenesis/events/blood_moon.yml`：

```yaml
blood_moon:
  trigger:
    type: random_night
    chance: 0.05
    cooldown: 3d
  stages:
    warning:
      delay: 0
      actions:
        broadcast:
          type: broadcast
          message: "&c&l血月正在升起..."
    spawn_wave:
      delay: 600
      condition: "player_count_online > 1"
      actions:
        spawn:
          type: spawn_around_players
          mob: blood_zombie
          count: 20
          radius: 50
    boss_stage:
      delay: 1200
      condition: "and(mob_count_in_radius(50) < 5, time == night)"
      actions:
        spawn_boss:
          type: spawn_boss
          mob: blood_moon_beast
          key: main
  on_end:
    condition: "boss_defeated(main)"
    timeout_ticks: 24000
    success_actions:
      reward:
        type: reward_all
        item: blood_bottle
        amount: 1
    fail_actions:
      broadcast:
        type: broadcast
        message: "&c血月在未被击败的情况下渐渐消退..."
```

**条件语法**

事件链条件支持比较运算、逻辑运算符、时间关键字与 Boss 状态查询：

| 表达式 | 含义 |
|--------|------|
| `boss_defeated` / `boss_alive` | 任意已注册 Boss 死亡 / 存活时为真。 |
| `boss_defeated(main)` / `boss_alive(main)` | 按 key 查询 Boss 状态，key 需与 `spawn_boss` 动作的 `key` 一致。 |
| `player_count_online > 5` | 在线玩家数量比较。 |
| `time == night` / `time == day` | 时间段关键字判断。还支持 `dawn`、`dusk`、`noon`、`midnight`。 |
| `time > 13000` | 原始世界刻比较。 |
| `elapsed_ticks >= 600` | 事件开始以来的刻数。 |
| `mob_count_in_radius(30) < 10` | 发起者周围指定半径内的非玩家生物数量。 |
| `and(a, b)` / `or(a, b)` / `not(a)` | 布尔逻辑，可嵌套。 |

`condition` 留空或省略时该阶段将始终执行。

### 爆炸地形控制

在 `plugins/AeternumGenesis/config.yml` 中全局禁用所有怪物爆炸破坏方块：

```yaml
explosions:
  disable-mob-terrain-damage: true
```

或在 `plugins/AeternumGenesis/mobs/*.yml` 中按怪物配置：

```yaml
ash_creeper:
  type: CREEPER
  explosion:
    destroy_terrain: false
```

---

## 命令与权限

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/genesis` | `genesis.use` | 基础命令 |
| `/genesis give <item-id> [player] [amount]` | `genesis.give` | 发放自定义物品 |
| `/genesis spawn <mob-id> [player\|x y z] [level]` | `genesis.spawn` | 生成自定义怪物 |
| `/genesis reload` | `genesis.reload` | 重载所有配置 |
| `/genesis list <items\|mobs\|skills\|spawns> [page]` | `genesis.list` | 列出已加载模板 |
| `/genesis atmosphere <apply\|remove\|list\|active>` | `genesis.admin` | 管理活跃氛围 |
| `/genesis event <start\|stop\|list\|templates\|info>` | `genesis.admin` | 管理事件链 |

### 权限节点

```yaml
genesis.use:      default: true
genesis.list:     default: true
genesis.give:     default: op
genesis.spawn:    default: op
genesis.reload:   default: op
genesis.admin:    default: op   # 授予以上所有权限
```

基础命令的简写 `/gs` 同样可用。

---

## 故障排查

### 构建失败

**问题**：Maven 构建时报依赖解析错误。

**解决方案**：

```bash
# 清除本地 Maven 缓存中该项目的依赖
rm -rf ~/.m2/repository/net/sakurain/mc/aeternumgenesis
mvn clean package -DskipTests
```

### UnsupportedClassVersionError

**问题**：服务端加载插件时报 `UnsupportedClassVersionError`。

**解决方案**：将服务端运行环境升级到 Java 25 或更新版本。AeternumGenesis 针对 Java 25 编译。

### 旧版材料支持警告

**问题**：控制台出现旧版材料支持警告。

**解决方案**：确保 `plugin.yml` 包含 `api-version: '1.21.1'`。该值默认已设置。

### 物品或怪物未加载

**问题**：自定义物品或怪物在游戏中未出现。

**解决方案**：

- 检查控制台是否有 YAML 解析错误。
- 确认文件路径位于 `plugins/AeternumGenesis/items/`、`mobs/`、`skills/`、`spawns/` 或 `sets/` 下。
- 确保同类型模板 ID 在所有文件中唯一。
- 修改后执行 `/genesis reload`。

### 生成规则不生效

**问题**：自定义怪物未自然生成。

**解决方案**：

- 对于 `REPLACE` 和 `DENY` 动作，需要开启原版生物生成（`doMobSpawning`）。
- 检查 `worlds`、`biomes` 和 `conditions` 是否与目标环境匹配。
- 确认密度限制未被提前占满。
- 如果多个规则针对同一种原版怪物，可适当提高 `priority`。

### 技能未触发

**问题**：怪物技能没有执行。

**解决方案**：

- 确认怪物 `skills` 列表中的技能 ID 与已加载的技能模板匹配。
- 检查技能条件，如生命值百分比、昼夜状态或冷却状态。
- 确保技能绑定到了支持的触发器。

---

## 贡献指南

欢迎贡献。请按以下流程进行：

1. Fork 本仓库。
2. 创建功能分支：`git checkout -b feature/your-feature`。
3. 按照代码风格指南进行修改。
4. 在本地构建并测试：`mvn clean package`。
5. 使用清晰的提交信息：`git commit -m 'feat: add new feature'`。
6. 推送到你的 Fork：`git push origin feature/your-feature`。
7. 向主分支提交 Pull Request。

### 代码质量要求

提交 Pull Request 前请确认：

- [ ] 使用 `mvn clean package -DskipTests` 能成功构建。
- [ ] 代码遵循项目命名规范。
- [ ] 新功能包含 YAML 示例或文档更新。
- [ ] API 变更包含 README 中 API 参考的更新。
- [ ] 没有引入新的编译器警告。

---

## 许可证

本项目采用 MIT 许可证。

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

## 致谢

本项目得益于以下开源项目和社区的支持：

- [PaperMC](https://papermc.io/) - 高性能 Minecraft 服务端软件
- [OpenJDK](https://openjdk.org/) - Java 开发工具包
- [Apache Maven](https://maven.apache.org/) - 构建与依赖管理
- [Adventure](https://docs.advntr.dev/) - Minecraft 用户界面库

---

## 联系方式

- **作者**：Yuyang.Wang
- **仓库**：[IYeaSakura/AeternumGenesis](https://github.com/IYeaSakura/AeternumGenesis)
- **GitHub**：[https://github.com/IYeaSakura](https://github.com/IYeaSakura)

如发现 Bug 或有功能建议，请提交 [issue](../../issues) 或 Pull Request。
