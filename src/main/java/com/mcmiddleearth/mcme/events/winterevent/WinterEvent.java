package co.mcme.lizzehface.winterevent;

import co.mcme.lizzehface.winterevent.commands.ClearCommand;
import co.mcme.lizzehface.winterevent.commands.GetCommand;
import co.mcme.lizzehface.winterevent.commands.MountCommand;
import co.mcme.lizzehface.winterevent.commands.SnowCommand;
import co.mcme.lizzehface.winterevent.listeners.SnowballListener;
import co.mcme.lizzehface.winterevent.stats.PlayerStatsContainer;
import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;

public class WinterEvent extends JavaPlugin {

    @Getter
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private static WinterEvent pluginInstance;
    @Getter
    private static Server serverInstance;
    @Getter
    private static File pluginDirectory;
    @Getter
    private static File playerDirectory;

    @Override
    public void onEnable() {
        this.pluginInstance = this;
        this.serverInstance = getServer();
        this.pluginDirectory = getDataFolder();
        if (!pluginDirectory.exists()) {
            pluginDirectory.mkdir();
        }
        this.playerDirectory = new File(pluginDirectory + System.getProperty("file.separator") + "players");
        if (!playerDirectory.exists()) {
            playerDirectory.mkdir();
        }
        serverInstance.getPluginManager().registerEvents(new SnowballListener(), this);
        getCommand("snow").setExecutor(new SnowCommand());
        getCommand("winterclear").setExecutor(new ClearCommand());
        getCommand("getsnow").setExecutor(new GetCommand());
        getCommand("mount").setExecutor(new MountCommand());
    }

    @Override
    public synchronized void onDisable() {
        PlayerStatsContainer.saveAll();
    }
}
