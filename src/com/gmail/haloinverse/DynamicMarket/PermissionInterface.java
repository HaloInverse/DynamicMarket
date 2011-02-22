package com.gmail.haloinverse.DynamicMarket;

import org.bukkit.command.CommandSender;

public interface PermissionInterface {
	public boolean permission(CommandSender thisSender, String permissionString);
}
