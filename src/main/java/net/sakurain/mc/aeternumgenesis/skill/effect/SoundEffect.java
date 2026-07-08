package net.sakurain.mc.aeternumgenesis.skill.effect;

import net.sakurain.mc.aeternumgenesis.skill.SkillContext;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class SoundEffect extends AbstractSkillEffect {

    public SoundEffect() {
        super("sound");
    }

    @Override
    public void execute(SkillContext context) {
        String soundName = string("sound", "").toLowerCase();
        if (soundName.isEmpty()) {
            return;
        }
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName));
        if (sound == null) {
            return;
        }
        Location location = location(context);
        if (location == null || location.getWorld() == null) {
            return;
        }
        float volume = (float) number("volume", 1.0);
        float pitch = (float) number("pitch", 1.0);
        SoundCategory category = SoundCategory.MASTER;
        try {
            category = SoundCategory.valueOf(string("category", "MASTER").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        location.getWorld().playSound(location, sound, category, volume, pitch);
    }
}
