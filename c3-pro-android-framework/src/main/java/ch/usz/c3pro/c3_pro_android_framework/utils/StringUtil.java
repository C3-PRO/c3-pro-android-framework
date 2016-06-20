package ch.usz.c3pro.c3_pro_android_framework.utils;

/**
 * C3PRO
 *
 * Created by manny Weber on 05/02/16.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
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

/**
 * This is a helper class providing tools to check Strings for content.
 */
public class StringUtil {
    public static boolean isNotNullOrEmpty (String str){
        return (str != null) && !str.isEmpty();
    }
}
