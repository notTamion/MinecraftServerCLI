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
    Versions {project: str},
}

#[tokio::main]
async fn main() {
    let cli = Cli::parse();

    match &cli.command {
        Commands::Projects {} => {
            println!("{}", join_value_vec(get_json_response("https://api.papermc.io/v2/projects").await.unwrap().get("projects").unwrap(), ", "));
        }
        Commands::Versions { project } => {

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