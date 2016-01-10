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
import com.mcmiddleearth.mcme.events.PVP.Handlers.ChatHandler;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

/**
 *
 * @author donoa_000
 * 
 * The great one file butler
 */
public class Thompson implements Player{
    
    @Getter
    private static Thompson Inst;
    
    private ConversationFactory convoFactory;
    
    private HashMap<String, UserProfile> profiles = new HashMap<>();
    
    private final Random rng = new Random();
    
    @Getter
    private boolean Conversing = false;
    
    public void welcome(Player p){
        Object profile = false;
        try{
            profile = DBmanager.loadObj(UserProfile.class, "UserProfiles" + Main.getFileSep() + p.getUniqueId());
        }catch(Exception e){}
        if(!profile.equals(false)){
            UserProfile up = (UserProfile) profile;
            sendMessage(p, "Welcome " + up.getTitle() + " " + p.getName().toLowerCase());
            profiles.put(p.getName(), (UserProfile) profile);
        }
    }
    
    public void farwell(Player p){
        if(profiles.containsKey(p.getName())){
            DBmanager.saveObj(profiles.get(p.getName()), new File(Main.getPluginDirectory() + Main.getFileSep() + "UserProfiles"), p.getUniqueId().toString());
            profiles.remove(p.getName());
        }
    }
    
    public void sendMessage(Player p, String msg){
        p.sendMessage(ChatColor.DARK_BLUE + "Thompson" + ChatColor.RESET + ": " + msg);
    }
    
    public String formatMessage(String msg){
        return ChatColor.DARK_BLUE + "Thompson" + ChatColor.RESET + ": " + msg;
    }
    
    public void executeCommand(String cmd){
        switch(cmd){
            default:
                Bukkit.dispatchCommand(null, cmd);
        }
    }
    
    public Thompson(Plugin p){
        PluginManager pm = p.getServer().getPluginManager();
        pm.registerEvents(new Listeners(), p);
        Inst = this;
    }
    
    private class UserProfile{
        
        @Getter @Setter
        private String title;
        
        @Getter @Setter
        private HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();
        
        public UserProfile(){}
        
        public UserProfile(String title){
            this.title = title;
        }

    }
    
    public class Listeners implements Listener, ConversationAbandonedListener{
        
        private final String[] goodbye = new String[] {
            "I'll be here if you need me",
            "You seem a bit busy at the moment",
            "If you need me just ask",
            "I will await our next meeting"
        };
        
        public Listeners(){
            convoFactory = new ConversationFactory(Main.getPlugin())
                .withModality(true)
                .withEscapeSequence("that is all")
                .withFirstPrompt(null)
                .withTimeout(600)
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage(formatMessage("I'm afraid I can't work with non players, I'm quite sorry."))
                .addConversationAbandonedListener(this);
        }
        
        @EventHandler
        public void onChat(AsyncPlayerChatEvent e){
            if(e.getMessage().toLowerCase().contains("thompson")){
                Player p = e.getPlayer();
                String msg = e.getMessage();
                if(!profiles.containsKey(e.getPlayer().getName())){
                    convoFactory.withFirstPrompt(new newUser()).buildConversation((Conversable) p).begin();
                    Conversing = true;
                }else{
                    Conversation c = convoFactory.withFirstPrompt(new nextCommand()).buildConversation((Conversable) p);
                    c.getContext().setSessionData("profile", profiles.get(e.getPlayer().getName()));
                    c.begin();
                    Conversing = true;
                }
                e.getRecipients().clear();
                e.getRecipients().add(p);
            }
        }
        
        private void echo(Conversable p, String input){
            p.sendRawMessage(ChatHandler.formatChat((Player) p).replace("%2$s", input));
        }
        
        @Override
        public void conversationAbandoned(ConversationAbandonedEvent e) {
            if(!e.gracefulExit()){
                e.getContext().getForWhom().sendRawMessage(formatMessage(goodbye[rng.nextInt(goodbye.length)]));
            }
            Conversing = false;
        }
        
        
        
