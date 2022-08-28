package net.teamuni.playerjoinandleave;

import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class UtilTool extends JavaPlugin implements Listener {

    String joinMessage = "";
    String leaveMessage = "";
    String firstTimeJoinMessage = "";
    String shiftRightClickCommand = "";
    List<String> commandsList;
    World world;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        getConfigMessages();
        CommandsManager.createCommandsYml();
        PlayerUuidManager.createCommandsYml();
        IgnorePlayerManager.createCommandsYml();
        registerCommands();
        BroadCasterCooldown.setupCooldown();
        getCommand("utiltool").setTabCompleter(new CommandTabCompleter());
        getSpawnInfo();
    }

    @Override
    public void onDisable() {
        PlayerUuidManager.save();
        IgnorePlayerManager.save();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        Player player = (Player) sender;
        String[] spawn = {"spawn", "tmvhs", "스폰", "넴주"};
        String[] whisper = {"귓", "귓속말", "rnlt", "rnltthrakf", "r", "w", "m", "msg", "whisper"};

        if (cmd.getName().equalsIgnoreCase("utiltool") && player.hasPermission("utiltool.reload")) {
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                saveConfig();
                getConfigMessages();
                CommandsManager.reload();
                CommandsManager.save();
                PlayerUuidManager.save();
                PlayerUuidManager.reload();
                IgnorePlayerManager.save();
                IgnorePlayerManager.reload();
                registerCommands();
                getSpawnInfo();
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "UtilTool has been reloaded!");
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("setspawn") && player.hasPermission("utiltool.setspawn")) {
            getConfig().set("spawnpoint.world", Objects.requireNonNull(player.getLocation().getWorld()).getName());
            getConfig().set("spawnpoint.x", player.getLocation().getX());
            getConfig().set("spawnpoint.y", player.getLocation().getY());
            getConfig().set("spawnpoint.z", player.getLocation().getZ());
            getConfig().set("spawnpoint.yaw", player.getLocation().getYaw());
            getConfig().set("spawnpoint.pitch", player.getLocation().getPitch());
            saveConfig();
            getSpawnInfo();
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Respawn point has been set!");
            return false;
        }
        if (Arrays.asList(spawn).contains(cmd.getName()) && player.hasPermission("utiltool.spawn")) {
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "이동 중...");
            player.teleport(new Location(world, x, y, z, yaw, pitch));
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("채팅청소") && player.hasPermission("utiltool.mychatclear")) {
            for (int myChatClearCount = 0; myChatClearCount < 100; myChatClearCount++) {
                player.sendMessage("");
            }
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your chat has been cleaned!");
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("전체채팅청소") && player.hasPermission("utiltool.allchatclear")) {
            for (int allChatClearCount = 0; allChatClearCount < 100; allChatClearCount++) {
                getServer().broadcastMessage("");
            }
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "All Chat has been cleaned!");
            return false;
        }

        if (Arrays.asList(whisper).contains(cmd.getName()) && player.hasPermission("utiltool.whisper")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (!IgnorePlayerManager.get().getStringList("Ignores").contains(args[0] + "." + player.getName())) {
                String targetMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (target != null) {
                    target.sendMessage("§e[ §6" + player.getName() + " §f→ §c나 §e]§f " + targetMsg);
                    player.sendMessage("§e[ §c나" + " §f→ §6" + args[0] + " §e]§f " + targetMsg);
                } else {
                    player.sendMessage("§e[알림] §f서버에 존재하지 않는 플레이어입니다!");
                }
            } else {
                player.sendMessage("§e[알림] §a" + args[0] + " §f님이 귓속말을 차단했습니다!");
            }
        }
        if (cmd.getName().equalsIgnoreCase("차단") && player.hasPermission("utiltool.whisper")) {
            if (args.length > 0) {
                String ignorePlayer = player.getName() + "." + args[0];
                if (!player.getName().equals(args[0])) {
                    if (!IgnorePlayerManager.get().getStringList("Ignores").contains(ignorePlayer)) {
                        List<String> playerIgnoreList = IgnorePlayerManager.get().getStringList("Ignores");
                        playerIgnoreList.add(ignorePlayer);
                        IgnorePlayerManager.get().set("Ignores", playerIgnoreList);
                        player.sendMessage("§e[알림] §a" + args[0] + " §f님을 차단했습니다!");
                    } else {
                        player.sendMessage("§e[알림] §f이미 차단한 플레이어입니다!");
                    }
                } else {
                    player.sendMessage("§e[알림] §f자기 자신을 차단할 수 없습니다!");
                }
            } else {
                player.sendMessage("§6/차단 [상대] - 상대의 귓속말을 차단합니다.");
                player.sendMessage("§6/차단해제 [상대] - 상대를 차단한 것을 해제합니다.");
            }
        }
        if (cmd.getName().equalsIgnoreCase("차단해제") && player.hasPermission("utiltool.whisper")) {
            if (args.length > 0) {
                if (!player.getName().equals(args[0])) {
                    String targetIgnore = player.getName() + "." + args[0];
                    if (IgnorePlayerManager.get().getStringList("Ignores").contains(targetIgnore)) {
                        List<String> playerIgnoreList = IgnorePlayerManager.get().getStringList("Ignores");
                        playerIgnoreList.remove(targetIgnore);
                        IgnorePlayerManager.get().set("Ignores", playerIgnoreList);
                        player.sendMessage("§e[알림] §a" + args[0] + " §f님을 차단한 것을 해제했습니다!");
                    } else {
                        player.sendMessage("§e[알림] §f차단 당하지 않은 플레이어입니다!");
                    }
                } else {
                    player.sendMessage("§e[알림] §f자기 자신을 차단 해제할 수 없습니다!");
                }
            } else {
                player.sendMessage("§6/차단 [상대] - 상대의 귓속말을 차단합니다.");
                player.sendMessage("§6/차단해제 [상대] - 상대를 차단한 것을 해제합니다.");
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("확성기") && player.hasPermission("utiltool.broadcaster")) {
            String lore = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
            if (BroadCasterCooldown.checkCooldown(player)) {
                if (args.length > 0) {
                    Bukkit.broadcast(Component.text(""));
                    Bukkit.broadcast(Component.text("§6[§f " + player.getName() + " §6] §b" + lore));
                    Bukkit.broadcast(Component.text(""));
                    BroadCasterCooldown.setCooldown(player, 300);
                } else {
                    player.sendMessage("§c[UtilTool] 사용법: /확성기 <메세지>");
                }
            } else {
                player.sendMessage("§a[UtilTool] §f확성기 재사용까지 §a" + BroadCasterCooldown.getCooldown(player) + "§f초 남았습니다");
            }
            return false;
        }
        if (commandsList != null && commandsList.contains(cmd.getName())) {
            for (String commandMessage : CommandsManager.get().getStringList("Commands." + cmd.getName())) {
                if (commandMessage != null) {
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, commandMessage)));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', commandMessage));
                    }
                }
            }
            if (CommandsManager.get().getStringList("Commands." + cmd.getName()).isEmpty()) {
                getLogger().info("The message assigned to the Commands does not exist.");
            }
            return false;
        }
        return false;
    }

    public void registerCommands() {
        try {
            commandsList = new ArrayList<>(CommandsManager.get().getConfigurationSection("Commands").getKeys(false));
        } catch (NullPointerException e) {
            e.printStackTrace();
            getLogger().info("The command does not exist in commands.yml.");
        }
        try {
            if (commandsList != null) {
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                CommandMap commandMap = (CommandMap) field.get(getServer().getPluginManager());
                for (String commandList : commandsList) {
                    PluginCommand pluginCommand = constructor.newInstance(commandList, this);
                    commandMap.register(getDescription().getName(), pluginCommand);
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException |
                NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void getConfigMessages() {
        try {
            joinMessage = getConfig().getString("join_message");
            leaveMessage = getConfig().getString("leave_message");
            firstTimeJoinMessage = getConfig().getString("first_time_join_message");
            shiftRightClickCommand = getConfig().getString("shift_right_click_command");
        } catch (NullPointerException e) {
            e.printStackTrace();
            getLogger().info("config.yml에서 정보를 불러오는데 문제가 발생하였습니다.");
        }
    }

    public void getSpawnInfo() {
        try {
            world = Bukkit.getServer().getWorld(Objects.requireNonNull(getConfig().getString("spawnpoint.world")));
            x = getConfig().getDouble("spawnpoint.x");
            y = getConfig().getDouble("spawnpoint.y");
            z = getConfig().getDouble("spawnpoint.z");
            yaw = (float) getConfig().getDouble("spawnpoint.yaw");
            pitch = (float) getConfig().getDouble("spawnpoint.pitch");
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            getLogger().info("스폰 정보를 불러오는데 문제가 발생하였습니다.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PlayerUuidManager.get().getStringList("UUIDs").contains(player.getUniqueId().toString())) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                event.joinMessage(Component.text(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, joinMessage))));
            } else {
                event.joinMessage(Component.text(ChatColor.translateAlternateColorCodes('&', joinMessage)));
            }
        } else {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                event.joinMessage(Component.text(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, firstTimeJoinMessage))));
            } else {
                event.joinMessage(Component.text(ChatColor.translateAlternateColorCodes('&', firstTimeJoinMessage)));
            }
            List<String> playerUuidList = PlayerUuidManager.get().getStringList("UUIDs");
            playerUuidList.add(player.getUniqueId().toString());
            PlayerUuidManager.get().set("UUIDs", playerUuidList);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            event.quitMessage(Component.text(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, leaveMessage))));
        } else {
            event.quitMessage(Component.text(ChatColor.translateAlternateColorCodes('&', leaveMessage)));
        }
    }

    @EventHandler
    public void onFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (Objects.requireNonNull(player.getLocation().getWorld()).getName().equals(getConfig().getString("spawnpoint.world"))) {
            if (player.getLocation().getY() <= 0) {
                World world = Bukkit.getWorld(Objects.requireNonNull(getConfig().getString("spawnpoint.world")));
                double x = getConfig().getDouble("spawnpoint.x");
                double y = getConfig().getDouble("spawnpoint.y");
                double z = getConfig().getDouble("spawnpoint.z");
                float yaw = (float) getConfig().getDouble("spawnpoint.yaw");
                float pitch = (float) getConfig().getDouble("spawnpoint.pitch");
                player.teleport(new Location(world, x, y, z, yaw, pitch));
                player.setFallDistance(0);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();
        List<String> rightClickWorld = getConfig().getStringList("enable_world");
        if (event.getRightClicked().getType().equals(EntityType.PLAYER) && p.isSneaking()) {
            if (rightClickWorld.stream().anyMatch(current_world -> p.getWorld().equals(Bukkit.getWorld(current_world)))) {
                String clickPlayerName = (event.getRightClicked()).getName();
                String replacedShiftRightClick = (shiftRightClickCommand.replace("%player%", clickPlayerName));
                p.performCommand(replacedShiftRightClick);
            }
        }
    }
}