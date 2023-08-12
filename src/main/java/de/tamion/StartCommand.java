package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.*;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(name = "start", description = "Start Server", mixinStandardHelpOptions = true)
public class StartCommand implements Runnable {

    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory = "default";
    @Override
    public void run() {
        try {
            String nogui = "";
            if(new File(directory + "/mcscli.properties").exists()) {
                Properties props = new Properties();
                props.load(new FileReader(directory + "/mcscli.properties"));
                String project = props.getProperty("project");
                String version = props.getProperty("version");
                String currentbuild = props.getProperty("build");
                if(memory.equals("default")) {
                    memory = props.getProperty("memory");
                }
                if(project.equals("paper") || project.equals("purpur")) {
                    nogui = "--nogui";
                }
                if (!props.getProperty("AutoUpdater").equals("true")) {
                    String latestbuild;
                    switch (project) {
                        case "purpur": latestbuild = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/latest")).get("build").asText(); break;
                        default: String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); latestbuild = builds[builds.length - 1]; break;
                    }
                    if (!latestbuild.equals(currentbuild)) {
                        System.out.println("Downloading " + project + " version " + version + " build #" + latestbuild + "...");
                        switch (project) {
                            case "purpur": FileUtils.copyURLToFile(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/" + latestbuild + "/download"), new File(directory + "/server.jar")); break;
                            default: FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + latestbuild + "/downloads/" + project + "-" + version + "-" + latestbuild + ".jar"), new File(directory + "/server.jar")); break;
                        }
                        System.out.println("Downloaded Server");
                        props.setProperty("build", latestbuild);
                        props.store(new FileWriter(directory + "/mcscli.properties"), "MinecraftServerCLI settings");
                        System.out.println("Updated properties file");
                    }
                }
            }
            if(memory.equals("default")) {
                memory = "2G";
            }
            System.out.println("Starting server");
            new ProcessBuilder("java", "-Xms" + memory, "-Xmx" + memory, "-jar", "./server.jar", nogui)
                    .directory(new File(directory))
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch(FileNotFoundException e) {
            System.out.println("No downloadable server software found");
        } catch (IOException e) {
            System.out.println("No Server found in Directory");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
