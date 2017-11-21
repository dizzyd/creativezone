package com.dizzyd.creativezone;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

public class Commands extends CommandTreeBase {

    public Commands() {
        addSubcommand(new RadiusCmd());
        addSubcommand(new WhitelistCmd());
    }

    @Override
    public String getName() {
        return "creativezone";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "creativezone.usage";
    }

    public static class RadiusCmd extends CommandBase
    {
        @Override
        public String getName() {
            return "radius";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "creativezone.radius.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length > 0) {
                // User wishes to set the radius; parse the argument as a number
                int newRadius = 0;
                try {
                    newRadius = Integer.valueOf(args[0]);
                } catch (NumberFormatException e) {
                    notifyCommandListener(sender, this, "creativezone.radius.set.invalidnumber");
                    return;
                }

                if (newRadius < 25 || newRadius > 1000) {
                    notifyCommandListener(sender, this, "creativezone.radius.set.invalidbounds");
                    return;
                }

                // Ok, we have a properly bounded radius; update the radius and save new config
                CreativeZoneMod.zoneRadius = newRadius;
                notifyCommandListener(sender, this, "creativezone.radius.set", CreativeZoneMod.zoneRadius);
            } else {
                // Display the current radius
                notifyCommandListener(sender, this, "creativezone.radius", CreativeZoneMod.zoneRadius);
            }
        }
    }

    public static class WhitelistCmd extends CommandBase {

        private EntityPlayer findPlayer(MinecraftServer server, String name) {
            return server.getPlayerList().getPlayerByUsername(name);
        }

        @Override
        public String getName() {
            return "whitelist";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "creativezone.whitelist.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 1) {
                // No arguments provided; display all whitelisted users
                StringBuilder b = new StringBuilder();
                for (String k: CreativeZoneMod.whitelist) {
                    b.append("\n* ").append(k);
                }
                notifyCommandListener(sender, this, "creativezone.whitelist", b.toString());
                return;
            }

            String subCommand = args[0];
            switch (args[0]) {
                case "add":
                    if ((args.length < 2 || server.getPlayerList().getPlayerByUsername(args[1]) == null)) {
                        notifyCommandListener(sender, this, "creativezone.whitelist.nouser");
                    } else {
                        CreativeZoneMod.whitelist.add(args[1]);
                        notifyCommandListener(sender, this, "creativezone.whitelist.added", args[1]);
                    }
                    break;
                case "rm":
                    if ((args.length < 2 || server.getPlayerList().getPlayerByUsername(args[1]) == null)) {
                        notifyCommandListener(sender, this, "creativezone.whitelist.nouser");
                    } else {
                        CreativeZoneMod.whitelist.remove(args[1]);
                        notifyCommandListener(sender, this, "creativezone.whitelist.removed", args[1]);
                    }
                    break;
                case "clear":
                    CreativeZoneMod.whitelist.clear();
                    notifyCommandListener(sender, this, "creativezone.whitelist.cleared");
                    break;
                default:
                    notifyCommandListener(sender, this, "creativezone.whitelist.unknowncmd");
            }
        }
    }

}
