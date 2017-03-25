package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.content.Context;

import org.hl7.fhir.dstu3.model.Contract;
import org.researchstack.backbone.task.Task;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ConsentTaskOptions;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/07/2016.
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
 * This Async Task will create a {@link Task} from a FHIR {@link Contract} that can be used with
 * a {@link ch.usz.c3pro.c3_pro_android_framework.consent.ViewConsentTaskActivity}
 * */
public class CreateTaskFromContractAsyncTask extends LoadResultAsyncTask<Task> {

    private Context context;
    private Contract contract;
    private ConsentTaskOptions options;

    public CreateTaskFromContractAsyncTask(Context context, Contract FHIRContract, ConsentTaskOptions consentOptions, String requestID, Callback.TaskReceiver taskCallback){
        super(requestID, taskCallback);
        this.context = context;
        contract = FHIRContract;
        options = consentOptions;
    }

    @Override
    protected Task doInBackground(Void... params) {
        Task task = Pyro.getContractAsTask(context, contract, options);
        return (task);
    }
}
