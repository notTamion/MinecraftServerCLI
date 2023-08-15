package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;

@CommandLine.Command(name = "versions", description = "List all available versions for a project", mixinStandardHelpOptions = true)
public class VersionsCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "Project you want to list the versions for", arity = "0..1") String project = "paper";
    @Override
    public void run() {
        project = project.toLowerCase();
        try {
            switch (project) {
                case "fabric":
                    JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/game"));
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode build : json) {
                        sb.append(build.get("version").asText() + ", ");
                    }
                    System.out.println(sb);
                    break;
                case "magma": System.out.println(new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/allVersions")).toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", ")); break;
                case "purpur": System.out.println(new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur")).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", ")); break;
                default: System.out.println(new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("\"", "").replaceAll(",", ", "));
            }
        } catch (IOException e) {
            System.out.println("No versions for project " + project + " found");
        }
    }
}