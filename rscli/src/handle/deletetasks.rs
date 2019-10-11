//删除任务
use reqwest::Url;
use std::io::Read;

pub fn deletetasks(addr: &str, json: &serde_json::Value) -> Result<(), Box<std::error::Error>> {
    let client = reqwest::Client::new();
    let mut servicepath = String::from(addr);
    servicepath.push_str("/api/v1/deletetask");
    let url = Url::parse(&mut servicepath)?;
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
