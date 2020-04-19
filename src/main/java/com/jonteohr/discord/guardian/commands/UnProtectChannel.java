package com.jonteohr.discord.guardian.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jonteohr.discord.guardian.App;
import com.jonteohr.discord.guardian.permission.PermissionCheck;
import com.jonteohr.discord.guardian.sql.Channels;
import com.jonteohr.discord.guardian.sql.Query;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UnProtectChannel extends ListenerAdapter {
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		PermissionCheck permissionCheck = new PermissionCheck();
		Query sql = new Query();
		Channels channels = new Channels();
		
		if(!args[0].equalsIgnoreCase(App.prefix + "unprotect")) // Don't listen unless this command is used
			return;
		if(!permissionCheck.isAdmin(e.getMember()) || !permissionCheck.isModerator(e.getMember())) // Author is not allowed to use this
			return;
		if(args.length < 2 || !args[1].contains("#")) { // Not enough arguments or channel not tagged
			e.getChannel().sendMessage(":x: **Incorrect usage!**\nCorrect usage: `" + App.prefix + "unprotect <#channel>`").queue();
			return;
		}
		
		TextChannel target = e.getMessage().getMentionedChannels().get(0);
		
		if(!channels.isChannelProtected(target)) { // Tagged channel is already protected
			e.getChannel().sendMessage(":x: **Channel is not protected!**").queue();
			return;
		}
		
		List<String> roleList = new ArrayList<String>();
		ResultSet res = sql.queryGet("SELECT role FROM channels WHERE channel='" + target.getId() + "' AND guild_id='" + e.getGuild().getId() + "';");
		try {
			while(res.next()) {
				roleList.add(res.getString(1));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		Role accessRole = e.getGuild().getRoleById(roleList.get(0));
		
		if(!channels.unProtectChannel(target, accessRole)) {
			e.getChannel().sendMessage(":x: **Something went wrong.**").queue();
			return;
		}

		e.getChannel().sendMessage(":white_check_mark: Channel " + target.getAsMention() + " is no longer password protected!").queue();
		return;
	}
}
