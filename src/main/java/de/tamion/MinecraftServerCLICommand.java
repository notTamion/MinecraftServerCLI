package de.tamion;

import picocli.CommandLine;

@CommandLine.Command(name = "mcs", description = "MinecraftServerCLI", mixinStandardHelpOptions = true, subcommands = {InstallCommand.class, StartCommand.class, ProjectsCommand.class, VersionsCommand.class, BuildsCommand.class})
public class MinecraftServerCLICommand implements Runnable {
    public static void main(String[] args) {
        CommandLine.run(new MinecraftServerCLICommand(), args);
    }

    @Override
    public void run() {
        System.out.println("Command Line Interface for Minecraft Servers. Use --help for more information");
    }
}