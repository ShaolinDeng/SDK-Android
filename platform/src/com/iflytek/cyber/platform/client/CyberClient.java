/*
 * Copyright (C) 2018 iFLYTEK CO.,LTD.
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

package com.iflytek.cyber.platform.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;

import okio.BufferedSource;
import okio.Source;

public abstract class CyberClient {

    final Gson gson;
    final DirectiveListener listener;

    CyberClient(Gson gson, DirectiveListener listener) {
        this.gson = gson;
        this.listener = listener;
    }

    public abstract void listen();

    public abstract void disconnect();

    public abstract void postEvent(JsonObject envelope, Source audio, EventCallback callback);

    public abstract void cancelEvent();

    public interface DirectiveListener {

        void onConnected();

        void onDisconnected(Throwable e);

        void onDirective(JsonObject directive);

        void onAttachment(String cid, BufferedSource source);

    }

    public interface EventCallback {

        void onDirective(JsonObject directive);

        void onFailure(Throwable e);

        void onEnd();

    }

    public static class CyberException extends IOException {

        int code;
        String message;

        CyberException(int code) {
            this(code, null);
        }

        CyberException(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static boolean isAuthFailed(Throwable t) {
            if (!(t instanceof CyberException)) {
                return false;
            }

            final CyberException e = (CyberException) t;
            return e.code == 401;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "CyberException[code=%d, message=%s]",
                    code, message);
        }

    }

}
