package de.aethos.lib.blocks.event.listener;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.event.CustomBlockBreakEvent;
import de.aethos.lib.option.Some;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class CustomBlockBreakListener implements Listener {
    public static final CustomBlockBreakListener INSTANCE = new CustomBlockBreakListener();
    private final Map<Block, ScheduledTask> DATA = new ConcurrentHashMap<>();
    private final Map<Block, ScheduledTask> DISPLAY = new ConcurrentHashMap<>();

    @EventHandler
    public void onBlockDamage(final BlockDamageEvent event) {
        if (CustomBlock.get(event.getBlock()) instanceof Some<? extends CustomBlock> some) {
            if (DATA.containsKey(event.getBlock())) {
                //TODO CHECK IF BLOCK IS INSTA BREAKING
                AethosLib.getPlugin(AethosLib.class).getLogger().info("CustomBlockBreakListener onBlockDamage may be insta-breaking or run into an error");
                return;
            }
            final CustomBlock custom = some.value();
            double speed = 20 * BreakUtil.apply(event.getPlayer(), custom.getDestroySpeed(event.getItemInHand()));
            int ticks = (int) Math.max(speed, 1);
            ScheduledTask task = Bukkit.getRegionScheduler().runDelayed(AethosLib.getPlugin(AethosLib.class), event.getBlock().getLocation(), scheduledTask -> {
                if (new CustomBlockBreakEvent(custom, event.getBlock(), event.getPlayer()).callEvent()) {
                    some.value().onRemove();
                    CustomBlock.remove(some.value());
                }
            }, ticks);
            DATA.put(event.getBlock(), task);
            AtomicInteger i = new AtomicInteger(1);
            ScheduledTask display_task = Bukkit.getRegionScheduler().runAtFixedRate(AethosLib.getPlugin(AethosLib.class), event.getBlock().getLocation(), scheduledTask -> {
                        i.getAndIncrement();
                        new BlockBreakProgressUpdateEvent(event.getBlock(), (float) i.get() / ticks, event.getPlayer()).callEvent();
                    }
                    , 1, 5);
            DISPLAY.put(event.getBlock(), display_task);
        }
    }

    @EventHandler
    public void onBreakEnd(final BlockDamageAbortEvent event) {
        ScheduledTask task = DATA.remove(event.getBlock());
        ScheduledTask display_task = DISPLAY.remove(event.getBlock());
        if (task == null) {
            return;
        }
        if (task.isCancelled()) {
            return;
        }
        display_task.cancel();
        task.cancel();
    }


    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (CustomBlock.get(event.getEntity()) instanceof Some<? extends CustomBlock> some) {
            CustomBlock custom = some.value();
            custom.onRemove();
            CustomBlock.remove(custom);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event instanceof CustomBlockBreakEvent ignore) {
            return;
        }
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            if (CustomBlock.get(event.getBlock()) instanceof Some<? extends CustomBlock> some) {
                CustomBlock custom = some.value();
                custom.onRemove();
                CustomBlock.remove(custom);
                return;
            }
        }
        event.setCancelled(CustomBlock.exists(event.getBlock()));
    }

    public interface BreakUtil {

        static double apply(Player player, float speed) {
            speed = applyPotion(player, speed);
            speed = applyHelmet(player, speed);
            speed = applyGround(player, speed);
            return speed;
        }

        static float applyGround(Player player, float speed) {
            if (!player.isOnGround()) {
                speed /= 5;
            }
            return speed;
        }

        static float applyHelmet(Player player, float speed) {
            if (player.isUnderWater()) {
                final ItemStack helmet = player.getInventory().getHelmet();
                if (helmet == null || !helmet.getEnchantments().containsKey(Enchantment.AQUA_AFFINITY)) {
                    speed /= 5;
                }
            }
            return speed;
        }

        static float applyPotion(Player player, float speed) {
            if (player.hasPotionEffect(PotionEffectType.HASTE)) {
                speed *= 0.2 * player.getPotionEffect(PotionEffectType.HASTE).getAmplifier() + 1;
            }
            if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                speed *= Math.pow(0.3, Math.min(player.getPotionEffect(PotionEffectType.MINING_FATIGUE).getAmplifier() + 1, 4));
            }
            return speed;
        }
    }
}
