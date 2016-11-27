package com.jgross.xbot;

import com.jgross.xbot.eventsystem.EventSystem;
import com.jgross.xbot.model.Bot;
import com.jgross.xbot.networking.Connection;

public class XBotLib {

    public static final EventSystem events = new EventSystem();

    /**
     * Run the bot specified in the parameter. This will have the bot connect and login into
     * hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. </br>
     * <b>This method will block until the bot has been disconnected from the server</b>
     * @param bot
     *           The bot to run.
     */
    public void run(Bot bot) {
        Connection con = new Connection(bot.connectionConfig());
        bot.run(con);
        try {
            con.waitForEnd();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the bot specified in the parameter in a separate thread. This method will have the bot connect and
     * login into hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. The thread running
     * this bot will be returned.
     * @param bot
     *           The bot to run
     * @return
     *        The tread object running this bot
     */
    public Thread runBotDysync(final Bot bot) {
        return new Thread() {
            @Override
            public void run() {
                XBotLib.this.run(bot);
            }
        };
    }

    /**
     * Run the bot specified in the parameter. This will have the bot connect and login into the
     * hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. </br>
     * <b>This method will block until the bot has been disconnected from the server</b>
     * @param bot
     *           The bot to run.
     */
    public static void runBot(Bot bot) {
        new XBotLib().run(bot);
    }

    /**
     * Run the bot specified in the parameter in a separate thread. This method will have the bot connect and
     * login into hipchat. This method will also invoke the {@link Bot#onLoad()} method for the bot. The thread running
     * this bot will be returned.
     * @param bot
     *           The bot to run
     * @return
     *        The tread object running this bot
     */
    public static Thread runBotDesync(Bot bot) {
        return new XBotLib().runBotDysync(bot);
    }

}
