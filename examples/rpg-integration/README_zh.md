# EasyMobs RPG 集成示例

本目录包含一个最小可运行的 Paper 插件示例，展示**外部插件**如何通过公开的 `EasyMobsAPI` 自定义 EasyMobs 的每一个部分——无需在 `plugins/EasyMobs/` 中放置任何 YAML 文件。

## 示例内容

| EasyMobs 功能 | 使用的 API | 示例行为 |
|---|---|---|
| **物品** | `ItemAPI#registerTemplate` / `unregisterTemplate` | 运行时注册 `rpg_fiery_longsword` 与 `rpg_guardian_shield` |
| **怪物** | `MobAPI#registerTemplate` / `unregisterTemplate` | 注册带有阵营、AI 目标、装备与 BossBar 的 `rpg_fallen_knight` 和 `rpg_necromancer` |
| **技能** | `SkillAPI#registerSkill` / `unregisterSkill` | 注册 `rpg_necrotic_blast` 与 `rpg_fireworks` |
| **生成规则** | `SpawnAPI#registerRule` / `unregisterRule` | 在黑暗、地下的区域将骷髅/凋灵骷髅替换为 `rpg_fallen_knight` |
| **自定义效果** | `SkillAPI#registerEffect` | 新增一个 `launch_firework` 效果类型 |
| **指令** | `ItemAPI#buildItem`、`MobAPI#spawnMob`、`SkillAPI#triggerSkill` | `/rpgitem`、`/rpgmob`、`/rpgskill` |

## 项目结构

```
examples/rpg-integration/
├── pom.xml
├── README.md
└── src/main/
    ├── java/net/sakurain/mc/easymobs/examples/rpg/
    │   ├── RpgIntegrationPlugin.java   # 主插件 + 指令处理
    │   └── LaunchFireworkEffect.java   # 自定义技能效果
    └── resources/plugin.yml
```

## 构建

1. 先构建 EasyMobs，确保本地 JAR 存在：

   ```bash
   cd ../..
   mvn clean package -DskipTests
   ```

2. 构建示例插件：

   ```bash
   cd examples/rpg-integration
   mvn clean package -DskipTests
   ```

输出文件为 `target/rpg-integration-1.0.0-SNAPSHOT.jar`。

## 安装

将两个 JAR 都复制到 Paper 服务器：

```
server/plugins/
├── EasyMobs-1.0.0-SNAPSHOT.jar
└── rpg-integration-1.0.0-SNAPSHOT.jar
```

`EasyMobsRpgIntegration` 在 `plugin.yml` 中声明了 `depend: [EasyMobs]`，因此 Paper 会先加载 EasyMobs。

## 游戏内使用

```
/rpgitem <玩家名> rpg_fiery_longsword
/rpgitem <玩家名> rpg_guardian_shield
/rpgmob rpg_fallen_knight
/rpgmob rpg_necromancer
/rpgskill rpg_fireworks
```

此外，在 `world` 或 `world_nether` 中，光照低于 7 且 Y 低于 64 时，骷髅/凋灵骷髅自然生成有几率变为 Fallen Knight。

## 如何获取 API

```java
EasyMobsAPI api = EasyMobsAPI.getInstance();
ItemAPI  itemAPI  = api.getItemAPI();
MobAPI   mobAPI   = api.getMobAPI();
SkillAPI skillAPI = api.getSkillAPI();
SpawnAPI spawnAPI = api.getSpawnAPI();
```

`EasyMobsAPI#getInstance()` 通过 Bukkit 的 `ServicesManager` 读取 EasyMobs 注册的服务。

## 运行时注册模式

所有模板都在插件启用时基于 Bukkit 的 `MemoryConfiguration` 构建并注册。例如一个怪物：

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

键名与值格式和 EasyMobs YAML 配置完全一致（物品、怪物、技能、生成规则）。

## 自定义技能效果

要添加 EasyMobs 尚未内置的效果类型，实现公开的 `SkillEffect` 接口并注册工厂：

```java
api.getSkillAPI().registerEffect("launch_firework", LaunchFireworkEffect::new);
```

之后该效果可以在任意技能配置中使用：

```yaml
effects:
  - type: launch_firework
    params:
      color: PURPLE
      fade: GREEN
      power: 2
```

## 清理

`onDisable()` 会注销所有运行时注册的模板，确保插件重载不会留下过期内容。

## 备注

- 本示例通过 Maven 的 `system` scope 依赖 `target/easymobs-1.0.0-SNAPSHOT.jar`。在实际项目中，应将 EasyMobs 安装到本地/远程 Maven 仓库，或改用多模块构建。
- 属性 `operation: SET_VALUE` 让配置值直接等于原版提示条显示值（例如 `1.6` 攻击速度）。
- 阵营 `undead_legion` 可防止示例怪物互相攻击。
