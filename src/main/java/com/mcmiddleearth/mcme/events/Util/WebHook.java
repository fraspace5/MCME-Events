/*
 * This file is part of MCME-Events.
 * 
 * MCME-Events is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MCME-Events is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MCME-Events.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.mcmiddleearth.mcme.events.Util;

import com.mcmiddleearth.mcme.events.Main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class WebHook {
    
    ChatColor COLOR_ERROR = ChatColor.RED;
    
    ChatColor COLOR_OK = ChatColor.GREEN;
    
    private final AtomicBoolean lock = new AtomicBoolean(false);
    
    private final String updateURL = "";
    
    private void update(CommandSender sender)
  {
	final BukkitScheduler bs = Main.getPlugin().getServer().getScheduler();
	final String pn = sender instanceof Player ? ((Player)sender).getName() : null;
	bs.scheduleAsyncDelayedTask(Main.getPlugin(), new Runnable()
	{
	  public void run()
	  {
		try
		{
		  while(!lock.compareAndSet(false, true))
		  {
			try
			{
			  Thread.sleep(1L);
			}
			catch(InterruptedException e)
			{
			}
		  }
		  String out;
		  try
		  {
			File to = new File(Main.getPlugin().getServer().getUpdateFolderFile(), updateURL.substring(updateURL.lastIndexOf('/')+1, updateURL.length()));
			File tmp = new File(to.getPath()+".au");
			if(!tmp.exists())
			{
			  Main.getPlugin().getServer().getUpdateFolderFile().mkdirs();
			  tmp.createNewFile();
			}
			URL url = new URL(updateURL);
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(tmp);
			byte[] buffer = new byte[4096];
			int fetched;
			while((fetched = is.read(buffer)) != -1)
			  os.write(buffer, 0, fetched);
			is.close();
			os.flush();
			os.close();
			if(to.exists())
			  to.delete();
			tmp.renameTo(to);
			out = COLOR_OK+Main.getPlugin().getName()+" ready! Restart server to finish the update.";
		  }
		  catch(Exception e)
		  {
			out = COLOR_ERROR+Main.getPlugin().getName()+" failed to update!";
			printStackTraceSync(e, true);
		  }
		  bs.scheduleSyncDelayedTask(Main.getPlugin(), new WebHook.SyncMessageDelayer(pn, new String[] {out}));
		  lock.set(false);
		}
		catch(Throwable t)
		{
		  printStackTraceSync(t, false);
		}
	  }
	});
  }
    
    private void printStackTraceSync(Throwable t, boolean expected)
  {
	BukkitScheduler bs = Main.getPlugin().getServer().getScheduler();
	try
	{
	  String prefix = "[AutoUpdate] ";
	  StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  t.printStackTrace(pw);
	  String[] sts = sw.toString().replace("\r", "").split("\n");
	  String[] out;
	  if(expected)
		out = new String[sts.length+25];
	  else
		out = new String[sts.length+27];
	  out[0] = prefix;
	  out[1] = prefix+"Internal error!";
	  out[2] = prefix+"If this bug hasn't been reported please open a ticket at http://forums.bukkit.org/threads/autoupdate-update-your-plugins.84421/";
	  out[3] = prefix+"Include the following into your bug report:";
	  out[4] = prefix+"          ======= SNIP HERE =======";
	  int i = 5;
	  if(!expected)
	  {
		out[++i] = prefix+"DISABLING UPDATER!";
		out[++i] = prefix;
	  }
	  bs.scheduleSyncDelayedTask(Main.getPlugin(), new WebHook.SyncMessageDelayer(null, out));
	}
	catch(Throwable e) //This prevents endless loops.
	{
	  e.printStackTrace();
	}
//	if(!expected)
//	{
//	  bs.cancelTask(pid);
//	  bs.scheduleAsyncDelayedTask(Main.getPlugin(), new Runnable()
//	  {
//		public void run()
//		{
//		  while(!lock.compareAndSet(false, true))
//		  {
//			try
//			{
//			  Thread.sleep(1L);
//			}
//			  catch(InterruptedException e)
//			{
//			}
//		  }
//		  pid = -1;
//		  config = null;
//		  needUpdate = updatePending = enabled = false;
//		  updateURL = updateVersion = pluginURL = type = null;
//		  lock.set(false);
//		}
//	  });
//	}
  }
    
    private class SyncMessageDelayer implements Runnable
  {
	private final String p;
	private final String[] msgs;
	
	private SyncMessageDelayer(String p, String[] msgs)
	{
	  this.p = p;
	  this.msgs = msgs;
	}
	
	public void run()
	{
	  try
	  {
		if(p != null)
		{
		  Player p = Main.getPlugin().getServer().getPlayerExact(this.p);
		  if(p != null)
			for(String msg: msgs)
			  if(msg != null)
				p.sendMessage(msg);
		}
		else
		{
		  Logger log = Main.getPlugin().getLogger();
		  for(String msg: msgs)
			if(msg != null)
			  log.info(msg);
		}
	  }
	  catch(Throwable t)
	  {
		printStackTraceSync(t, false);
	  }
	}
  }
}