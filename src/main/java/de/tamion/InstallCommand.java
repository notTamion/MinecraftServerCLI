package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(name = "install", description = "Install PaperMC Server", mixinStandardHelpOptions = true)
public class InstallCommand implements Runnable {
    @CommandLine.Parameters(index = "0", arity = "0..1") String version = "latest";
    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @CommandLine.Option(names = {"-b", "--build"}, description = "Build of Version") String build = "latest";
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project you want to download: paper, velocity, waterfall") String project = "paper";
    @CommandLine.Option(names = {"-n", "--no-start"}, description = "Don't start the server after installing") Boolean nostart = false;
    @CommandLine.Option(names = {"-m", "--memory"}, description = "How much RAM you want to give the server") String memory = "2G";
    @Override
    public void run() {
        try {
            if(version.equals("latest")) {
                String[] versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            if(build.equals("latest")) {
                String[] builds = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                build = builds[builds.length - 1];
            }
            System.out.println("Downloading " + project + " version " + version + " build #" + build + "...");
            FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + build + "/downloads/" + project + "-" + version + "-" + build + ".jar"), new File(directory + "/server.jar"));
            System.out.println("Downloaded Server");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                FileUtils.writeStringToFile(new File(directory + "/start.bat"), "java -jar " + PaperCLICommand.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1) + " start -m \"" + memory + "\"");
            } else {
                FileUtils.writeStringToFile(new File(directory + "/start.sh"), "java -jar " + PaperCLICommand.class.getProtectionDomain().getCodeSource().getLocation().getFile() + " start -m \"" + memory + "\"");
            }
            System.out.println("Created Start Script");
            Properties props = new Properties();
            if(build.equals("latest")) {
                props.setProperty("AutoUpdater", "true");
            } else {
                props.setProperty("AutoUpdater", "false");
            }
            props.setProperty("project", project);
            props.setProperty("version", version);
            props.setProperty("build", build);
            props.store(new FileWriter(directory + "/papercli.properties"), "PaperCLI settings");
            System.out.println("Created Properties File");
            if(!nostart) {
                System.out.println("Starting server");
                new ProcessBuilder("java", "-Xms" + memory, "-Xmx" + memory, "-jar", "./server.jar")
                        .directory(new File(directory))
                        .inheritIO()
                        .start()
                        .waitFor();
            }
        } catch(FileNotFoundException e) {
            System.out.println("No downloadable server software found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
