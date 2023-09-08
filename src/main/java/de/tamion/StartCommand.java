package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(name = "start", description = "Start Server", mixinStandardHelpOptions = true)
public class StartCommand implements Runnable {

    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @CommandLine.Option(names = {"-sc", "--startcommand"}, description = "The Command used to start the server, replaces %memory and %nogui with there respective values") String startCommand;
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory;
    @CommandLine.Option(names = {"-ng", "--nogui"}, description = "Start the server without a gui") Boolean nogui;
    @Override
    public void run() {
        if(new File(directory + "/mcscli.properties").exists()) {
            try {
                Properties props = new Properties();
                props.load(new FileReader(directory + "/mcscli.properties"));
                String project = props.getProperty("project");
                String version = props.getProperty("version");
                if(startCommand == null) {
                    startCommand = props.getProperty("startcommand");
                }
                if (memory == null) {
                    memory = props.getProperty("memory");
                }
                if (nogui == null) {
                    nogui = Boolean.parseBoolean(props.getProperty("nogui"));
                }
                if (props.getProperty("autoupdater").equals("true")) {
                    try {
                        String latestbuild = null;
                        switch (project) {
                            case "fabric":
                                JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/loader"));
                                latestbuild = json.iterator().next().get("version").asText();
                                json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/installer"));
                                latestbuild = latestbuild + ":" + json.iterator().next().get("version").asText();
                                break;
                            case "magma":
                                latestbuild = new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest")).get("name").asText();
                                break;
                            case "purpur":
                                latestbuild = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/latest")).get("build").asText();
                                break;
                            default:
                                String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                                latestbuild = builds[builds.length - 1];
                        }
                        if (!latestbuild.equals(props.getProperty("build"))) {
                            System.out.println("Downloading " + project + " version " + version + " build #" + latestbuild + "...");
                            switch (project) {
                                case "fabric":
                                    String[] builds = latestbuild.split(":");
                                    FileUtils.copyURLToFile(new URL("https://meta.fabricmc.net/v2/versions/loader/" + version + "/" + builds[0] + "/" + builds[1] + "/server/jar"), new File(directory + "/server.jar"));
                                    break;
                                case "magma":
                                    FileUtils.copyURLToFile(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest/" + latestbuild + "/download"), new File(directory + "/server.jar"));
                                    break;
                                case "purpur":
                                    FileUtils.copyURLToFile(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/" + latestbuild + "/download"), new File(directory + "/server.jar"));
                                    break;
                                default:
                                    FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + latestbuild + "/downloads/" + project + "-" + version + "-" + latestbuild + ".jar"), new File(directory + "/server.jar"));
                            }
                            System.out.println("Downloaded Server");
                            props.setProperty("build", latestbuild);
                            props.store(new FileWriter(directory + "/mcscli.properties"), "MinecraftServerCLI settings");
                            System.out.println("Updated properties file");
                        }
                    } catch (IOException e) {
                        System.out.println("Unable to run AutoUpdater");
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println("Please send exception to developer on Discord: tamion\n");
            } catch(NullPointerException e) {
                System.out.println("Unable to find value in Properties File. Proceeding with default values");
            } catch (IOException e) {
                System.out.println("Unable to read Properties file");
            }
        }
        if(startCommand == null) {
            startCommand = "java -Xms%memory -Xmx%memory -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar ./server.jar %nogui";
        }
        if(memory == null) {
            memory = "2G";
        }
        if(nogui == null) {
            nogui = false;
        }
        String noguis = "";
        if(nogui) {
            noguis = "-nogui";
        }
        startCommand = startCommand
                .replaceAll("%memory", memory)
                .replaceAll("%nogui", noguis);
        System.out.println("Starting server");
        try {
            new ProcessBuilder(startCommand.split(" "))
                    .directory(new File(directory))
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (InterruptedException e) {
            System.out.println("Server Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
