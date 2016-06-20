package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import ch.usz.c3pro.c3_pro_android_framework.C3PRO;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;

/**
 * C3PRO
 *
 * Created by manny Weber on 06/07/16.
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
 * This job can be used to asynchronously run a HAPI query. If no FHIRServerURL is provided, the
 * URL set in the dataqueue will be used. Jobs with the same singleInstanceID will only run if no
 * other job with same ID is present in the queue. Define a QueryPoster with the query in the
 * runQuery method.
 * Be aware that the query will run on a background thread. It won't be possible to access the UI
 * from it. Use a Handler to send the result to the main thread first.
 * */
public class HAPIQueryJob extends Job {
    private DataQueue.QueryPoster queryPoster;
    private String url;

    /**
     * The QueryPoster will get a generic HAPI client for the specified URL on which it can run its
     * query. If you add multiple jobs to the queue with the same singleINstanceID, only one will run.
     * */
    public HAPIQueryJob(String singleInstanceID, DataQueue.QueryPoster poster, String FHIRServerURL){
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(singleInstanceID));
        queryPoster = poster;
        url = FHIRServerURL;
    }

    /**
     * The QueryPoster will get a generic HAPI client for the URL specified in the C3PRO on
     * which it can run its query. If you add multiple jobs to the queue with the same
     * singleINstanceID, only one will run.
     * */
    public HAPIQueryJob(String singleInstanceID, DataQueue.QueryPoster poster){
        this(singleInstanceID, poster, C3PRO.getDataQueue().getFHIRServerURL());
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        queryPoster.runQuery(C3PRO.getFhirContext().newRestfulGenericClient(url));
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
