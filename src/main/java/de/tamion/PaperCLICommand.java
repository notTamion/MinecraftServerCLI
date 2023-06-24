package de.tamion;

import picocli.CommandLine;

@CommandLine.Command(name = "paper", description = "Paper Command", mixinStandardHelpOptions = true, subcommands = {InstallCommand.class, StartCommand.class})
public class PaperCLICommand implements Runnable {
    public static void main(String[] args) {
        CommandLine.run(new PaperCLICommand(), args);
    }

    @Override
    public void run() {
        System.out.println("Command Line Interface for PaperMC. Use --help for more information");
    }
}