package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(name = "start", description = "Start Server", mixinStandardHelpOptions = true)
public class StartCommand implements Runnable {

    @CommandLine.Option(names = {"-d", "--directory"}, description = "Server Directory") String directory = ".";
    @Override
    public void run() {
        try {
            if(new File(directory + "/papercli.properties").exists()) {
                Properties props = new Properties();
                props.load(new FileReader(new File(directory + "/papercli.properties")));
                if (props.getProperty("AutoUpdater").equals("true")) {
                    String project = props.getProperty("project");
                    String version = props.getProperty("version");
                    String currentbuild = props.getProperty("build");
                    String[] builds = new ObjectMapper().readTree(IOUtils.toString(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version))).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                    String latestbuild = builds[builds.length - 1];
                    if (!latestbuild.equals(currentbuild)) {
                        System.out.println("Downloading " + project + " version " + version + " build #" + latestbuild + "...");
                        FileUtils.copyURLToFile(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds/" + latestbuild + "/downloads/" + project + "-" + version + "-" + latestbuild + ".jar"), new File(directory + "/server.jar"));
                        System.out.println("Downloaded Server");
                    }
                }
            }
            System.out.println("Starting server");
            new ProcessBuilder("java", "-jar", "./server.jar")
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
