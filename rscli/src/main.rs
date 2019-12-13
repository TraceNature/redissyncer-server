#[macro_use]
extern crate log;
extern crate chrono;
extern crate env_logger;
#[macro_use]
extern crate serde_json;
// extern crate serde_yaml;

use chrono::Local;
use env_logger::Builder;
use log::LevelFilter;
use rscli::deletetasks;
use std::io::Read;
use std::io::Write;

use clap::{App, Arg, SubCommand};
use yaml_rust::YamlLoader;

use rscli::{createtask, listtasks, starttask, stoptasks};

fn main() {
    Builder::new()
        .format(|buf, record| {
            writeln!(
                buf,
                "{} [{}] - {}",
                Local::now().format("%Y-%m-%dT%H:%M:%S"),
                record.level(),
                record.args()
            )
        })
        .filter(None, LevelFilter::Info)
        .init();

    info!("welcom use rscli");

    let matches = App::new("rscli")
        .version("1.0")
        .author("JiaShiwen")
        .about("redis syncer cli client")
        .arg(
            Arg::with_name("config")
                .short("c")
                .long("config")
                .value_name("FILE")
                .help("Sets a custom config file")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("v")
                .short("v")
                .multiple(true)
                .help("Sets the level of verbosity"),
        )
        .subcommand(
            SubCommand::with_name("createtask")
                .about("controls testing features")
                .version("1.3")
                .author("Someone E. <someone_else@other.com>")
                .arg(
                    Arg::with_name("execute")
                        .short("e")
                        .long("execute")
                        .value_name("executefile")
                        .help("execute command by json file.")
                        .takes_value(true),
                )
                .arg(
                    Arg::with_name("taskname")
                        .long("taskname")
                        .short("n")
                        .takes_value(true)
                        .help("Task Name "),
                )
                .arg(
                    Arg::with_name("source")
                        .long("source")
                        .short("s")
                        .takes_value(true)
                        .help("Source addresses '10.0.0.100:6897,10.0.0.111:6878,...'"),
                )
                .arg(
                    Arg::with_name("target")
                        .long("target")
                        .short("t")
                        .takes_value(true)
                        .help("Target addresses '10.0.0.100:6897,10.0.0.111:6878,...'"),
                )
                .arg(
                    Arg::with_name("sourcepasswd")
                        .long("sourcepasswd")
                        .default_value("")
                        .help("sourcepasswd"),
                )
                .arg(
                    Arg::with_name("targetpasswd")
                        .long("targetpasswd")
                        .default_value("")
                        .help("target passwd"),
                )
                .arg(
                    Arg::with_name("autostart")
                        .long("autostart")
                        .short("a")
                        .help("autostart"),
                ),
        )
        .subcommand(
            SubCommand::with_name("starttask")
                .about("Start tasks")
                .version("1.0")
                .author("JiaShiwen . <jiashiwen@jd.com>")
                .arg(
                    Arg::with_name("taskid")
                        .short("i")
                        .long("taskid")
                        .takes_value(true)
                        .help("By specifying taskid to start task"),
                ),
        )
        .subcommand(
            SubCommand::with_name("stoptasks")
                .about("Start tasks")
                .version("1.0")
                .author("JiaShiwen . <jiashiwen@jd.com>")
                .arg(
                    Arg::with_name("taskids")
                        .short("i")
                        .long("taskids")
                        .takes_value(true)
                        .help("By specifying taskid to stop task,multi id please us 'id1,id2,...'"),
                ),
        )
        .subcommand(
            SubCommand::with_name("listtasks")
                .about("List tasks")
                .version("1.0")
                .author("JiaShiwen . <jiashiwen@jd.com>")
                .arg(
                    Arg::with_name("all")
                        .short("a")
                        .long("all")
                        .help("List all tasks"),
                )
                .arg(
                    Arg::with_name("ids")
                        .short("i")
                        .long("ids")
                        .takes_value(true)
                        .help("List by taskids,splite by ','"),
                )
                .arg(
                    Arg::with_name("names")
                        .short("n")
                        .long("names")
                        .takes_value(true)
                        .help("List by tasknames,splite by ','"),
                )
                .arg(
                    Arg::with_name("status")
                        .short("s")
                        .long("status")
                        .takes_value(true)
                        .help("List by taskstatus,live、stop、broken"),
                ),
        )
        .subcommand(
            SubCommand::with_name("deletetasks")
                .about("Delete tasks")
                .version("1.0")
                .author("JiaShiwen . <jiashiwen@jd.com>")
                .arg(
                    Arg::with_name("taskids")
                        .short("i")
                        .long("taskids")
                        .takes_value(true)
                        .help("delete task by taskid"),
                ),
        )
        .get_matches();

    //校验配置文件
    let configfile = matches.value_of("config").unwrap_or("config.yml");

    let path = std::path::Path::new(&configfile);
    let mut file = match std::fs::File::open(path) {
        Result::Ok(val) => val,
        Result::Err(err) => {
            error!("Unable to open file '{}': \n {:#?}", configfile, err);
            std::process::exit(1);
        }
    };

    // .expect("Unable to open file");
    let mut contents = String::new();
    file.read_to_string(&mut contents)
        .expect("Unable to read file");
    let docs = YamlLoader::load_from_str(&contents).unwrap();
    let doc = &docs[0];

    //提取server字段值
    if doc["server"].is_badvalue() {
        error!("Server field not exists,pleas check your config file");
        std::process::exit(1);
    }

    println!("yaml doc is: {:?}", doc["server"]);
    let address = doc["server"].as_str().unwrap();
    let mut urlhead = String::from("http://");
    urlhead.push_str(&address);

    println!("Using urlhead is: {}", urlhead);

    // Vary the output based on how many times the user used the "verbose" flag
    // (i.e. 'myprog -v -v -v' or 'myprog -vvv' vs 'myprog -v'
    match matches.occurrences_of("v") {
        0 => println!("No verbose info"),
        1 => println!("Some verbose info"),
        2 => println!("Tons of verbose info"),
        3 | _ => println!("Don't be crazy"),
    }

    //创建任务
    if let Some(matches) = matches.subcommand_matches("createtask") {
        if matches.is_present("debug") {
            println!("Printing debug info...");
        } else {
            println!("Printing normally...");
        }
    }

    if let Some(matches) = matches.subcommand_matches("createtask") {
        let exefile = match matches.value_of("execute") {
            Some(ok) => ok,
            None => "",
        };

        //通过文件创建task
        if exefile != "" {
            info!("use execute file:'{}'", exefile);

            let file = match std::fs::File::open(exefile) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("Open execute file error: {:#?}", err);
                    std::process::exit(1);
                }
            };

            let json: serde_json::Value = match serde_json::from_reader(file) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("parse json error: {:#?}", err);
                    std::process::exit(1);
                }
            };

            match createtask(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("Create tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
            std::process::exit(0);
        }

        //通过参数创建task
        let mut map = serde_json::Map::new();
        map.insert(
            "threadName".to_string(),
            json!(matches.value_of("taskname").unwrap()),
        );
        map.insert(
            "sourceRedisAddress".to_string(),
            json!(matches.value_of("source").unwrap()),
        );
        map.insert(
            "targetRedisAddress".to_string(),
            json!(matches.value_of("target").unwrap()),
        );
        map.insert(
            "sourcePassword".to_string(),
            json!(matches.value_of("sourcepasswd").unwrap()),
        );
        map.insert(
            "targetPassword".to_string(),
            json!(matches.value_of("targetpasswd").unwrap()),
        );

        if matches.is_present("autostart") {
            info!("auto start is : true")
        }
        let json = json!(map);
        match createtask(&mut urlhead, &json) {
            Result::Ok(val) => val,
            Result::Err(err) => {
                error!("Create tasks error: {:#?}", err);
                std::process::exit(1);
            }
        };
    }

    //启动任务
    if let Some(matches) = matches.subcommand_matches("starttask") {
        if matches.is_present("taskid") {
            let taskid = matches.value_of("taskid").unwrap();
            let mut map = serde_json::Map::new();
            map.insert("taskid".to_string(), json!(taskid));
            let json = json!(map);
            match starttask(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("Start  tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
        }
        std::process::exit(0);
    }

    //停止任务
    if let Some(matches) = matches.subcommand_matches("stoptasks") {
        if matches.is_present("taskids") {
            let mut taskids: Vec<&str> = matches.value_of("taskids").unwrap().split(",").collect();
            taskids.sort();
            taskids.dedup();
            let mut map = serde_json::Map::new();
            map.insert("taskids".to_string(), json!(taskids));
            let json = json!(map);
            match stoptasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("Delete tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
        }
        std::process::exit(0);
    }

    //任务列表
    if let Some(matches) = matches.subcommand_matches("listtasks") {
        if matches.is_present("all") {
            let mut map = serde_json::Map::new();
            map.insert("regulation".to_string(), json!("all"));
            let json = json!(map);
            match listtasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("List tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
            std::process::exit(0);
        }
        if matches.is_present("ids") {
            let mut ids: Vec<&str> = matches.value_of("ids").unwrap().split(",").collect();
            ids.sort();
            ids.dedup();
            let mut map = serde_json::Map::new();
            map.insert("regulation".to_string(), json!("byids"));
            map.insert("taskids".to_string(), json!(ids));
            let json = json!(map);
            match listtasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("List tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
            std::process::exit(0);
        }
        if matches.is_present("names") {
            let mut names: Vec<&str> = matches.value_of("names").unwrap().split(",").collect();
            names.sort();
            names.dedup();
            let mut map = serde_json::Map::new();
            map.insert("regulation".to_string(), json!("bynames"));
            map.insert("tasknames".to_string(), json!(names));
            let json = json!(map);
            match listtasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("List tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
            std::process::exit(0);
        }
        if matches.is_present("status") {
            let status = matches.value_of("status").unwrap();
            let mut map = serde_json::Map::new();
            map.insert("regulation".to_string(), json!("bystatus"));
            map.insert("taskstatus".to_string(), json!(status));
            let json = json!(map);
            match listtasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("List tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
            std::process::exit(0);
        }
    }

    //删除任务
    if let Some(matches) = matches.subcommand_matches("deletetasks") {
        if matches.is_present("taskids") {
            let mut taskids: Vec<&str> = matches.value_of("taskids").unwrap().split(",").collect();
            taskids.sort();
            taskids.dedup();
            let mut map = serde_json::Map::new();
            map.insert("taskids".to_string(), json!(taskids));
            let json = json!(map);
            match deletetasks(&mut urlhead, &json) {
                Result::Ok(val) => val,
                Result::Err(err) => {
                    error!("Delete tasks error: {:#?}", err);
                    std::process::exit(1);
                }
            };
        }
        std::process::exit(0);
    }
}
