package net.bemacized.grimoire;

import net.bemacized.grimoire.eventhandlers.MainChatProcessor;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Grimoire {

    private final static Logger LOG = Logger.getLogger(Grimoire.class.getName());
    private static Grimoire instance;

    public static Grimoire getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new Grimoire(System.getenv("BOT_TOKEN"));
    }

    private JDA discord;

    private Grimoire(String token) {
        // Verify existence of token
        if (token == null) {
            LOG.severe("No discord bot token was set in the BOT_TOKEN environment variable! Quitting...");
            System.exit(1);
        }

        // Log in to Discord
        try {
            LOG.info("Logging in to Discord...");
            discord = new JDABuilder(AccountType.BOT)
                    .setAutoReconnect(true)
                    .setToken(token)
                    .buildBlocking();
            LOG.info("Discord login complete.");
        } catch (LoginException e) {
            LOG.log(Level.SEVERE, "Could not log in to Discord. Quitting...", e);
            System.exit(1);
        } catch (RateLimitedException e) {
            LOG.log(Level.SEVERE, "Walked into a rate limit while logging in. Please try again later. Quitting...", e);
            System.exit(1);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Login procedure was interrupted. Quitting...", e);
            System.exit(1);
        }

        // Register EventHandlers
        discord.addEventListener(new MainChatProcessor());
    }
}