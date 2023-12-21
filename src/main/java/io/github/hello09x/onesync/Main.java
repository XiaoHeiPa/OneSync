package io.github.hello09x.onesync;

import com.google.gson.Gson;
import io.github.hello09x.bedrock.menu.ChestMenuRegistry;
import io.github.hello09x.onesync.api.handler.SnapshotHandler;
import io.github.hello09x.onesync.command.CommandRegistry;
import io.github.hello09x.onesync.handler.*;
import io.github.hello09x.onesync.listener.SnapshotListener;
import io.github.hello09x.onesync.listener.SynchronizeListener;
import io.github.hello09x.onesync.manager.LockingManager;
import io.github.hello09x.onesync.manager.SynchronizeManager;
import io.github.hello09x.onesync.repository.constant.SnapshotCause;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter
    private static ChestMenuRegistry menuRegistry;

    @Getter
    private final static Gson gson = new Gson();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        menuRegistry = new ChestMenuRegistry(this);
        CommandRegistry.register();

        {
            var sm = Bukkit.getServicesManager();
            sm.register(SnapshotHandler.class, EnderChestSnapshotHandler.instance, this, ServicePriority.Highest);
            sm.register(SnapshotHandler.class, InventorySnapshotHandler.instance, this, ServicePriority.Highest);
            sm.register(SnapshotHandler.class, ProfileSnapshotHandler.instance, this, ServicePriority.High);
            sm.register(SnapshotHandler.class, AdvancementSnapshotHandler.instance, this, ServicePriority.Normal);
            sm.register(SnapshotHandler.class, PDCSnapshotHandler.instance, this, ServicePriority.Normal);
            sm.register(SnapshotHandler.class, PotionEffectSnapshotHandler.instance, this, ServicePriority.Normal);
        }

        {
            var pm = super.getServer().getPluginManager();
            pm.registerEvents(SynchronizeListener.instance, this);
            pm.registerEvents(SnapshotListener.instance, this);
        }

        {
            var messenger = getServer().getMessenger();
            messenger.registerIncomingPluginChannel(this, LockingManager.CHANNEL, LockingManager.instance);
            messenger.registerOutgoingPluginChannel(this, LockingManager.CHANNEL);
        }

        LockingManager.instance.relockAll();   // 热重载
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        super.onDisable();
        SynchronizeManager.instance.saveAll(SnapshotCause.PLUGIN_DISABLE);  // 关闭服务器不会调用 PlayerQuitEvent 事件, 因此需要全量保存一次

        {
            var messenger = getServer().getMessenger();
            messenger.unregisterIncomingPluginChannel(this);
            messenger.unregisterOutgoingPluginChannel(this);
        }

        {
            Bukkit.getServicesManager().unregisterAll(this);
        }

    }
}
