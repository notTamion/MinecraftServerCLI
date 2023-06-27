package de.tamion;

import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

@CommandLine.Command(name = "paper", description = "Paper Command", mixinStandardHelpOptions = true, subcommands = {InstallCommand.class, StartCommand.class, ProjectsCommand.class, VersionsCommand.class, BuildsCommand.class})
public class PaperCLICommand implements Runnable {
    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        CommandLine.run(new PaperCLICommand(), args);
        AnsiConsole.systemUninstall();
    }

    @Override
    public void run() {
        System.out.println("Command Line Interface for PaperMC. Use --help for more information");
    }
}