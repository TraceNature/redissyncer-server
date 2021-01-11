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

package syncer.transmission.util;

import syncer.jedis.Jedis;
import syncer.jedis.ScanParams;
import syncer.jedis.ScanResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 平行时空
 * @created 2019-04-26 14:52
 **/
public class JeUtil {
    public static List<String> getScan(Jedis jedis, String key) {
        List<String> list = new ArrayList<>();
        ScanParams params = new ScanParams();
        params.match(key);
        params.count(100);
        while (true) {
            ScanResult scanResult = jedis.scan("0",params);
            List<String> elements = scanResult.getResult();
            if (elements != null && elements.size() > 0) {
                list.addAll(elements);
            }
            String cursor = scanResult.getCursor();
            if ("0".equals(cursor)) {
                break;
            }
        }
        return list;
    }

    public static Set<String> getScanSet(Jedis jedis, String key) {
        Set<String> list = new HashSet<String>();
        ScanParams params = new ScanParams();
        params.match(key);
        params.count(100);
        while (true) {
            ScanResult scanResult = jedis.scan("0",params);
            List<String> elements = scanResult.getResult();
            if (elements != null && elements.size() > 0) {
                list.addAll(elements);
            }
            String cursor = scanResult.getCursor();
            if ("0".equals(cursor)) {
                break;
            }
        }
        return list;
    }
}
