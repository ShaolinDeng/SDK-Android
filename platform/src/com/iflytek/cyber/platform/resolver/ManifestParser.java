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

package com.iflytek.cyber.platform.resolver;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.iflytek.cyber.CyberDelegate;
import com.iflytek.cyber.platform.CyberCore;
import com.iflytek.cyber.platform.CyberDelegateImpl;
import com.iflytek.cyber.resolver.ResolverModule;

class ManifestParser {

    private static final String TAG = "ManifestParser";

    private static final String MODULE_PREFIX = "ResolverModule:";

    static void parse(Context context, CyberCore core, ResolverManager manager) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);

            if (ai.metaData == null) {
                return;
            }

            for (String className : ai.metaData.keySet()) {
                Object value = ai.metaData.get(className);
                if (!(value instanceof String) || !((String) value).startsWith(MODULE_PREFIX)) {
                    continue;
                }

                final String namespace = ((String) value).substring(MODULE_PREFIX.length());

                Object module;

                try {
                    module = Class.forName(className)
                            .getDeclaredConstructor(Context.class, CyberDelegate.class)
                            .newInstance(context, new CyberDelegateImpl(core, namespace));
                } catch (Exception ignored) {
                    try {
                        module = Class.forName(className)
                                .getDeclaredConstructor(Context.class)
                                .newInstance(context);
                        Log.e(TAG, "Resolver module " + className + " is using a deprecated " +
                                "constructor ResolverModule(Context)");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to instantiate resolver module: " + className, e);
                        continue;
                    }
                }

                if (!(module instanceof ResolverModule)) {
                    Log.e(TAG, "Not a ResolverModule: " + className);
                    continue;
                }

                manager.register(namespace, (ResolverModule) module);
                Log.d(TAG, "Loaded resolver module: " + className);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse meta-data", e);
        }
    }

}
