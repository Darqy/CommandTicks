package net.darqy.CommandTicks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandTask implements Runnable {
    
    private static final Map<String, InternalCommandExecutor> internalCommands = new HashMap();

    private List<String> commands;

    public CommandTask(List commands) {
        this.commands = commands;
    }

    @Override
    public void run() {
        for (int i = 0; i < commands.size(); i++) {
            final String command = commands.get(i);
            final String[] split = command.split(" ");
            final String first = split[0];
            
            if (first.startsWith("^")) {
                final String action = first.toLowerCase().substring(1);
                InternalCommandExecutor ice = internalCommands.get(action);
                if (ice != null) {
                    ice.execute(commands, i, split);
                    
                    if (ice.shouldBreak()) {
                        break;
                    }
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    private interface InternalCommandExecutor {
        public void execute(List<String> commands, int index, String[] split);
        public boolean shouldBreak();
    }

    private static class PlayerCommandExecutor implements InternalCommandExecutor {
        @Override
        public void execute(List<String> commands, int index, String[] split) {
            String command = commands.get(index).substring(8);

            for (Player p : Bukkit.getOnlinePlayers()) {
                Bukkit.dispatchCommand(p, command);
            }
        }
        @Override
        public boolean shouldBreak() {
            return false;
        }
    }

    private static class PauseCommandExecutor implements InternalCommandExecutor {
        @Override
        public void execute(List<String> commands, int index, String[] split) {
            long delay;
            try {
                delay = Long.parseLong(split[1]);
            } catch (NumberFormatException e) {
                return;
            }
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(CommandTicks.getPlugin(),
                        new CommandTask(commands.subList(index + 1, commands.size())), delay * 20);
        }
        @Override
        public boolean shouldBreak() {
            return true;
        }
    }
    
    private static class WeightedCommandExecutor implements InternalCommandExecutor {
        private static final Random random = new Random();
        @Override
        public void execute(List<String> commands, int index, String[] split) {
            int chance;
            try {
                chance = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                return;
            }
            
            if (random.nextInt(100) <= chance) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Util.joinStrings(split, 2, ' '));
            }
        }
        @Override
        public boolean shouldBreak() {
            return false;
        }
    }
    
    private static class SayCommandExecutor implements InternalCommandExecutor {
        @Override
        public void execute(List<String> commands, int index, String[] split) {
            String message = ChatColor.translateAlternateColorCodes('&', Util.joinStrings(split, 1, ' '));
            Bukkit.broadcastMessage(message);
        }
        @Override
        public boolean shouldBreak() {
            return false;
        }
    }
        
    static {
        internalCommands.put("player", new PlayerCommandExecutor());
        internalCommands.put("pause", new PauseCommandExecutor());
        internalCommands.put("weighted", new WeightedCommandExecutor());
        internalCommands.put("say", new SayCommandExecutor());
    }
    
}
