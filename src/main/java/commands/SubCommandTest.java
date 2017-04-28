package commands;

import sx.blah.discord.handle.obj.IMessage;

public class SubCommandTest {
    @DiscordCommand("subcommands")
    public String[] subcommands(IMessage msg, String...arg) {
        return arg;
    }

    @DiscordSubCommand(parent = "subcommands", name = "sub1")
    public String[] subcommandsSub1(IMessage msg, String...args) {
        return args;
    }

        @DiscordSubCommand(parent = "subcommandsSub1", name = "sub1")
        public String[] subcommandsSub1Sub1(IMessage msg, String...args) {
            return args;
        }

            @DiscordSubCommand(parent = "subcommandsSub1Sub1", name = "sub1")
            public void subcommandsSub1Sub1Sub1(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub1 sub1 sub1`");
            }

            @DiscordSubCommand(parent = "subcommandsSub1Sub1", name = "sub2")
            public void subcommandsSub1Sub1Sub2(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub1 sub1 sub2`");
            }

            @DiscordSubCommand(parent = "subcommandsSub1Sub1", name = "sub3")
            public void subcommandsSub1Sub1Sub3(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub1 sub1 sub3`");
            }

        @DiscordSubCommand(parent = "subcommandsSub1", name = "sub3")
        public void subcommandsSub1Sub3(IMessage msg, String...args) {
            msg.getChannel().sendMessage("you executed `subcommands sub1 sub3`");
        }

    @DiscordSubCommand(parent = "subcommands", name = "sub2")
    public String[] subcommandsSub2(IMessage msg, String...args) {
        return args;
    }
    //-------------------------------------------------------
        @DiscordSubCommand(parent = "subcommandsSub2", name = "sub1")
        public String[] subcommandsSub2Sub1(IMessage msg, String...args) {
            return args;
        }

            @DiscordSubCommand(parent = "subcommandsSub2Sub1", name = "sub1")
            public void subcommandsSub2Sub1Sub1(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub2 sub1 sub1`");
            }

            @DiscordSubCommand(parent = "subcommandsSub2Sub1", name = "sub2")
            public void subcommandsSub2Sub1Sub2(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub2 sub1 sub2`");
            }

            @DiscordSubCommand(parent = "subcommandsSub2Sub1", name = "sub3")
            public void subcommandsSub2Sub1Sub3(IMessage msg, String...args) {
                msg.getChannel().sendMessage("you executed `subcommands sub2 sub1 sub3`");
        }

        @DiscordSubCommand(parent = "subcommandsSub2", name = "sub3")
        public void subcommandsSub2Sub3(IMessage msg, String...args) {
            msg.getChannel().sendMessage("you executed `subcommands sub2 sub3`");
        }


    //-------------------------------------------------------
    @DiscordSubCommand(parent = "subcommands", name = "sub3")
    public void subcommandsSub3(IMessage msg, String...args) {
        msg.getChannel().sendMessage("you executed `subcommands sub3`");
    }


}
