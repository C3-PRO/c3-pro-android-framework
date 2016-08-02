package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import ch.usz.c3pro.c3_pro_android_framework.C3PROErrorCode;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;

/**
 * C3PRO
 *
 * Created by manny Weber on 07/15/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public abstract class LoadResultJob<T> extends Job {
    private static int HANDLER_MESSAGE_RESULT_READY = 1;
    protected String request;
    private Handler dataHandler;
    protected Callback.LoadResultCallback resultCallback;



    public LoadResultJob(Params jobParams, final String requestID, final Callback.LoadResultCallback callback) {
        super(jobParams);
        request = requestID;
        resultCallback = callback;
        dataHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_MESSAGE_RESULT_READY) {
                    T result = (T) msg.obj;
                    callback.onSuccess(requestID, result);
                } else {
                    callback.onFail(requestID, C3PROErrorCode.JOB_HANDLER_ERROR);
                }
            }
        };
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        resultCallback.onFail(request, C3PROErrorCode.JOB_CANCELLED);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    protected void returnResult(T result) {
        Message msg = new Message();
        msg.what = HANDLER_MESSAGE_RESULT_READY;
        msg.obj = result;
        dataHandler.sendMessage(msg);
    }
}

