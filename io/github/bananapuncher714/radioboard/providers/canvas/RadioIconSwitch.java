package io.github.bananapuncher714.radioboard.providers.canvas;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.dependency.DependencyManager;

/**
 * A simple switch that runs different commands for each state it can be in
 * 
 * @author BananaPuncher714
 */
public class RadioIconSwitch implements RadioIcon {
	protected int[] switchOff;
	protected int[] switchOn;
	protected List< String > commandOns;
	protected List< String > commandOffs;
	protected int width;
	protected boolean on = false;
	
	protected RadioCanvas provider;
	
	public RadioIconSwitch( int[] switchOn, int[] switchOff, int width, List< String > commandOn, List< String > commandOff ) {
		this.switchOn = switchOn;
		this.switchOff = switchOff;
		this.commandOns = commandOn;
		this.commandOffs = commandOff;
		this.width = width;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return switchOn.length / width;
	}
	
	public boolean isOn() {
		return on;
	}
	
	@Override
	public int[] getDisplay() {
		return on ? switchOn : switchOff;
	}
	
	@Override
	public void init( RadioCanvas provider ) {
		this.provider = provider;
	}
	
	@Override
	public void onClick( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y ) {
		if ( action == DisplayInteract.LOOK || action == DisplayInteract.PROJECTILE  ) {
			return;
		}
		if ( action == DisplayInteract.PROJECTILE && entity instanceof Projectile ) {
			ProjectileSource source = ( ( Projectile ) entity ).getShooter();
			if ( source instanceof Player ) {
				entity = ( Entity ) source;
			} else {
				return;
			}
		}
		if ( !( entity instanceof Player ) ) {
			return;
		}
		Player player = ( Player ) entity;
		on = !on;
		List< String > commands = ( on ? commandOns : commandOffs );
		for ( String cmd : commands ) {
			String command = cmd.replace( "%player_name%", player.getName() );
			CommandSender sender = Bukkit.getConsoleSender();
			if ( command.startsWith( "/" ) ) {
				command = command.substring( 1 );
				sender = player;
			}
			Bukkit.dispatchCommand( sender, DependencyManager.parse( player, command ) );
		}
		
		if ( provider != null ) {
			provider.update( this );
		}
	}
	
	@Override
	public void terminate() {
		provider = null;
	}
}
