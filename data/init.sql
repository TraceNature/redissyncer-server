

/*
 Navicat Premium Data Transfer

 Source Server         : syncer
 Source Server Type    : SQLite
 Source Server Version : 3017000
 Source Schema         : main

 Target Server Type    : SQLite
 Target Server Version : 3017000
 File Encoding         : 65001

 Date: 09/03/2020 17:45:57
*/

PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for sqlite_sequence
-- ----------------------------
DROP TABLE IF EXISTS "sqlite_sequence";
CREATE TABLE "sqlite_sequence" (
  "name",
  "seq"
);

-- ----------------------------
-- Records of "sqlite_sequence"
-- ----------------------------
INSERT INTO "sqlite_sequence" VALUES ('t_rdb_version', NULL);

-- ----------------------------
-- Table structure for t_rdb_version
-- ----------------------------
DROP TABLE IF EXISTS "t_rdb_version";
CREATE TABLE "t_rdb_version" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "redis_version" text(15) DEFAULT '',
  "rdb_version" integer(2)
);

-- ----------------------------
-- Table structure for t_task
-- ----------------------------
DROP TABLE IF EXISTS "t_task";
CREATE TABLE "t_task" (
  "id" varchar(255) NOT NULL,
  "groupId" varchar(255) NOT NULL,
  "taskName" varchar(50) NOT NULL DEFAULT '',
  "sourceRedisAddress" varchar(255) NOT NULL,
  "targetRedisAddress" varchar(255) NOT NULL,
  "sourcePassword" varchar(255) DEFAULT '',
  "targetPassword" varchar(255) DEFAULT '',
  "autostart" integer(2) NOT NULL DEFAULT 1,
  "afresh" integer(2) NOT NULL DEFAULT 1,
  "batchSize" integer NOT NULL DEFAULT 1500,
  "tasktype" integer(2) NOT NULL DEFAULT 1,
  "offsetPlace" integer(2) NOT NULL,
  "taskMsg" varchar(255) DEFAULT '',
  "offset" integer NOT NULL DEFAULT -1,
  "status" integer NOT NULL DEFAULT 0,
  PRIMARY KEY ("id")
);

-- ----------------------------
-- Auto increment value for t_rdb_version
-- ----------------------------

PRAGMA foreign_keys = true;
