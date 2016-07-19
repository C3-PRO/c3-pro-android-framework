package ch.usz.c3pro.c3_pro_android_framework;

import android.content.Context;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;

import ca.uhn.fhir.context.FhirContext;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;


/**
 * C3PRO
 *
 * Created by manny Weber on 06/08/16.
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
 *  * This class will provide the FHIR Context and, if you initialize it with a FHIR Server URL, a
 *  dataqueue as a singleton. For the data to outlive activity lifecycles, it should be initialized
 *  in your application onCreate.
 *  The FHIR Context is a costly object and you should only keep one instance around.
 * */
public class C3PRO {
    private static FhirContext fhirContext;
    private static JobManager jobManager;
    private static DataQueue dataQueue;



    private C3PRO() {
    }

    public static void init(Context context, String FHIRServerURL) {
        initFhirContext();
        initJobManager(context);
        initDataQueue(FHIRServerURL);
    }

    public static void init(Context context) {
        initFhirContext();
        initJobManager(context);
    }

    public static void initFhirContext() {
        if (fhirContext == null) {
            fhirContext = FhirContext.forDstu3();
        }
    }

    public static void initJobManager(Context context) {
        if (jobManager == null) {
            jobManager = new JobManager(getDefaultBuilder(context).build());
        }
    }

    public static void initDataQueue(String FHIRServerURL){
        if (dataQueue == null){
            dataQueue = new DataQueue(FHIRServerURL, getJobManager());
        }
    }

    public static FhirContext getFhirContext() {
        return fhirContext;
    }

    public static JobManager getJobManager() {
        return jobManager;
    }

    public static DataQueue getDataQueue(){
        return dataQueue;
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
