package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import android.content.Context;
import android.content.Intent;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.researchstack.backbone.task.Task;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.questionnaire.ViewQuestionnaireTaskActivity;

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
 * This Async Task will create a {@link Intent} from a FHIR {@link Questionnaire} that can be used to
 * present the Questionnaire to the user. This Async Task won't block the UI and can be used if the
 * Questionnaire is expected to be large and will take some time to convert.
 * For normal size Questionnaires, the Intent can be obtained directly from the {@link ViewQuestionnaireTaskActivity}.
 * */
public class CreateIntentFromQuestionnaireAsyncTask extends LoadResultAsyncTask<Intent> {
    private Context context;
    private Questionnaire questionnaire;

    public CreateIntentFromQuestionnaireAsyncTask(Context context, Questionnaire FHIRQuestionnaire, String requestID, Callback.IntentReceiver intentCallback){
        super(requestID, intentCallback);
        this.context = context;
        questionnaire = FHIRQuestionnaire;
    }

    @Override
    protected Intent doInBackground(Void... params) {
        Task task = Pyro.getQuestionnaireAsTask(questionnaire);
        Intent intent = new Intent(context, ViewQuestionnaireTaskActivity.class);
        intent.putExtra(ViewQuestionnaireTaskActivity.EXTRA_TASK, task);
        return intent;
    }
}