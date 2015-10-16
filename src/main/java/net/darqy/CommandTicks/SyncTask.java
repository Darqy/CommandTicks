package net.darqy.CommandTicks;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.util.List;
import org.bukkit.Bukkit;

public class SyncTask extends Task {
    
    private CommandTicks plugin = CommandTicks.getPlugin();
    private List<String> commands;
    
    public SyncTask(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandTask(commands));
    }
    
}