        private class newUser extends StringPrompt {

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("I dont belive we've met") + "\n" + formatMessage("What title would you like me to use?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                UserProfile up = new UserProfile(input);
                profiles.put(((Player) context.getForWhom()).getName(), up);
                context.setSessionData("profile", up);
                return new anythingElse();
            }
        }
        
        private class anythingElse extends StringPrompt {

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("Will that be all?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                if(input.toLowerCase().contains("no")){
                    return new nextCommand();
                }else if(input.toLowerCase().contains("yes")){
                    context.getForWhom().sendRawMessage(formatMessage(goodbye[rng.nextInt(goodbye.length)]));
                    return Prompt.END_OF_CONVERSATION;
                }else{
                    context.getForWhom().sendRawMessage(formatMessage("I'm afraid I don't understand,"));
                    return new anythingElse();
                }
            }
        }
        
        private class nextCommand extends StringPrompt {

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("How can I help you " + ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                UserProfile up = (UserProfile)context.getSessionData("profile");
                for(String s : new String[] {",", "."}){
                    input = input.replace(s, "");
                }
                ArrayList<String> cmd = new ArrayList<String>(Arrays.asList(input.toLowerCase().split(" ")));
                for(String s : new String[] {"the", "an", "a"}){
                    cmd.remove(s);
                }
                if(cmd.contains("alias")){
                    cmd.remove("alias");
                    if(cmd.contains("delete") || cmd.contains("remove")){
                        int start = cmd.indexOf("delete");
                        if(start == -1){
                            start = cmd.indexOf("remove");
                        }
                        String alias = StringUtils.join(cmd.subList(start, cmd.size()-1), " ");
                        context.setSessionData("alias", alias);
                        return new deleteAlias();
                    }else if(cmd.contains("edit")){
                        String alias = StringUtils.join(cmd.subList(cmd.indexOf("edit"), cmd.size()-1), " ");
                        context.setSessionData("alias", alias);
                        return new editAlias();
                    }else if(cmd.contains("new") || cmd.contains("set")){
                        int start = cmd.indexOf("new");
                        if(start == -1){
                            start = cmd.indexOf("set");
                        }
                        String alias = StringUtils.join(cmd.subList(start, cmd.size()-1), " ");
                        context.setSessionData("alias", alias);
                        return new newAlias();
                    }
                }
                for(String k : up.getAliases().keySet()){
                    if(input.toLowerCase().contains(k)){
                        
                    }
                }
                context.getForWhom().sendRawMessage(formatMessage("I'm afraid I don't understand,"));
                return new nextCommand();
            }
        }
        
        private class editAlias extends StringPrompt implements SureCode{

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("What would you like the alias " + context.getSessionData("alias").toString() + " changed to " + 
                                    ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                context.setSessionData("SureOfWhat", "make the alias " + context.getSessionData("alias").toString() + " excute " + input);
                context.setSessionData("exec", input);
                return new Sure();
            }

            @Override
            public void sure(ConversationContext context) {
                UserProfile up = (UserProfile)context.getSessionData("profile");
                up.getAliases().put(context.getSessionData("alias").toString(), new ArrayList<>(Arrays.asList(context.getSessionData("exec").toString().split("; "))));
            }

            @Override
            public Prompt getLast() {
                return new nextCommand();
            }
        }
        
        private class newAlias extends StringPrompt implements SureCode{

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("What would you like the alias " + context.getSessionData("alias").toString() + " to do " + 
                                    ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                context.setSessionData("SureOfWhat", "make the new alias " + context.getSessionData("alias").toString() + " excute " + input);
                context.setSessionData("exec", input);
                return new Sure();
            }
            
            @Override
            public void sure(ConversationContext context) {
                UserProfile up = (UserProfile)context.getSessionData("profile");
                up.getAliases().put(context.getSessionData("alias").toString(), new ArrayList<>(Arrays.asList(context.getSessionData("exec").toString().split("; "))));
            }

            @Override
            public Prompt getLast() {
                return new nextCommand();
            }
        }
        
        private class deleteAlias extends StringPrompt implements SureCode{

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("What would you like the alias " + context.getSessionData("alias").toString() + " to do " + 
                                    ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                context.setSessionData("SureOfWhat", "make the new alias " + context.getSessionData("alias").toString() + " excute " + input);
                context.setSessionData("exec", input);
                return new Sure();
            }
            
