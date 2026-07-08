package net.sakurain.mc.aeternumgenesis.eventchain;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventChainTemplateParsingTest {

    private EventChainTemplate parse(String yaml) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        return EventChainTemplate.fromConfig("test_event", config);
    }

    @Test
    void parsesTrigger() {
        EventChainTemplate template = parse("""
                test_event:
                  trigger:
                    type: random_night
                    chance: 0.05
                    cooldown: 3d
                    min_players: 2
                """);
        assertEquals(EventChainTemplate.Trigger.Type.RANDOM_NIGHT, template.trigger().type());
        assertEquals(0.05, template.trigger().chance(), 0.001);
        assertEquals(2, template.trigger().minPlayers());
        assertTrue(template.trigger().cooldownTicks() > 0);
    }

    @Test
    void parsesStages() {
        EventChainTemplate template = parse("""
                test_event:
                  stages:
                    warning:
                      id: warning
                      delay: 10
                      actions:
                        broadcast:
                          type: broadcast
                          message: "Hello"
                    spawn:
                      delay: 100
                      condition: "player_count_online > 5"
                      actions:
                        spawn:
                          type: spawn_around_players
                          mob: zombie
                          count: 5
                """);
        assertEquals(2, template.stages().size());
        assertEquals("warning", template.stages().get(0).id());
        assertEquals(10, template.stages().get(0).delayTicks());
        assertEquals(1, template.stages().get(0).actions().size());
        assertEquals("broadcast", template.stages().get(0).actions().get(0).type());
        assertEquals("Hello", template.stages().get(0).actions().get(0).parameters().get("message"));

        assertEquals("spawn", template.stages().get(1).id());
        assertFalse(template.stages().get(1).condition().isBlank());
        assertEquals("player_count_online > 5", template.stages().get(1).condition().expression());
    }

    @Test
    void parsesEndConfig() {
        EventChainTemplate template = parse("""
                test_event:
                  on_end:
                    condition: boss_defeated
                    timeout_ticks: 12000
                    success_actions:
                      reward:
                        type: reward_all
                        item: diamond
                        amount: 1
                    fail_actions:
                      punish:
                        type: punish_all
                        effect: WEAKNESS
                        duration: 6000
                """);
        assertNotNull(template.onEnd());
        assertEquals("boss_defeated", template.onEnd().condition().expression());
        assertEquals(12000, template.onEnd().timeoutTicks());
        assertEquals(1, template.onEnd().successActions().size());
        assertEquals(1, template.onEnd().failActions().size());
    }

    @Test
    void emptyConfigUsesDefaults() {
        EventChainTemplate template = parse("");
        assertEquals(EventChainTemplate.Trigger.Type.MANUAL, template.trigger().type());
        assertTrue(template.stages().isEmpty());
        assertNotNull(template.onEnd());
    }
}
