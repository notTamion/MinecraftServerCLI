package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;

@CommandLine.Command(name = "projects", description = "list all available Projects", mixinStandardHelpOptions = true)
public class ProjectsCommand implements Runnable {
    @Override
    public void run() {
        try {
            System.out.println(new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects")).get("projects").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", ") + ", " + new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2")).get("projects").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", "));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
