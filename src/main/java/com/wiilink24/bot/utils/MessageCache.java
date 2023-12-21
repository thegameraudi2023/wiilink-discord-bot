package com.wiilink24.bot.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 *
 * Thank you Jagrosh for the message cache code
 * @author John Grosh (john.a.grosh@gmail.com)
 */

public class MessageCache
{
    private final static int SIZE = 1000;
    private final HashMap<Long,FixedCache<Long,CachedMessage>> cache = new HashMap<>();

    public CachedMessage putMessage(Message message)
    {
        if(!cache.containsKey(message.getGuild().getIdLong()))
            cache.put(message.getGuild().getIdLong(), new FixedCache<>(SIZE));
        return cache.get(message.getGuild().getIdLong()).put(message.getIdLong(), new CachedMessage(message));
    }

    public CachedMessage pullMessage(Guild guild, long messageId)
    {
        if(!cache.containsKey(guild.getIdLong()))
            return null;
        return cache.get(guild.getIdLong()).pull(messageId);
    }

    public List<CachedMessage> getMessages(Guild guild, Predicate<CachedMessage> predicate)
    {
        if(!cache.containsKey(guild.getIdLong()))
            return Collections.EMPTY_LIST;
        return cache.get(guild.getIdLong()).getValues().stream().filter(predicate).collect(Collectors.toList());
    }

    public class CachedMessage implements ISnowflake
    {
        private final String content, username, discriminator;
        private final long id, author, channel, guild;
        private final List<Attachment> attachments;

        private CachedMessage(Message message)
        {
            content = message.getContentRaw();
            id = message.getIdLong();
            author = message.getAuthor().getIdLong();
            username = message.getAuthor().getName();
            discriminator = message.getAuthor().getDiscriminator();
            channel = message.getChannel().getIdLong();
            guild = message.isFromGuild() ? message.getGuild().getIdLong() : 0L;
            attachments = message.getAttachments();
        }

        public String getContentRaw()
        {
            return content;
        }

        public List<Attachment> getAttachments()
        {
            return attachments;
        }

        public String getUsername()
        {
            return username;
        }

        public String getDiscriminator()
        {
            return discriminator;
        }

        public long getAuthorId()
        {
            return author;
        }

        public TextChannel getTextChannel(ShardManager shardManager)
        {
            if (guild == 0L)
                return null;
            Guild g = shardManager.getGuildById(guild);
            if (g == null)
                return null;
            return g.getTextChannelById(channel);
        }

        public Guild getGuild(ShardManager shardManager)
        {
            if (guild == 0L)
                return null;
            return shardManager.getGuildById(guild);
        }

        @Override
        public long getIdLong()
        {
            return id;
        }
    }
}
