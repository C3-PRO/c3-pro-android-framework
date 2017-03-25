package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.content.Context;
import android.content.Intent;

import org.hl7.fhir.dstu3.model.Contract;

import ch.usz.c3pro.c3_pro_android_framework.consent.ViewConsentTaskActivity;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ConsentTaskOptions;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/15/2016.
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
 * This Async Task will create a {@link Intent} from a FHIR {@link Contract} that can be used to
 * present the Consent procedure to the user. This Async Task won't block the UI and can be used if the
 * Contract is expected to be large and will take some time to convert.
 * For normal size Contracts, the Intent can be obtained directly from the {@link ViewConsentTaskActivity}.
 * */
public class CreateIntentFromContractAsyncTask  extends LoadResultAsyncTask<Intent> {
    private Context context;
    private Contract contract;
    private ConsentTaskOptions options;

    public CreateIntentFromContractAsyncTask(Context context, Contract contract, ConsentTaskOptions options, String requestID, Callback.IntentReceiver intentCallback){
        super(requestID, intentCallback);
        this.context = context;
        this.contract = contract;
        this.options = options;
    }

    @Override
    protected Intent doInBackground(Void... params) {
        Intent intent = ViewConsentTaskActivity.newIntent(context, contract, options);
        return intent;
    }
}