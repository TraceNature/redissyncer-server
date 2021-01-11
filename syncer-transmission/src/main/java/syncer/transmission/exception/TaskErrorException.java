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

package syncer.transmission.exception;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/7
 */
public class TaskErrorException extends Exception{
    private static final long serialVersionUID = -1L;

    public TaskErrorException() {
        super();
    }

    public TaskErrorException(String message) {
        super(message);
    }

    public TaskErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
