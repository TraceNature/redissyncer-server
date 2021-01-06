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

package syncer.replica.entity;




import syncer.replica.util.objectutil.Strings;
import lombok.Getter;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Leon Chen
 * @since 2.4.0
 */

@Getter
public final class RedisURI implements Comparable<RedisURI>, Serializable {

    private static final long serialVersionUID = 1L;

    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private String string;

    private transient String user;
    private transient boolean ssl;
    private transient String host;
    private transient String path;
    private transient String query;
    private transient int port = -1;
    private transient String scheme;
    private transient String password;
    private transient String userInfo;
    private transient String fragment;
    private transient String authority;
    private transient FileType fileType;

    private transient URI uri;
    public Map<String, String> parameters = new HashMap<>();

    public RedisURI(String uri) throws URISyntaxException {
        parse(uri);
        this.string = this.uri.toString();
    }

    /**
     * @param uri uri
     * @throws URISyntaxException illegal uri
     * @since 3.0.0
     */
    public RedisURI(URI uri) throws URISyntaxException {
        this(Objects.requireNonNull(uri).toString());
    }

    /**
     * @param file file
     * @throws URISyntaxException illegal file
     * @throws MalformedURLException illegal file
     * @since 3.0.0
     */
    public RedisURI(File file) throws MalformedURLException, URISyntaxException {
        this(fromFile(file));
        if (getFileType() == null) {
            throw new MalformedURLException(this.string);
        }
    }



    @Override
    public boolean equals(Object o) {
        return o instanceof RedisURI && this.uri.equals(((RedisURI) o).uri);
    }

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    @Override
    public int compareTo(RedisURI that) {
        return this.uri.compareTo(that.uri);
    }

    @Override
    public String toString() {
        return this.uri.toString();
    }

    public URL toURL() throws MalformedURLException {
        Objects.requireNonNull(getFileType());
        try {
            return new URI("file", uri.getRawAuthority(), uri.getRawPath(), uri.getRawQuery(), uri.getRawFragment()).toURL();
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    public String toASCIIString() {
        return encode(this.uri.toString());
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.defaultWriteObject();
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        this.port = -1;
        is.defaultReadObject();
        try {
            parse(this.string);
        } catch (URISyntaxException x) {
            IOException y = new InvalidObjectException("Invalid Redis URI");
            y.initCause(x);
            throw y;
        }
    }

    private void parse(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
        if (this.uri.getScheme() != null) {
            if (this.uri.getScheme().equalsIgnoreCase("redis")) {
                this.scheme = "redis";
            } else if (this.uri.getScheme().equalsIgnoreCase("rediss")) {
                this.scheme = "rediss";
                this.ssl = true;
            } else {
                throw new URISyntaxException(uri, "scheme must be [redis] or [rediss].");
            }
        } else {
            throw new URISyntaxException(uri, "scheme must be [redis] or [rediss].");
        }
        this.host = this.uri.getHost();
        this.path = this.uri.getPath();
        this.query = this.uri.getQuery();
        this.port = this.uri.getPort() == -1 ? 6379 : this.uri.getPort();
        this.userInfo = this.uri.getUserInfo();
        this.fragment = this.uri.getFragment();
        this.authority = this.uri.getAuthority();
        if (this.path != null && this.userInfo == null) {
            int idx = this.path.lastIndexOf('.');
            if (idx >= 0) {
                String type = this.path.substring(idx + 1);
                this.fileType = FileType.parse(type);
            }
        }

        if (this.userInfo != null) {
            int idx = this.userInfo.indexOf(':');
            if (idx < 0) {
                this.user = this.userInfo;
            } else if (idx == 0) {
                this.password = this.userInfo.substring(idx + 1);
            } else /*（idx > 0）*/{
                this.user = this.userInfo.substring(0, idx);
                String password = this.userInfo.substring(idx + 1);
                if (password != null && password.length() != 0) {
                    this.password = password;
                }
            }
        }

        String rawQuery = this.uri.getRawQuery();
        if (rawQuery == null) {
            return;
        }

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        StringBuilder sb = key;
        for (char c : rawQuery.toCharArray()) {
            switch (c) {
                case '&':
                case ';':
                    if (key.length() > 0 && value.length() > 0) {
                        parameters.put(decode(key.toString()), decode(value.toString()));
                    }
                    key.setLength(0);
                    value.setLength(0);
                    sb = key;
                    break;
                case '=':
                    sb = value;
                    break;
                default:
                    sb.append(c);
            }
        }
        if (key.length() > 0 && value.length() > 0) {
            parameters.put(decode(key.toString()), decode(value.toString()));
        }
    }

    private static String decode(String s) {
        if (s == null) {
            return null;
        }
        int n = s.length();
        if (n == 0) {
            return s;
        }
        if (s.indexOf('%') < 0) {
            return s;
        }

        StringBuilder sb = new StringBuilder(n);
        ByteBuffer bb = ByteBuffer.allocate(n);

        char c = s.charAt(0);

        int i = 0;
        while (i < n) {
            if (c != '%') {
                sb.append(c);
                if (++i >= n)
                {
                    break;
                }
                c = s.charAt(i);
                continue;
            }
            bb.clear();
            while (true) {
                bb.put(decode(s.charAt(++i), s.charAt(++i)));
                if (++i >= n) {
                    break;
                }
                c = s.charAt(i);
                if (c != '%') {
                    break;
                }
            }
            bb.flip();
            CharBuffer cb = Strings.decode(bb);
            sb.append(cb.toString());
        }

        return sb.toString();
    }

    private static int decode(char c) {
        if ((c >= '0') && (c <= '9'))
        {
            return c - '0';
        }
        if ((c >= 'a') && (c <= 'f'))
        {
            return c - 'a' + 10;
        }
        if ((c >= 'A') && (c <= 'F'))
        {
            return c - 'A' + 10;
        }
        return -1;
    }

    private static byte decode(char c1, char c2) {
        return (byte) (((decode(c1) & 0xF) << 4) | ((decode(c2) & 0xF) << 0));
    }

    private static String encode(String s) {
        int n = s.length();
        if (n == 0) {
            return s;
        }

        int i = 0;
        while (true) {
            if (s.charAt(i) >= '\u0080') {
                break;
            }
            if (++i >= n) {
                return s;
            }
        }

        String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
        ByteBuffer bb = Strings.encode(CharBuffer.wrap(ns));

        StringBuilder sb = new StringBuilder();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xFF;
            if (b >= 0x80) {
                appendEscape(sb, (byte) b);
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    private static void appendEscape(StringBuilder sb, byte b) {
        sb.append('%');
        sb.append(HEX_DIGITS[(b >> 4) & 0x0F]);
        sb.append(HEX_DIGITS[(b >> 0) & 0x0F]);
    }

    private static URI fromFile(File file) throws URISyntaxException {
        URI u = Objects.requireNonNull(file).toURI();
        return new URI("redis", u.getRawAuthority(), u.getRawPath(), u.getRawQuery(), u.getRawFragment());
    }
}
