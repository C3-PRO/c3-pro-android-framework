package ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs;

import com.birbit.android.jobqueue.Params;

import org.hl7.fhir.dstu3.model.Bundle;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;

/**
 * C3-PRO
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
 * This job is used by the DataQueue to asynchronously read a resource from the FHIRServer.
 * The Handler is used to transfer the resource to the main (UI) thread, so it could be used to
 * update UI elements.
 */
public class ReadResourceJob extends LoadResultJob<Bundle> {
    private String search;
    private String url;

    /**
     * searchURL defines the search, can be absolute or relative to the FHIRServerURL, where the resource is
     * loaded from. requestID will be passed back for identification with the result to the resourceReceiver.
     * */
    public ReadResourceJob(final String requestID, String searchURL, Callback.LoadResultCallback callback, String FHIRServerURL){
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(requestID), requestID, callback);
        search = searchURL;
        url = FHIRServerURL;
    }

    /**
     * searchURL defines the search, can be absolute or relative to the FHIRServerURL defined in
     * the C3PRO, where the resource is loaded from. requestID will be passed back for
     * identification with the result to the resourceReceiver.
     * */
    public ReadResourceJob(String requestID, String searchURL, Callback.LoadResultCallback callback){
        this(requestID, searchURL, callback, DataQueue.getInstance().getFHIRServerURL());
    }


    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Bundle response = Pyro.getFhirContext().newRestfulGenericClient(url).search()
                .byUrl(search)
                .returnBundle(Bundle.class)
                .execute();
        returnResult(response);
    }
}
