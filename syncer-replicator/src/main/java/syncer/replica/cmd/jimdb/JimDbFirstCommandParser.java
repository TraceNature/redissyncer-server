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
package syncer.replica.cmd.jimdb;


import syncer.replica.cmd.CommandParser;

/**
 * @author zhanenqiang
 * @Description jimdb增量命令首次解析---> [transmit bj 1 set y y] --》[set y y]
 * @Date 2020/4/29
 */
public class JimDbFirstCommandParser implements CommandParser<JimdbFirstCommand> {

    @Override
    public JimdbFirstCommand parse(Object[] command) {
        Object[]data=new Object[command.length - 3];
        for (int i = 3, j = 0; i < command.length; i++) {
            data[j++]=command[i];
        }
        return new JimdbFirstCommand(data);
    }
}
