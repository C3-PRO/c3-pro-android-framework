package ch.usz.c3pro.c3_pro_android_framework.dataqueue;

import android.content.Context;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.CreateResourceJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.ReadQuestionnaireFromURLJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.ReadResourceJob;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;


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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This DataQueue will manage async jobs to upload and download data from the FHIRServer as well as
 * converting files between HAPI FHIR and ResearchStack. It is provided by the C3PRO class.
 * Set up initialize the C3PRO class in the onCreate method of your application and access the
 * Queue through it.
 */
public class DataQueue {
    public static String UPLOAD_GROUP_TAG = "FHIR_UPLOAD_GROUP";

    private static DataQueue instance = null;
    private JobManager jobManager;
    private String server;

    public static void init (Context context, String FHIRServerURL){
        instance = new DataQueue(FHIRServerURL, new JobManager(getDefaultBuilder(context).build()));
    }

    public static DataQueue getInstance(){
        if (instance == null){
            //error
        }
        return instance;
    }

    /**
     * The DataQueue needs the URL to a FHIR Server and a JobManager to run. A DataQueue is provided
     * as a singleton by the C3PRO class, no need to have another instance of it around!
     * */
    private DataQueue(String FHIRServerURL, JobManager manager) {
        jobManager = manager;
        server = FHIRServerURL;
    }

    /**
     * Creates the FHIR resource on the server provided at the setup of C3PRO.
     * */
    public void create(IBaseResource resource) {
        CreateResourceJob job = new CreateResourceJob(resource, server);
        jobManager.addJobInBackground(job);
    }

    /**
     * searchURL defines the search, can be absolute or relative to the FHIRServerURL defined in
     * the C3PRO, where the resource is loaded from. requestID will be passed back for
     * identification with the result to the resourceReceiver.
     * */
    public void read(String requestID, String searchURL, Callback.ReceiveBundleCallback callback) {
        ReadResourceJob job = new ReadResourceJob(requestID, searchURL, callback);
        jobManager.addJobInBackground(job);
    }

    /**
     * reads a Questionnaire from a json file at an URL
     * */
    public void getJsonQuestionnaireFromURL(String requestID, String url, Callback.QuestionnaireReceiver callback){
        ReadQuestionnaireFromURLJob job = new ReadQuestionnaireFromURLJob(requestID, url, callback);
        jobManager.addJobInBackground(job);
    }

    /**
     * The class Job can be subclassed to run custom jobs asynchronously through the DataQueue
     * */
    public void addJob(Job job) {
        jobManager.addJobInBackground(job);
    }

    /**
     * returns the URL which is setup with the C3PRO
     * */
    public String getFHIRServerURL() {
        return server;
    }

    private static Configuration.Builder getDefaultBuilder(Context context) {
        Configuration.Builder builder = new Configuration.Builder(context)
/**.customLogger(new CustomLogger() {
 private static final String TAG = "JOBMANAGER";

 @Override
 public boolean isDebugEnabled() {
 return true;
 }

 @Override
 public void d(String text, Object... args) {
 Log.d(TAG, String.format(text, args));
 }

 @Override
 public void e(Throwable t, String text, Object... args) {
 Log.e(TAG, String.format(text, args), t);
 }

 @Override
 public void e(String text, Object... args) {
 Log.e(TAG, String.format(text, args));
 }

 @Override
 public void v(String text, Object... args) {

 }
 })*/

                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120);//wait 2 minute
        return builder;
    }
}
