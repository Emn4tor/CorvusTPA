package de.emn4tor.utils;

/*
 *  @author: Emn4tor
 *  @created: 08.03.2025
 */
import de.emn4tor.CorvusTPA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class MessageManager {
    private final CorvusTPA plugin;
    private final MiniMessage miniMessage;
    private YamlConfiguration langConfig;
    private String currentLanguage;

    public MessageManager(CorvusTPA plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadLanguage();
    }

    /**
     * Loads the language file based on config setting
     */
    public void loadLanguage() {
        // Create languages directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Get language from config
        currentLanguage = plugin.getConfig().getString("language", "en-US");

        // Save default language files if they don't exist
        saveDefaultLanguageFile("en-US.yml");

        // Load the language file
        File langFile = new File(langDir, currentLanguage + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + currentLanguage + ".yml not found! Falling back to en-US.yml");
            currentLanguage = "en-US";
            langFile = new File(langDir, "en-US.yml");

            // If even en-US.yml doesn't exist, create it
            if (!langFile.exists()) {
                saveDefaultLanguageFile("en-US.yml");
            }
        }

        // Load the language configuration
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        plugin.getLogger().info("Loaded language: " + currentLanguage);
    }

    /**
     * Saves a default language file from resources
     */
    private void saveDefaultLanguageFile(String fileName) {
        File langFile = new File(plugin.getDataFolder() + "/languages", fileName);
        if (!langFile.exists()) {
            try (InputStream in = plugin.getResource("languages/" + fileName)) {
                if (in != null) {
                    Files.copy(in, langFile.toPath());
                    plugin.getLogger().info("Created default language file: " + fileName);
                } else {
                    // Create a basic language file if it doesn't exist in resources
                    YamlConfiguration defaultLang = new YamlConfiguration();
                    setDefaultMessages(defaultLang);
                    defaultLang.save(langFile);
                    plugin.getLogger().info("Created new language file: " + fileName);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create language file: " + fileName);
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets default messages in a YamlConfiguration
     */
    private void setDefaultMessages(YamlConfiguration config) {
        config.set("tpa-request-sent", "<gradient:blue:aqua>Teleport request sent to <player></gradient>");
        config.set("tpa-request-received", "<gradient:gold:yellow><player> wants to teleport to you</gradient>\n<green>[Accept]</green> <red>[Deny]</red>");
        config.set("tphere-request-sent", "<gradient:blue:aqua>Teleport here request sent to <player></gradient>");
        config.set("tphere-request-received", "<gradient:gold:yellow><player> wants you to teleport to them</gradient>\n<green>[Accept]</green> <red>[Deny]</red>");
        config.set("request-accepted-sender", "<gradient:green:lime><player> accepted your teleport request</gradient>");
        config.set("request-accepted-target", "<gradient:green:lime>You accepted <player>'s teleport request</gradient>");
        config.set("request-denied-sender", "<gradient:red:yellow><player> denied your teleport request</gradient>");
        config.set("request-denied-target", "<gradient:red:yellow>You denied <player>'s teleport request</gradient>");
        config.set("request-cancelled", "<gradient:gray:white>You cancelled your teleport request</gradient>");
        config.set("request-cancelled-target", "<gradient:gray:white><player> cancelled their teleport request</gradient>");
        config.set("request-expired", "<gradient:gray:white>Your teleport request has expired</gradient>");
        config.set("request-expired-target", "<gradient:gray:white>Teleport request from <player> has expired</gradient>");
        config.set("no-request-found", "<gradient:red:yellow>You don't have any pending teleport requests</gradient>");
        config.set("player-not-found", "<gradient:red:yellow>Player <player> not found or offline</gradient>");
        config.set("self-teleport", "<gradient:red:yellow>You cannot teleport to yourself</gradient>");
        config.set("already-has-request", "<gradient:red:yellow>You already have a pending teleport request</gradient>");
    }

    /**
     * Gets a message from the language file with a fallback
     */
    private String getMessage(String key, String defaultValue) {
        String message = langConfig.getString(key);
        if (message == null) {
            plugin.getLogger().warning("Missing message key in language file: " + key + ", using default");
            return defaultValue;
        }
        return message;
    }

    public void sendTpaRequest(Player sender, Player target) {
        // Message to sender
        String senderMsg = getMessage("tpa-request-sent",
                "<gradient:blue:aqua>Teleport request sent to <player></gradient>");
        Component senderComponent = miniMessage.deserialize(senderMsg,
                Placeholder.parsed("player", target.getName()));
        sender.sendMessage(senderComponent);

        // Message to target with clickable buttons
        String targetMsg = getMessage("tpa-request-received",
                "<gradient:gold:yellow><player> wants to teleport to you</gradient>\n" +
                        "<green>[Accept]</green> <red>[Deny]</red>");

        Component acceptButton = miniMessage.deserialize("<green>[Accept]</green>")
                .clickEvent(ClickEvent.runCommand("/tpaccept"));

        Component denyButton = miniMessage.deserialize("<red>[Deny]</red>")
                .clickEvent(ClickEvent.runCommand("/tpdeny"));

        // Replace placeholder buttons with actual clickable buttons
        String[] parts = targetMsg.split("\\[Accept\\].*\\[Deny\\]");
        if (parts.length >= 1) {
            Component firstPart = miniMessage.deserialize(parts[0],
                    Placeholder.parsed("player", sender.getName()));

            Component message = Component.empty()
                    .append(firstPart)
                    .append(acceptButton)
                    .append(Component.text(" "))
                    .append(denyButton);

            if (parts.length > 1) {
                Component lastPart = miniMessage.deserialize(parts[1]);
                message = message.append(lastPart);
            }

            target.sendMessage(message);
        } else {
            Component targetComponent = miniMessage.deserialize(targetMsg,
                    Placeholder.parsed("player", sender.getName()));
            target.sendMessage(targetComponent);
        }
    }

    public void sendTpHereRequest(Player sender, Player target) {
        // Message to sender
        String senderMsg = getMessage("tphere-request-sent",
                "<gradient:blue:aqua>Teleport here request sent to <player></gradient>");
        Component senderComponent = miniMessage.deserialize(senderMsg,
                Placeholder.parsed("player", target.getName()));
        sender.sendMessage(senderComponent);

        // Message to target with clickable buttons
        String targetMsg = getMessage("tphere-request-received",
                "<gradient:gold:yellow><player> wants you to teleport to them</gradient>\n" +
                        "<green>[Accept]</green> <red>[Deny]</red>");

        Component acceptButton = miniMessage.deserialize("<green>[Accept]</green>")
                .clickEvent(ClickEvent.runCommand("/tpaccept"));

        Component denyButton = miniMessage.deserialize("<red>[Deny]</red>")
                .clickEvent(ClickEvent.runCommand("/tpdeny"));

        // Replace placeholder buttons with actual clickable buttons
        String[] parts = targetMsg.split("\\[Accept\\].*\\[Deny\\]");
        if (parts.length >= 1) {
            Component firstPart = miniMessage.deserialize(parts[0],
                    Placeholder.parsed("player", sender.getName()));

            Component message = Component.empty()
                    .append(firstPart)
                    .append(acceptButton)
                    .append(Component.text(" "))
                    .append(denyButton);

            if (parts.length > 1) {
                Component lastPart = miniMessage.deserialize(parts[1]);
                message = message.append(lastPart);
            }

            target.sendMessage(message);
        } else {
            Component targetComponent = miniMessage.deserialize(targetMsg,
                    Placeholder.parsed("player", sender.getName()));
            target.sendMessage(targetComponent);
        }
    }

    public void sendRequestAccepted(Player sender, Player target) {
        String senderMsg = getMessage("request-accepted-sender",
                "<gradient:green:lime><player> accepted your teleport request</gradient>");
        Component senderComponent = miniMessage.deserialize(senderMsg,
                Placeholder.parsed("player", target.getName()));
        sender.sendMessage(senderComponent);

        String targetMsg = getMessage("request-accepted-target",
                "<gradient:green:lime>You accepted <player>'s teleport request</gradient>");
        Component targetComponent = miniMessage.deserialize(targetMsg,
                Placeholder.parsed("player", sender.getName()));
        target.sendMessage(targetComponent);
    }

    public void sendRequestDenied(Player sender, Player target) {
        String senderMsg = getMessage("request-denied-sender",
                "<gradient:red:yellow><player> denied your teleport request</gradient>");
        Component senderComponent = miniMessage.deserialize(senderMsg,
                Placeholder.parsed("player", target.getName()));
        sender.sendMessage(senderComponent);

        String targetMsg = getMessage("request-denied-target",
                "<gradient:red:yellow>You denied <player>'s teleport request</gradient>");
        Component targetComponent = miniMessage.deserialize(targetMsg,
                Placeholder.parsed("player", sender.getName()));
        target.sendMessage(targetComponent);
    }

    public void sendRequestCancelled(Player sender) {
        String msg = getMessage("request-cancelled",
                "<gradient:gray:white>You cancelled your teleport request</gradient>");
        Component component = miniMessage.deserialize(msg);
        sender.sendMessage(component);
    }

    public void sendRequestCancelledTarget(Player target, Player sender) {
        String msg = getMessage("request-cancelled-target",
                "<gradient:gray:white><player> cancelled their teleport request</gradient>");
        Component component = miniMessage.deserialize(msg,
                Placeholder.parsed("player", sender.getName()));
        target.sendMessage(component);
    }

    public void sendRequestExpired(Player sender) {
        String msg = getMessage("request-expired",
                "<gradient:gray:white>Your teleport request has expired</gradient>");
        Component component = miniMessage.deserialize(msg);
        sender.sendMessage(component);
    }

    public void sendRequestExpiredTarget(Player target, Player sender) {
        String msg = getMessage("request-expired-target",
                "<gradient:gray:white>Teleport request from <player> has expired</gradient>");
        Component component = miniMessage.deserialize(msg,
                Placeholder.parsed("player", sender != null ? sender.getName() : "unknown"));
        target.sendMessage(component);
    }

    public void sendNoRequestFound(Player player) {
        String msg = getMessage("no-request-found",
                "<gradient:red:yellow>You don't have any pending teleport requests</gradient>");
        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);
    }

    public void sendPlayerNotFound(Player player, String targetName) {
        String msg = getMessage("player-not-found",
                "<gradient:red:yellow>Player <player> not found or offline</gradient>");
        Component component = miniMessage.deserialize(msg,
                Placeholder.parsed("player", targetName));
        player.sendMessage(component);
    }

    public void sendSelfTeleport(Player player) {
        String msg = getMessage("self-teleport",
                "<gradient:red:yellow>You cannot teleport to yourself</gradient>");
        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);
    }

    public void sendAlreadyHasRequest(Player player) {
        String msg = getMessage("already-has-request",
                "<gradient:red:yellow>You already have a pending teleport request</gradient>");
        Component component = miniMessage.deserialize(msg);
        player.sendMessage(component);
    }
}

