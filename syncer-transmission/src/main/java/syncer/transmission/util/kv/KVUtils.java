// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.util.kv;

import syncer.replica.cmd.impl.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.replica.rdb.sync.datatype.DumpKeyValuePair;
import syncer.replica.util.objectutil.Strings;

public class KVUtils {
    public static synchronized String getKey(Event event) {
        try {
            if (event instanceof DefaultCommand) {
                DefaultCommand dc = (DefaultCommand) event;
                return Strings.byteToString(dc.getArgs()[0]);
            }else   if (event instanceof BatchedKeyValuePair<?, ?>) {
                BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
                return (String) batchedKeyValuePair.getKey();
            }else   if (event instanceof DumpKeyValuePair) {
                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;

                return Strings.byteToString(valueDump.getKey());
            }

        }catch (Exception e){

        }
        return null;
    }
}
