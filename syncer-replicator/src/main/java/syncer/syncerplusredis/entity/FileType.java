/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncer.syncerplusredis.entity;

/**
 * @since 2.1.0
 */
public enum FileType {
    //AOF REB MIXED 分别对应 aof rdb mixed混合文件
    AOF, RDB, MIXED,ONLINEAOF,ONLINERDB,SYNC,ONLINEMIXED,COMMANDDUMPUP;

    /**
     * @param type string type
     * @return FileType
     * @since 2.4.0
     */
    static FileType parse(String type) {
        if (type == null) {
            return null;
        } else if ("aof".equalsIgnoreCase(type)) {
            return AOF;
        } else if ("rdb".equalsIgnoreCase(type)) {
            return RDB;
        } else if ("mix".equalsIgnoreCase(type)) {
            return MIXED;
        } else if ("mixed".equalsIgnoreCase(type)) {
            return MIXED;
        } else if ("onelinerdb".equalsIgnoreCase(type)) {
            return ONLINERDB;
        } else if ("onelineaof".equalsIgnoreCase(type)) {
            return ONLINEAOF;
        }else if("commanddumpup".equalsIgnoreCase(type)){
            return COMMANDDUMPUP;
        } else {
            return null;
        }
    }
}
