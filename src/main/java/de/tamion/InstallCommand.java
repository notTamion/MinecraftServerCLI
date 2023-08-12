package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "install", description = "Install PaperMC Server", mixinStandardHelpOptions = true)
public class InstallCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project you want to download: paper, velocity, waterfall, purpur") String project = "paper";
    @CommandLine.Parameters(index = "0", description = "Version you want to install", arity = "0..1") String version = "latest";
    @CommandLine.Option(names = {"-b", "--build"}, description = "Build of Version") String build = "latest";
    @CommandLine.Option(names = {"-n", "--no-start"}, description = "Don't start the server after installing") Boolean nostart;
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory = "2G";


    @Override
    public void run() {
        project = project.toLowerCase();
        try {
            Properties props = new Properties();
            if(version.equalsIgnoreCase("latest")) {
                String[] versions;
                switch (project) {
                    case "purpur": versions = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); break;
                    default: versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                }
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            if(build.equalsIgnoreCase("latest")) {
                switch (project) {
                    case "purpur": build = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/latest")).get("build").asText(); break;
                    default: String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); build = builds[builds.length - 1]; break;
                }
                props.setProperty("AutoUpdater", "true");
            } else {
                props.setProperty("AutoUpdater", "false");
            }
            System.out.println("Downloading " + project + " version " + version + " build #" + build + "...");
            switch (project) {
                case "purpur": FileUtils.copyURLToFile(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/" + build + "/download"), new File(directory + "/server.jar")); break;
                default: FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + build + "/downloads/" + project + "-" + version + "-" + build + ".jar"), new File(directory + "/server.jar"));
            }
            System.out.println("Downloaded Server");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                FileUtils.writeStringToFile(new File(directory + "/start.bat"), "java -jar " + MinecraftServerCLICommand.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1) + " start");
            } else {
                FileUtils.writeStringToFile(new File(directory + "/start.sh"), "java -jar " + MinecraftServerCLICommand.class.getProtectionDomain().getCodeSource().getLocation().getFile() + " start");
            }
            System.out.println("Created Start Script");
            props.setProperty("project", project);
            props.setProperty("version", version);
            props.setProperty("build", build);
            props.setProperty("memory", memory);
            props.store(new FileWriter(directory + "/mcscli.properties"), "MinecraftServerCLI settings");
            System.out.println("Created Properties File");
            if(project.equals("paper") || project.equals("purpur")) {
                FileUtils.writeStringToFile(new File(directory + "/eula.txt"), "eula=true");
                System.out.println("Accepted Eula");
            }
            if(nostart) {
                return;
            }
            String nogui = "";
            if(project.equals("paper") || project.equals("purpur")) {
                nogui = "--nogui";
            }
            System.out.println("Starting server");
            new ProcessBuilder("java", "-Xms" + memory, "-Xmx" + memory, "-jar", "./server.jar", nogui)
                    .directory(new File(directory))
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch(FileNotFoundException e) {
            System.out.println("No downloadable server software found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
