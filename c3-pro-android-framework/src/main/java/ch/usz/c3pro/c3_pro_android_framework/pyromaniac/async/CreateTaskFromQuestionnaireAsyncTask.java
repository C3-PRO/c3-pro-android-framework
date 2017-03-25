package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.researchstack.backbone.task.Task;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;

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
 * This Async Task will create a {@link Task} from a FHIR {@link Questionnaire} that can be used with
 * a {@link ch.usz.c3pro.c3_pro_android_framework.questionnaire.ViewQuestionnaireTaskActivity}
 * */
public class CreateTaskFromQuestionnaireAsyncTask extends LoadResultAsyncTask<Task> {
    private Questionnaire questionnaire;

    public CreateTaskFromQuestionnaireAsyncTask(Questionnaire FHIRQuestionnaire, String requestID, Callback.TaskReceiver taskCallback){
        super(requestID, taskCallback);
        questionnaire = FHIRQuestionnaire;
    }

    @Override
    protected Task doInBackground(Void... params) {
        Task task = Pyro.getQuestionnaireAsTask(questionnaire);
        return (task);
    }
}
