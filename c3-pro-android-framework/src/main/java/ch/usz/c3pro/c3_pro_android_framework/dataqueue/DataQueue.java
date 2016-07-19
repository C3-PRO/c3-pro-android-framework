package ch.usz.c3pro.c3_pro_android_framework.dataqueue;

import android.content.res.Resources;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.researchstack.backbone.task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import ch.usz.c3pro.c3_pro_android_framework.C3PRO;
import ch.usz.c3pro.c3_pro_android_framework.C3PROErrorCode;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.CreateResourceJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.LoadResultJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.ReadQuestionnaireFromURLJob;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.ReadResourceJob;


/**
 * C3PRO
 * <p/>
 * Created by manny Weber on 06/07/16.
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
 * This DataQueue will manage async jobs to upload and download data from the FHIRServer as well as
 * converting files between HAPI FHIR and ResearchStack. It is provided by the C3PRO class.
 * Set up initialize the C3PRO class in the onCreate method of your application and access the
 * Queue through it.
 */
public class DataQueue {
    public static String UPLOAD_GROUP_TAG = "FHIR_UPLOAD_GROUP";

    private JobManager jobManager;
    private String server;

    /**
     * The ReceiveBundleCallback interface is used to pass back downloaded resources in a FHIR Bundle.
     * */
    public interface ReceiveBundleCallback extends LoadResultJob.LoadResultCallback<Bundle>{
    }

    /**
     * The CreateTaskCallback interface is used to pass back Tasks that were created from Questionnaires.
     * */
    public interface CreateTaskCallback extends LoadResultJob.LoadResultCallback<Task> {
    }

    /**
     * The CreateQuestionnaireCallback interface is used to pass back Questionnaires.
     * */
    public interface CreateQuestionnaireCallback extends LoadResultJob.LoadResultCallback<Questionnaire> {
    }

    /**
     * The CreateTaskCallback interface is used to pass back Tasks that were created from Questionnaires.
     * */
    public interface CreateQuestionnaireResponseCallback extends LoadResultJob.LoadResultCallback<QuestionnaireResponse> {
    }


    /**
     * Interface needed for a HAPIQueryJob. Implement the runQuery method and run a HAPI Query on the
     * provided client.
     */
    public interface QueryPoster {
        void runQuery(IGenericClient client);
    }

    /**
     * The DataQueue needs the URL to a FHIR Server and a JobManager to run. A DataQueue is provided
     * as a singleton by the C3PRO class, no need to have another instance of it around!
     * */
    public DataQueue(String FHIRServerURL, JobManager manager) {
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
    public void read(String requestID, String searchURL, ReceiveBundleCallback callback) {
        ReadResourceJob job = new ReadResourceJob(requestID, searchURL, callback);
        jobManager.addJobInBackground(job);
    }

    /**
     * reads a Questionnaire from a json file at an URL
     * */
    public void getJsonQuestionnaireFromURL(String requestID, String url, CreateQuestionnaireCallback callback){
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


    /**
     * loads the content of the file corresponding to the rawID into a string.
     * */
    public static String getRawFileAsString(Resources res, int rawID) {

        //InputStream is = res.openRawResource(R.raw.questionnaire_textvalues);
        InputStream is = res.openRawResource(rawID);

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writer.toString();
    }

    /**
     * returns a Questionnaire from the jason with corresponding to the rawID from the "raw" resource
     * folder.
     * */
    public static Questionnaire getQuestionnaireFromRawJson(Resources res, int rawID) {

        IParser parser = C3PRO.getFhirContext().newJsonParser();

        String json = getRawFileAsString(res, rawID);

        return parser.parseResource(Questionnaire.class, json);

    }
}
