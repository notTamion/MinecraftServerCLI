package de.tamion;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;

@CommandLine.Command(name = "versions", description = "List all available versions for a project", mixinStandardHelpOptions = true)
public class VersionsCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Project you want to list the versions for", arity = "0..1") String project = "paper";
    @Override
    public void run() {
        try {
            if(project.equals("purpur")) {
                System.out.println(new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur")).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", "));
            } else {
                System.out.println(new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", "));
            }
        } catch (IOException e) {
            System.out.println("No versions for project found");
        }
    }
}
