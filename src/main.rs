use clap::{Parser, Subcommand};
use serde_json::Value;

#[derive(Parser)]
#[command(author, version, about, long_about = None)]
#[command(propagate_version = true)]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// List all available Projects
    Projects {},
    /// List all available Versions for the Project
    Versions {
        #[clap(default_value = "paper")]
        project: String
    },
    /// List all available Builds for the Version of the Project
    Builds {
        #[arg(long, short, default_value = "paper", alias = "p")]
        project: String,
        #[clap(default_value = "latest")]
        ver: String
    },
    /// Install the selected Build
    Install {
        #[arg(long, short, default_value = ".", alias = "d")]
        directory: String,
        #[arg(long, short, default_value = "paper", alias = "p")]
        project: String,
        #[clap(default_value = "latest")]
        ver: String,
        #[arg(long, short, default_value = "latest", alias = "b")]
        build: i32,
        #[arg(long, short, default_value = "java -Xms%memory -Xmx%memory -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar ./server.jar %nogui", alias = "d")]
        startcommand: String,
        #[arg(long, short, alias = "ns")]
        nostart: bool,
        #[arg(long, short, alias = "ng")]
        nogui: bool,
        #[arg(long, short, default_value = "2G", alias = "m")]
        memory: String
    }
}

#[tokio::main]
async fn main() {
    let cli = Cli::parse();

    match &cli.command {
        Commands::Projects {} => {
            println!("{}", join_value_vec(get_json_response("https://api.papermc.io/v2/projects").await.unwrap().get("projects").unwrap(), ", "));
        }
        Commands::Versions { project } => {
            println!("{}", join_value_vec(get_json_response(format!("https://api.papermc.io/v2/projects/{}", project).as_str()).await.unwrap().get("versions").unwrap(), ", "));
        }
        Commands::Builds { project, ver } => {
            let mut version = ver.clone();
            if version == "latest" {
                version = get_json_response(format!("https://api.papermc.io/v2/projects/{}", project).as_str()).await.unwrap().get("versions").unwrap().as_array().unwrap().last().unwrap().as_str().unwrap().to_string();
            }
            println!("{}", join_value_int_vec(get_json_response(format!("https://api.papermc.io/v2/projects/{}/versions/{}", project, version).as_str()).await.unwrap().get("builds").unwrap(), ", "));
        }
        Commands::Install { project, ver, build, directory, memory, nogui, nostart, startcommand } => {
            let mut version = ver.clone();
            let mut build = build.clone();
        }
        _ => {}
    }
}

async fn get_json_response(url: &str) -> Option<Value> {
    serde_json::from_str(reqwest::get(url).await.unwrap().text().await.unwrap().as_str()).unwrap()
}

fn join_value_vec(values: &Value, seperator: &str) -> String {
    values.as_array().unwrap().iter().map(|v| v.as_str().unwrap()).collect::<Vec<&str>>().join(seperator)
}

fn join_value_int_vec(values: &Value, seperator: &str) -> String {
    values.as_array().unwrap().iter().map(|v| v.as_i64().unwrap().to_string()).collect::<Vec<String>>().join(seperator)
}
