package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(name = "install", description = "Install PaperMC Server", mixinStandardHelpOptions = true)
public class InstallCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project you want to download") String project = "paper";
    @CommandLine.Parameters(index = "0", description = "Version you want to install", arity = "0..1") String version = "latest";
    @CommandLine.Option(names = {"-b", "--build"}, description = "Build of Version") String build = "latest";
    @CommandLine.Option(names = {"-sc", "--startcommand"}, description = "The Command used to start the server, replaces %memory and %nogui with there respective values") String startCommand = "java -Xms%memory -Xmx%memory -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar ./server.jar %nogui";
    @CommandLine.Option(names = {"-ns", "--nostart"}, description = "Don't start the server after installing") boolean nostart;
    @CommandLine.Option(names = {"-ng", "--nogui"}, description = "Start the server without a gui") boolean nogui;
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory = "2G";

    @Override
    public void run() {
        project = project.toLowerCase();
        Properties props = new Properties();
        try {
            if (version.equalsIgnoreCase("latest")) {
                String[] versions;
                switch (project) {
                    case "fabric":
                        JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/game"));
                        versions = new String[]{json.iterator().next().get("version").asText()};
                        break;
                    case "magma":
                        versions = new String[]{IOUtils.toString(new URL("https://api.magmafoundation.org/api/v2/latestVersion"))};
                        break;
                    case "purpur":
                        versions = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                        break;
                    default:
                        versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                }
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            if (build.equalsIgnoreCase("latest")) {
                switch (project) {
                    case "fabric":
                        JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/loader"));
                        build = json.iterator().next().get("version").asText();
                        json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/installer"));
                        build = build + ":" + json.iterator().next().get("version").asText();
                        break;
                    case "magma":
                        build = new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest")).get("name").asText();
                        break;
                    case "purpur":
                        build = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/latest")).get("build").asText();
                        break;
                    default:
                        String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                        build = builds[builds.length - 1];
                }
                props.setProperty("autoupdater", "true");
            } else {
                props.setProperty("autoupdater", "false");
            }
            System.out.println("Downloading " + project + " version " + version + " build #" + build + "...");
            switch (project) {
                case "fabric":
                    String[] builds = build.split(":");
                    FileUtils.copyURLToFile(new URL("https://meta.fabricmc.net/v2/versions/loader/" + version + "/" + builds[0] + "/" + builds[1] + "/server/jar"), new File(directory + "/server.jar"));
                    break;
                case "magma":
                    FileUtils.copyURLToFile(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest/" + build + "/download"), new File(directory + "/server.jar"));
                    break;
                case "purpur":
                    FileUtils.copyURLToFile(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/" + build + "/download"), new File(directory + "/server.jar"));
                    break;
                default:
                    FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + build + "/downloads/" + project + "-" + version + "-" + build + ".jar"), new File(directory + "/server.jar"));
            }
        } catch (MalformedURLException e) {
            System.out.println("Please send exception to developer on Discord: tamion\n");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to find " + project + " version " + version + " build " + build);
        }
        System.out.println("Downloaded Server");
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                FileUtils.writeStringToFile(new File(directory + "/start.bat"), "mcs start");
            } else {
                FileUtils.writeStringToFile(new File(directory + "/start.sh"), "mcs start");
            }
        } catch (IOException e) {
            System.out.println("Unable to create Start Script");
        }
        System.out.println("Created Start Script");
        props.setProperty("project", project);
        props.setProperty("version", version);
        props.setProperty("build", build);
        props.setProperty("nogui", String.valueOf(nogui));
        props.setProperty("memory", memory);
        props.setProperty("startcommand", startCommand);
        try {
            props.store(new FileWriter(directory + "/mcscli.properties"), "MinecraftServerCLI settings");
        } catch (IOException e) {
            System.out.println("Unable to create Properties File");
        }
        System.out.println("Created Properties File");
        try {
            if(project.equals("paper") || project.equals("purpur") || project.equals("magma") || project.equals("fabric") || project.equals("folia")) {
                FileUtils.writeStringToFile(new File(directory + "/eula.txt"), "eula=true");
                System.out.println("Accepted Eula");
            }
        } catch (IOException e) {
            System.out.println("Unable to accept Eula");
        }
        if(!nostart) {
            CommandLine.run(new StartCommand(), "-d", directory);
        }
    }
}
