// #[macro_use]

extern crate reqwest;
pub extern crate serde;
pub extern crate serde_json;
pub use serde::{Deserialize, Serialize};

mod handle;

pub use self::handle::createtask::createtask;
pub use self::handle::deletetasks::deletetasks;
pub use self::handle::listtasks::{gettasks, listtasks};
pub use self::handle::startstop::{starttask, stoptasks};
