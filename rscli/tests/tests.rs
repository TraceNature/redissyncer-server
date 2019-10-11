use assert_cmd::prelude::*;
// use predicates::str::contains;
use std::process::Command;

#[test]
fn cli_no_args() {
    Command::cargo_bin("rscli").unwrap().assert().failure();
}

#[test]
fn cli_version() {
    Command::cargo_bin("rscli")
        .unwrap()
        .arg("-V")
        .assert()
        .success();
}
