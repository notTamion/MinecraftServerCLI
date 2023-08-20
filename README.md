# MinecraftServerCLI
Command Line Interface for listing, installing builds of Minecraft Servers

Installation guide: https://github.com/notTamion/MinecraftServerCLI/wiki/Installation

Currently supported servers: paper, travertine, waterfall, velocity, folia, purpur, magma, fabric

Here are a few examples:\
`mcs projects`Lists all currently available projects\
`mcs versions`Lists all currently available versions of paper\
`mcs versions purpur`Lists all currently available versions of purpur\
`mcs builds 1.19.2`Lists all currently available builds of paper version 1.19.2\
`mcs builds -p purpur`Lists all currently available builds of the latest purpur version\
`mcs install`Installs and starts the latest build of paper\
`mcs install 1.19.2 -p purpur`Installs and starts the latest build of purpur version 1.19.2\
`mcs install -d Servers/Paper -b 131 -m 4G -n`Installs without starting the build 131 of the latest paper version in the directory "Servers/Paper" giving it 4G of memory when using mcs start\
`mcs start -m 2G`Starts the server in the current directory with 2G of memory\
`mcs start -d Servers/Paper`Starts the server in the directory "Servers/Paper"\

Note: Most commands default to the latest build/version of paper

For more information use the --help flag on the command\
For help please contact me on spigot or discord: tamion