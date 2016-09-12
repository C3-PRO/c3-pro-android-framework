package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.os.AsyncTask;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/02/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Generic Task to load an object in the background and pass it back.
 * */
public abstract class LoadResultAsyncTask<T> extends AsyncTask<Void, Void, T> {
    protected String requestID;
    protected Callback.LoadResultCallback resultCallback;

    public LoadResultAsyncTask(String requestID, Callback.LoadResultCallback resultCallback) {
        this.requestID = requestID;
        this.resultCallback = resultCallback;
    }

    @Override
    protected void onPostExecute(T result){
        resultCallback.onSuccess(requestID, result);
    }
}
