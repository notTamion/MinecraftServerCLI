package de.tamion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
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
        project = project.toLowerCase();
        try {
            if(version.equalsIgnoreCase("latest")) {
                String[] versions;
                switch (project) {
                    case "fabric": JsonNode json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions/game"));versions = new String[]{json.iterator().next().get("version").asText()}; break;
                    case "magma": versions = new String[]{IOUtils.toString(new URL("https://api.magmafoundation.org/api/v2/latestVersion"))}; break;
                    case "purpur": versions = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); break;
                    default: versions = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project)).get("versions").toString().replaceAll("\\[", "").replaceAll("]", "").split(","); break;
                }
                version = versions[versions.length - 1].replaceAll("\"", "");
            }
            StringBuilder sb = new StringBuilder();
            JsonNode json;
            if(verbose) {
                switch (project) {
                    case "fabric":
                        json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions"));
                        sb.append("Loader: ");
                        for (JsonNode lbuild : json.get("loader")) {
                            sb.append(lbuild.get("version").asText() + ", ");
                        }
                        sb.append("\n\nInstaller: ");
                        for (JsonNode ibuild : json.get("installer")) {
                            sb.append(ibuild.get("version").asText() + ", ");
                        }
                        break;
                    case "magma": json = new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/" + version));
                        for (JsonNode build : json) {
                            sb.append(build.get("name") + ", ");
                        }
                        break;
                    case "purpur":
                        json = new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/" + project + "/" + version + "?detailed=true"));
                        for (JsonNode build : json.get("builds").get("all")) {
                            sb.append(build.get("build") + "\n");
                            for(JsonNode changes : build.get("commits")) {
                                sb.append(changes.get("hash").asText()+ "\n" + changes.get("description").asText().replaceAll("\n", "").replaceAll("\r", "") + "\n\n");
                            }
                        }
                        break;
                    default:
                        json = new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version + "/builds"));
                        for (JsonNode build : json.get("builds")) {
                            sb.append(build.get("build") + "\n");
                            for(JsonNode changes : build.get("changes")) {
                                sb.append(changes.get("commit").asText() + "\n" + changes.get("message").asText().replaceAll("\n", "").replaceAll("\r", "") + "\n\n");
                            }
                        }
                }
            } else {
                switch (project) {
                    case "fabric":
                        json = new ObjectMapper().readTree(new URL("https://meta.fabricmc.net/v2/versions"));
                        sb.append("Loader: ");
                        for (JsonNode lbuild : json.get("loader")) {
                            sb.append(lbuild.get("version").asText() + ", ");
                        }
                        sb.append("\n\nInstaller: ");
                        for (JsonNode ibuild : json.get("installer")) {
                            sb.append(ibuild.get("version").asText() + ", ");
                        }
                        break;
                    case "magma": json = new ObjectMapper().readTree(new URL("https://api.magmafoundation.org/api/v2/" + version));
                        for (JsonNode build : json) {
                            sb.append(build.get("name") + ", ");
                        }
                        break;
                    case "purpur": sb.append(new ObjectMapper().readTree(new URL("https://api.purpurmc.org/v2/purpur/" + version)).get("builds").get("all").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ", ").replaceAll("\"", "")); break;
                    default: sb.append(new ObjectMapper().readTree(new URL("https://api.papermc.io/v2/projects/" + project + "/versions/" + version)).get("builds").toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ", ")); break;
                }
            }
            System.out.println(sb);
        } catch (IOException e) {
            System.out.println("Couldn't find any builds for " + project + " version " + version);
        }
    }
}
