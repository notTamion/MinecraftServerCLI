package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;

@CommandLine.Command(name = "builds", description = "List all available build for a version", mixinStandardHelpOptions = true)
public class BuildsCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project that has the version") String project = "paper";
    @CommandLine.Parameters(index = "0", description = "Version you want to list the builds for", arity = "0..1") String version = "latest";
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Also list changes") boolean verbose;
    @Override
    public void run() {
        try {
            if(version.equals("latest")) {
                String[] versions;
                if(project.equals("purpur")) {
                    versions = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur")).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                } else {
                    versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
                }
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            StringBuilder sb = new StringBuilder();
            if(verbose) {
                JsonNode json;
                if(project.equals("purpur")) {
                    json = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur/" + version + "?detailed=true"));
                    for (JsonNode build : json.get("builds").get("all")) {
                        sb.append("\033[44m" + build.get("build") + "\033[0m\n");
                        for(JsonNode changes : build.get("commits")) {
                            sb.append("\033[0;36m" + changes.get("hash").asText() + "\033[0m\n" + changes.get("description").asText().replaceAll("\n", "").replaceAll("\r", "") + "\n\n");
                        }
                    }
                } else {
                    json = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds"));
                    for (JsonNode build : json.get("builds")) {
                        sb.append("\033[44m" + build.get("build") + "\033[0m\n");
                        for(JsonNode changes : build.get("changes")) {
                            sb.append("\033[0;36m" + changes.get("commit").asText() + "\033[0m\n" + changes.get("message").asText().replaceAll("\n", "").replaceAll("\r", "") + "\n\n");
                        }
                    }
                }
            } else {
                if(project.equals("purpur")) {
                    sb.append(new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur/" + version)).get("builds").get("all").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ", ").replaceAll("\"", ""));
                } else {
                    sb.append(new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ", "));
                }
            }
            System.out.println(sb.toString());
        } catch (IOException e) {
            System.out.println("Couldn't find version");
        }
    }
}
