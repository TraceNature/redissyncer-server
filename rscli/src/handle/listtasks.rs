//任务列表

extern crate reqwest;
use reqwest::Url;
// use serde_json::json;
// use std::collections::hash_map::HashMap;
use std::io::Read;

pub fn gettasks(addr: &str) -> Result<(), Box<std::error::Error>> {
    let url = Url::parse(addr)?;
    let mut res = reqwest::get(url)?;
    let mut body = String::new();
    res.read_to_string(&mut body)?;

    println!("Status: {}", res.status());
    println!("Headers:\n{:#?}", res.headers());
    println!("Body:\n{}", body);
    Ok(())
}

// 获取正在运行的任务列表
pub fn listtasks(addr: &str, json: &serde_json::Value) -> Result<(), Box<std::error::Error>> {
    let client = reqwest::Client::new();
    let mut servicepath = String::from(addr);
    servicepath.push_str("/api/v1/listtasks");
    println!("{:?}", servicepath);
    let url = Url::parse(&mut servicepath)?;
    println!("url is: {}", url);
    // let mut res = reqwest::get(url).header("Content-Type","application/json;charset=utf-8").send()?;
    let mut res = client
        .post(url)
        .header("Content-Type", "application/json;charset=utf-8")
        .body(serde_json::to_string_pretty(&json).unwrap())
        .send()?;
    let mut body = String::new();
    res.read_to_string(&mut body)?;

    // println!("Body:\n{}", body);
    let serde_value: serde_json::Value = serde_json::from_str(&body.to_string()).unwrap();

    println!("{}", serde_json::to_string_pretty(&serde_value).unwrap());
    Ok(())
}