            @Override
            public void sure(ConversationContext context) {
                UserProfile up = (UserProfile)context.getSessionData("profile");
                up.getAliases().put(context.getSessionData("alias").toString(), new ArrayList<>(Arrays.asList(context.getSessionData("exec").toString().split("; "))));
            }

            @Override
            public Prompt getLast() {
                return new nextCommand();
            }
        }
        
        private class Sure extends StringPrompt {

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("Are you sure you would like to " + context.getSessionData("SureOfWhat").toString() + " " + 
                                    ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                if(input.toLowerCase().contains("no")){
                    return ((SureCode) context.getSessionData("SureCode")).getLast();
                }else if(input.toLowerCase().contains("yes")){
                    SureCode sc = (SureCode) context.getSessionData("SureCode");
                    sc.sure(context);
                    return new anythingElse();
                }else{
                    context.getForWhom().sendRawMessage(formatMessage("I'm afraid I don't understand,"));
                    return new Sure();
                }
            }
        }
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_BLUE + "Thompson";
    }

    @Override
    public void setDisplayName(String string) {}

    @Override
    public String getPlayerListName() {
        return ChatColor.DARK_BLUE + "Thompson";
    }

    @Override
    public void setPlayerListName(String string) {}

    @Override
    public void setCompassTarget(Location lctn) {}

    @Override
    public Location getCompassTarget() {
        return this.getLocation();
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public void sendRawMessage(String string) {
        
    }

    @Override
    public void kickPlayer(String string) {}

    @Override
    public void chat(String string) {
        
    }

    @Override
    public boolean performCommand(String string) {
        return Bukkit.dispatchCommand(this, string);
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public void setSneaking(boolean bln) {}

    @Override
    public boolean isSprinting() {
        return false;
    }

    @Override
    public void setSprinting(boolean bln) {}

    @Override
    public void saveData() {
        for(Entry e : profiles.entrySet()){
            DBmanager.saveObj(e.getValue(), new File(Main.getPluginDirectory() + Main.getFileSep() + "UserProfiles"), 
                    Bukkit.getPlayer(e.getKey().toString()).getUniqueId().toString());
        }
    }

    @Override
    public void loadData() {}

    @Override
    public void setSleepingIgnored(boolean bln) {}

    @Override
    public boolean isSleepingIgnored() {
        return true;
    }

    @Override
    public void playNote(Location lctn, byte b, byte b1) {}

    @Override
    public void playNote(Location lctn, Instrument i, Note note) {}

    @Override
    public void playSound(Location lctn, Sound sound, float f, float f1) {}

    @Override
    public void playSound(Location lctn, String string, float f, float f1) {}

    @Override
    public void playEffect(Location lctn, Effect effect, int i) {}

    @Override
    public <T> void playEffect(Location lctn, Effect effect, T t) {}

    @Override
    public void sendBlockChange(Location lctn, Material mtrl, byte b) {}

    @Override
    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes) {return true;}

    @Override
    public void sendBlockChange(Location lctn, int i, byte b) {}

    @Override
    public void sendSignChange(Location lctn, String[] strings) throws IllegalArgumentException {}

    @Override
    public void sendMap(MapView mv) {}

    @Override
    public void updateInventory() {}

    @Override
    public void awardAchievement(Achievement a) {}

    @Override
    public void removeAchievement(Achievement a) {
        Bukkit.broadcastMessage("Thompson just earned " + a.name() + " WOW!");
    }

    @Override
    public boolean hasAchievement(Achievement a) {
        return true;
    }

    @Override
    public void incrementStatistic(Statistic ststc) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc) throws IllegalArgumentException {}

    @Override
    public void incrementStatistic(Statistic ststc, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc, int i) throws IllegalArgumentException {}

    @Override
    public void setStatistic(Statistic ststc, int i) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic ststc) throws IllegalArgumentException {
        return 9001;
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc, Material mtrl) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic ststc, Material mtrl) throws IllegalArgumentException {
        return 9001;
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc, Material mtrl, int i) throws IllegalArgumentException {}

    @Override
    public void setStatistic(Statistic ststc, Material mtrl, int i) throws IllegalArgumentException {}

    @Override
    public void incrementStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {}

    @Override
    public int getStatistic(Statistic ststc, EntityType et) throws IllegalArgumentException {
        return 9001;
    }

    @Override
    public void incrementStatistic(Statistic ststc, EntityType et, int i) throws IllegalArgumentException {}

    @Override
    public void decrementStatistic(Statistic ststc, EntityType et, int i) {}

    @Override
    public void setStatistic(Statistic ststc, EntityType et, int i) {}

    @Override
    public void setPlayerTime(long l, boolean bln) {}

    @Override
    public long getPlayerTime() {
        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {}

    @Override
    public void setPlayerWeather(WeatherType wt) {}

    @Override
    public WeatherType getPlayerWeather() {
        return WeatherType.CLEAR;
    }

    @Override
    public void resetPlayerWeather() {}

    @Override
    public void giveExp(int i) {}

    @Override
    public void giveExpLevels(int i) {}

    @Override
    public float getExp() {
        return 9001;
    }

    @Override
    public void setExp(float f) {}

    @Override
    public int getLevel() {
        return 9001;
    }

    @Override
    public void setLevel(int i) {}

    @Override
    public int getTotalExperience() {
        return 9001;
    }

    @Override
    public void setTotalExperience(int i) {}

    @Override
    public float getExhaustion() {
        return 0;
    }

    @Override
    public void setExhaustion(float f) {}

    @Override
    public float getSaturation() {
        return 0;
    }

    @Override
    public void setSaturation(float f) {}

    @Override
    public int getFoodLevel() {
        return 20;
    }

    @Override
    public void setFoodLevel(int i) {}

    @Override
    public Location getBedSpawnLocation() {
        return this.getLocation();
    }

    @Override
    public void setBedSpawnLocation(Location lctn) {}

    @Override
    public void setBedSpawnLocation(Location lctn, boolean bln) {}

    @Override
    public boolean getAllowFlight() {
        return true;
    }

    @Override
    public void setAllowFlight(boolean bln) {

    }

    @Override
    public void hidePlayer(Player player) {}

    @Override
    public void showPlayer(Player player) {}

    @Override
    public boolean canSee(Player player) {
        return true;
    }

    @Override
    public boolean isOnGround() {
        return true;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean bln) {}

    @Override
    public void setFlySpeed(float f) throws IllegalArgumentException {}

    @Override
    public void setWalkSpeed(float f) throws IllegalArgumentException {}

    @Override
    public float getFlySpeed() {
        return 1;
    }

    @Override
    public float getWalkSpeed() {
        return 1;
    }

    @Override
    public void setTexturePack(String string) {}

    @Override
    public void setResourcePack(String string) {}

    @Override
    public Scoreboard getScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @Override
    public void setScoreboard(Scoreboard scrbrd) throws IllegalArgumentException, IllegalStateException {}

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean bln) {}

    @Override
    public void setHealthScale(double d) throws IllegalArgumentException {}

    @Override
    public double getHealthScale() {
        return 1;
    }

    @Override
    public Spigot spigot() {
        return null; //TODO what is this for
    }

    @Override
    public String getName() {
        return "Thompson";
    }

    @Override
    public PlayerInventory getInventory() {
        return (PlayerInventory) Bukkit.createInventory(this, InventoryType.PLAYER);
    }

    @Override
    public Inventory getEnderChest() {
        return Bukkit.createInventory(this, InventoryType.PLAYER);
    }

    @Override
    public boolean setWindowProperty(InventoryView.Property prprt, int i) {
        return false;
    }

    @Override
    public InventoryView getOpenInventory() {
        return null;
    }

    @Override
    public InventoryView openInventory(Inventory invntr) {
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location lctn, boolean bln) {
        return null;
    }

    @Override
    public InventoryView openEnchanting(Location lctn, boolean bln) {
        return null;
    }

    @Override
    public void openInventory(InventoryView iv) {}

    @Override
    public void closeInventory() {}

    @Override
    public ItemStack getItemInHand() {
        return new ItemStack(Material.BLAZE_ROD);
    }

    @Override
    public void setItemInHand(ItemStack is) {}

    @Override
    public ItemStack getItemOnCursor() {
        return new ItemStack(Material.BLAZE_ROD);
    }

    @Override
    public void setItemOnCursor(ItemStack is) {}

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.SPECTATOR;
    }

    @Override
    public void setGameMode(GameMode gm) {}

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public int getExpToLevel() {
        return 1;
    }

    @Override
    public double getEyeHeight() {
        return 1.75;
    }

    @Override
    public double getEyeHeight(boolean bln) {
        return 1.75;
    }

    @Override
    public Location getEyeLocation() {
        return this.getLocation().add(0, 1.75, 0);
    }

    @Override
    public List<Block> getLineOfSight(HashSet<Byte> hs, int i) {
        return new ArrayList<Block>();
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> set, int i) {
        return new ArrayList<Block>();
    }

    @Override
    public Block getTargetBlock(HashSet<Byte> hs, int i) {
        return this.getLocation().getBlock();
    }

    @Override
    public Block getTargetBlock(Set<Material> set, int i) {
        return this.getLocation().getBlock();
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i) {
        return Arrays.asList(new Block[] {this.getLocation().getBlock(), this.getLocation().getBlock()});
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
        return Arrays.asList(new Block[] {this.getLocation().getBlock(), this.getLocation().getBlock()});
    }

    @Override
    public Egg throwEgg() {
        return ((Player) Bukkit.getOnlinePlayers().toArray()[rng.nextInt(Bukkit.getOnlinePlayers().size()-1)]).throwEgg();
    }

    @Override
    public Snowball throwSnowball() {
        return ((Player) Bukkit.getOnlinePlayers().toArray()[rng.nextInt(Bukkit.getOnlinePlayers().size()-1)]).throwSnowball();
    }

    @Override
    public Arrow shootArrow() {
        return ((Player) Bukkit.getOnlinePlayers().toArray()[rng.nextInt(Bukkit.getOnlinePlayers().size()-1)]).shootArrow();
    }

    @Override
    public int getRemainingAir() {
        return 20;
    }

    @Override
    public void setRemainingAir(int i) {}

    @Override
    public int getMaximumAir() {
        return 20;
    }

    @Override
    public void setMaximumAir(int i) {}

    @Override
    public int getMaximumNoDamageTicks() {
        return 20;
    }

    @Override
    public void setMaximumNoDamageTicks(int i) {}

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public int _INVALID_getLastDamage() {
        return 0;
    }

    @Override
    public void setLastDamage(double d) {}

    @Override
    public void _INVALID_setLastDamage(int i) {}

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public void setNoDamageTicks(int i) {}

    @Override
    public Player getKiller() {
        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe) {
        return false;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe, boolean bln) {
        return false;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> clctn) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType pet) {
        return false;
    }

    @Override
    public void removePotionEffect(PotionEffectType pet) {}

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return new ArrayList<PotionEffect>();
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        return true;
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean bln) {}

    @Override
    public EntityEquipment getEquipment() {
        return null;
    }

    @Override
    public void setCanPickupItems(boolean bln) {}

    @Override
    public boolean getCanPickupItems() {
        return false;
    }

    @Override
    public boolean isLeashed() {
        return true;
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        throw new IllegalStateException();
    }

    @Override
    public boolean setLeashHolder(Entity entity) {
        return false;
    }

    @Override
    public Location getLocation() {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    @Override
    public Location getLocation(Location lctn) {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    @Override
    public void setVelocity(Vector vector) {}

    @Override
    public Vector getVelocity() {
        return new Vector();
    }

    @Override
    public World getWorld() {
        return this.getLocation().getWorld();
    }

    @Override
    public boolean teleport(Location lctn) {
        return false;
    }

    @Override
    public boolean teleport(Location lctn, PlayerTeleportEvent.TeleportCause tc) {
        return false;
    }

    @Override
    public boolean teleport(Entity entity) {
        return false;
    }

    @Override
    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause tc) {
        return false;
    }

    @Override
    public List<Entity> getNearbyEntities(double d, double d1, double d2) {
        return new ArrayList<Entity>();
    }

    @Override
    public int getEntityId() {
        return -1;
    }

    @Override
    public int getFireTicks() {
        return 0;
    }

    @Override
    public int getMaxFireTicks() {
        return 0;
    }

    @Override
    public void setFireTicks(int i) {}

    @Override
    public void remove() {}

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public Entity getPassenger() {
        return null;
    }

    @Override
    public boolean setPassenger(Entity entity) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return 0;
    }

    @Override
    public void setFallDistance(float f) {}

    @Override
    public void setLastDamageCause(EntityDamageEvent ede) {}

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    }

    @Override
    public int getTicksLived() {
        return 9001;
    }

    @Override
    public void setTicksLived(int i) {}

    @Override
    public void playEffect(EntityEffect ee) {}

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public boolean isInsideVehicle() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Override
    public Entity getVehicle() {
        return null;
    }

    @Override
    public void setCustomName(String string) {}

    @Override
    public String getCustomName() {
        return "Thompson";
    }

    @Override
    public void setCustomNameVisible(boolean bln) {}

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public void setMetadata(String string, MetadataValue mv) {}

    @Override
    public List<MetadataValue> getMetadata(String string) {
        return new ArrayList<MetadataValue>();
    }

    @Override
    public boolean hasMetadata(String string) {
        return true;
    }

    @Override
    public void removeMetadata(String string, Plugin plugin) {}

    @Override
    public void sendMessage(String string) {}

    @Override
    public void sendMessage(String[] strings) {}

    @Override
    public boolean isPermissionSet(String string) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission prmsn) {
        return true;
    }

    @Override
    public boolean hasPermission(String string) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission prmsn) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public void removeAttachment(PermissionAttachment pa) {}

    @Override
    public void recalculatePermissions() {}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Bukkit.getConsoleSender().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean bln) {}

    @Override
    public void damage(double d) {}

    @Override
    public void _INVALID_damage(int i) {}

    @Override
    public void damage(double d, Entity entity) {}

    @Override
    public void _INVALID_damage(int i, Entity entity) {}

    @Override
    public double getHealth() {
        return 20;
    }

    @Override
    public int _INVALID_getHealth() {
        return 20;
    }

    @Override
    public void setHealth(double d) {}

    @Override
    public void _INVALID_setHealth(int i) {}

    @Override
    public double getMaxHealth() {
        return 20;
    }

    @Override
    public int _INVALID_getMaxHealth() {
        return 20;
    }

    @Override
    public void setMaxHealth(double d) {}

    @Override
    public void _INVALID_setMaxHealth(int i) {}

    @Override
    public void resetMaxHealth() {}

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type) {
        return ((Player) Bukkit.getOnlinePlayers().toArray()[rng.nextInt(Bukkit.getOnlinePlayers().size()-1)]).launchProjectile(type);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type, Vector vector) {
        return ((Player) Bukkit.getOnlinePlayers().toArray()[rng.nextInt(Bukkit.getOnlinePlayers().size()-1)]).launchProjectile(type, vector);
    }

//    @Override
//    public boolean isConversing() {
//        
//    }

    @Override
    public void acceptConversationInput(String string) {}

    @Override
    public boolean beginConversation(Conversation c) {
        return true;
    }

    @Override
    public void abandonConversation(Conversation c) {}

    @Override
    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae) {}

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public void setBanned(boolean bln) {}

    @Override
    public boolean isWhitelisted() {
        return true;
    }

    @Override
    public void setWhitelisted(boolean bln) {}

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes) {}

    @Override
    public Set<String> getListeningPluginChannels() {
        HashSet<String> hs = new HashSet<>();
        for(Plugin p : Bukkit.getPluginManager().getPlugins()){
            hs.add(p.getName());
        }
        return hs;
    }
    
    private interface SureCode{
        public void sure(ConversationContext context);

        public Prompt getLast();
    }
}
