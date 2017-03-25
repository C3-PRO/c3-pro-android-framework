package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.content.Intent;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.researchstack.backbone.task.Task;

import ca.uhn.fhir.rest.client.IGenericClient;
import ch.usz.c3pro.c3_pro_android_framework.errors.C3PROErrorCode;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/02/2016.
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
 * These Callbacks are used to pass back objects loaded in a background task
 * */
public class Callback {

    public interface LoadResultCallback<T> {
        void onSuccess(String requestID, T result);

        void onFail(String requestID, C3PROErrorCode code);
    }

    public interface TaskReceiver extends LoadResultCallback<Task> {}

    public interface QuestionnaireResponseReceiver extends LoadResultCallback<QuestionnaireResponse>{}

    public interface QuantityReceiver extends LoadResultCallback<Quantity>{}

    public interface ObservationReceiver extends LoadResultCallback<Observation>{}

    public interface QuestionnaireReceiver extends LoadResultCallback<Questionnaire>{}

    public interface IntentReceiver extends LoadResultCallback<Intent>{}


    /////////////////////////
    // DataQueue Callbacks //
    /////////////////////////


    /**
     * The ReceiveBundleCallback interface is used to pass back downloaded resources in a FHIR Bundle.
     * */
    public interface ReceiveBundleCallback extends LoadResultCallback<Bundle>{
    }

    /**
     * The UploadCallback is used to inform the caller if an upload of a resource could not be completed.
     * */
    public interface UploadCallback {
        void onFail (IBaseResource resource, C3PROErrorCode code);
    }

    /**
     * Interface needed for a HAPIQueryJob. Implement the runQuery method and run a HAPI Query on the
     * provided client.
     */
    public interface QueryPoster {
        void runQuery(IGenericClient client);
    }
}