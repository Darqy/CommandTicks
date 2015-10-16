package net.darqy.CommandTicks;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandTicks extends JavaPlugin {
   
    private static CommandTicks instance;
    
    private Scheduler scheduler = new Scheduler();
    private Map<String, String> taskIds = new HashMap();
    
    private YamlConfiguration config;
    
    private static final File configFile = new File("./plugins/CommandTicks/tasks.yml");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    @Override
    public void onEnable() {
        if (instance == null) instance = this;
        config = getExecutablesConfig(false);
        
        loadTasks();

        scheduler.start();
        getLogger().log(Level.INFO, "Scheduler started!");
    }
    
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (args.length < 1) {
            return false;
        }
        
        String action = args[0].toLowerCase();
        if ("suspend".equals(action)) {
            scheduler.stop();
            getLogger().info("Scheduler stopped!");
            s.sendMessage("§aTask scheduler suspended, no more tasks will execute");
        } else if ("continue".equals(action)) {
            scheduler.start();
            getLogger().info("Scheduler started!");
            s.sendMessage("§aTask scheduler resumed");
        } else if ("stop".equals(action)) {
            if (args.length < 2) {
                s.sendMessage("§eMissing task argument");
                return true;
            }
            
            String taskName = args[1].toLowerCase();
            if (!taskIds.containsKey(taskName)) {
                s.sendMessage("§eNo scheduled task by that name");
                return true;
            }
            
            String id = taskIds.get(taskName);
            scheduler.deschedule(id);
            taskIds.remove(taskName);
            
            s.sendMessage("§aTask §e" + taskName + " §apaused");
        } else if ("start".equals(action)) {
            if (args.length < 2) {
                s.sendMessage("§eMissing task argument");
                return true;
            }
            
            String taskName = args[1].toLowerCase();
            if (taskIds.containsKey(taskName)) {
                s.sendMessage("§eThat task is already scheduled");
                return true;
            }
            
            if (!config.contains(taskName)) {
                s.sendMessage("§eNo task by that name found in the config");
            } else {
                if (!loadTask(taskName, true)) {
                    s.sendMessage("§cCouldn't schedule task, check console for details");
                } else {
                    s.sendMessage("§aTask §e" + taskName + " §ascheduled");
                }
            }
        } else if ("reload".equals(action)) {
            config = getExecutablesConfig(true);
            scheduler.stop();
            scheduler = new Scheduler();
            
            loadTasks();
            
            scheduler.start();
            s.sendMessage("§aConfiguration and scheduler reloaded!");
        } else if ("status".equals(action)) {
            if (args.length < 2) {
                s.sendMessage("§eMissing task argument");
                return true;
            }
            
            String id = taskIds.get(args[1].toLowerCase());
            if (id == null) {
                s.sendMessage("§eNo scheduled task by that name");
                return true;
            }
            
            Predictor p = new Predictor(scheduler.getSchedulingPattern(id), System.currentTimeMillis());
            s.sendMessage("§aNext run at§e " + sdf.format(p.nextMatchingDate()));
        } else if ("list".equals(action)) {
            StringBuilder list = new StringBuilder();
            for (String task : taskIds.keySet()) {
                if (list.length() > 0) list.append("§a,§e ");
                list.append(task);
            }
            
            s.sendMessage("§aScheduled tasks:§e " + list.toString());
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    public void onDisable() {
        scheduler.stop();
    }
    
    public static CommandTicks getPlugin() {
        return instance;
    }
    
    private YamlConfiguration getExecutablesConfig(boolean reload) {
        if (config != null && !reload) {
            return config;
        }
        
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            config = YamlConfiguration.loadConfiguration(getResource("tasks.yml"));
            try {
                config.save(configFile);
            } catch (IOException e) {
                ;
            }
        }
        return config;
    }

    private void loadTasks() {
        for (String taskName : config.getKeys(false)) {
            loadTask(taskName, false);
        }
    }
    
    public boolean loadTask(String taskName, boolean override) {
        ConfigurationSection task = config.getConfigurationSection(taskName);

        if (!override && !task.getBoolean("enabled", true)) return false;

        List<String> commands;
        if (task.contains("commands")) {
            commands = task.getStringList("commands");
        } else {
            getLogger().warning("Command list missing in task \"" + taskName + "\", skipping.");
            return false;
        }
        
        if (!task.contains("schedule")) {
            getLogger().warning("Schedule pattern missing for task \"" + taskName + "\", skipping.");
            return false;
        }
        
        SchedulingPattern sp;
        try {
            sp = new SchedulingPattern(task.getString("schedule"));
        } catch (InvalidPatternException ex) {
            getLogger().log(Level.WARNING, ex.getMessage());
            getLogger().warning("Schedule pattern incorrect for task \"" + taskName + "\" skipping.");
            return false;
        }
        
        String id = (String) scheduler.schedule(sp, new SyncTask(commands));
        if (id == null) {
            return false;
        }
        
        taskIds.put(taskName, id);
        return true;
    }
    
}
