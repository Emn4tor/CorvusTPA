package de.emn4tor;


import de.emn4tor.commands.tpa.*;
import de.emn4tor.utils.MessageManager;
import de.emn4tor.utils.RequestManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class CorvusTPA extends JavaPlugin {

    private RequestManager requestManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();


        // Create languages directory if it doesn't exist
        File langDir = new File(getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Initialize managers
        this.messageManager = new MessageManager(this);
        this.requestManager = new RequestManager(this);

        // Register commands
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPADenyCommand(this));
        getCommand("tphere").setExecutor(new TPAHereCommand(this));
        getCommand("tpcancel").setExecutor(new TPACancelCommand(this));

        getLogger().info("Thanks for using CorvusTPA!");
        getLogger().info("I ate a cookie once, it was good.");
        getLogger().info("Also I like turtles.");
        getLogger().info("CorvusTPA has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all pending requests
        if (requestManager != null) {
            requestManager.cancelAllRequests();
        }

        getLogger().info("CorvusTPA has been disabled!");
        getLogger().info("Bye bye!");
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}

