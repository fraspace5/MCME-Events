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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author donoa_000
 * 
 * The great one file butler
 */
public class Thompson{
    
    private static HashMap<String, UserProfile> profiles = new HashMap<>();
    
    private static final Random rng = new Random();
    
    public static void welcome(Player p){
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
    
    public static void farwell(Player p){
        if(profiles.containsKey(p.getName())){
            DBmanager.saveObj(profiles.get(p.getName()), new File(Main.getPluginDirectory() + Main.getFileSep() + "UserProfiles"), p.getUniqueId().toString());
            profiles.remove(p.getName());
        }
    }
    
    public static void sendMessage(Player p, String msg){
        p.sendMessage(ChatColor.DARK_BLUE + "Thompson" + ChatColor.RESET + ": " + msg);
    }
    
    public static String formatMessage(String msg){
        return ChatColor.DARK_BLUE + "Thompson" + ChatColor.RESET + ": " + msg;
    }
    
    private static class UserProfile{
        
        @Getter @Setter
        private String title;
        
        @Getter @Setter
        private HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();
        
        public UserProfile(){}
        
        public UserProfile(String title){
            this.title = title;
        }

    }
    
    public static class Listeners implements Listener, ConversationAbandonedListener{
        
        private final ConversationFactory convoFactory;
        
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
                }else{
                    Conversation c = convoFactory.withFirstPrompt(new nextCommand()).buildConversation((Conversable) p);
                    c.getContext().setSessionData("profile", profiles.get(e.getPlayer().getName()));
                    c.begin();
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
        }
        
        private static interface SureCode{
            public void sure(ConversationContext context);
            
            public Prompt getLast();
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
                        
                    }else if(cmd.contains("edit")){
                        String alias = StringUtils.join(cmd.subList(cmd.indexOf("edit"), cmd.size()-1), " ");
                        context.setSessionData("alias", alias);
                        context.setSessionData("type", "EditAlias");
                        return new editAlias();
                    }else if(cmd.contains("new") || cmd.contains("set")){
                        
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
                return new Sure();
            }

            @Override
            public void sure(ConversationContext context) {
                
            }

            @Override
            public Prompt getLast() {
                return null;
            }
        }
        
        private class newAlias extends StringPrompt {

            @Override
            public String getPromptText(ConversationContext context) {
                return formatMessage("What would you like the alias " + context.getSessionData("alias").toString() + " to do " + 
                                    ((UserProfile)context.getSessionData("profile")).getTitle() + "?");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                echo(context.getForWhom(), input);
                return null;
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
}
