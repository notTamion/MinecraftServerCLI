package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project you want to download") String project = "paper";
    @CommandLine.Parameters(index = "0", description = "Version you want to install", arity = "0..1") String version = "latest";
    @CommandLine.Option(names = {"-b", "--build"}, description = "Build of Version") String build = "latest";
    @CommandLine.Option(names = {"-n", "--no-start"}, description = "Don't start the server after installing") boolean nostart;
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory = "2G";

    @Override
    public void run() {
        project = project.toLowerCase();
        try {
            Properties props = new Properties();
            if(version.equalsIgnoreCase("latest")) {
                String[] versions;
                switch (project) {
                    case "fabric": JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/game"));versions = new String[]{json.iterator().next().get("version").asText()}; break;
                    case "magma": versions = new String[]{IOUtils.toString(new URL("https://api.magmafoundation.org/api/v2/latestVersion"))}; break;
                    case "purpur": versions = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); break;
                    default: versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                }
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            if(build.equalsIgnoreCase("latest")) {
                switch (project) {
                    case "fabric": JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/loader")); build = json.iterator().next().get("version").asText(); json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/installer"));build = build + ":" + json.iterator().next().get("version").asText();break;
                    case "magma": build = new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest")).get("name").asText(); break;
                    case "purpur": build = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/latest")).get("build").asText(); break;
                    default: String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); build = builds[builds.length - 1]; break;
                }
                props.setProperty("AutoUpdater", "true");
            } else {
                props.setProperty("AutoUpdater", "false");
            }
            System.out.println("Downloading " + project + " version " + version + " build #" + build + "...");
            switch (project) {
                case "fabric": String[] builds = build.split(":"); FileUtils.copyURLToFile(new URL("https://meta.fabricmc.net/v2/versions/loader/" + version + "/" + builds[0] + "/" + builds[1] + "/server/jar"), new File(directory + "/server.jar")); break;
                case "magma": FileUtils.copyURLToFile(new URL("https://api.magmafoundation.org/api/v2/" + version + "/latest/" + build + "/download"), new File(directory + "/server.jar")); break;
                case "purpur": FileUtils.copyURLToFile(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "/" + build + "/download"), new File(directory + "/server.jar")); break;
                default: FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + build + "/downloads/" + project + "-" + version + "-" + build + ".jar"), new File(directory + "/server.jar"));
            }
            System.out.println("Downloaded Server");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                FileUtils.writeStringToFile(new File(directory + "/start.bat"), "mcs start");
            } else {
                FileUtils.writeStringToFile(new File(directory + "/start.sh"), "mcs start");
            }
            System.out.println("Created Start Script");
            props.setProperty("project", project);
            props.setProperty("version", version);
            props.setProperty("build", build);
            props.setProperty("memory", memory);
            props.store(new FileWriter(directory + "/mcscli.properties"), "MinecraftServerCLI settings");
            System.out.println("Created Properties File");
            if(project.equals("paper") || project.equals("purpur") || project.equals("magma") || project.equals("fabric") || project.equals("folia")) {
                FileUtils.writeStringToFile(new File(directory + "/eula.txt"), "eula=true");
                System.out.println("Accepted Eula");
            }
            if(nostart) {
                return;
            }
            String nogui = "";
            if(project.equals("paper") || project.equals("purpur") || project.equals("fabric") || project.equals("folia")) {
                nogui = "--nogui";
            }
            System.out.println("Starting server");
            new ProcessBuilder("java", "-Xms" + memory, "-Xmx" + memory, "-jar", "./server.jar", nogui)
                    .directory(new File(directory))
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch(FileNotFoundException e) {
            System.out.println("No downloadable server software found for " + project + " version " + version + " build " + build);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
