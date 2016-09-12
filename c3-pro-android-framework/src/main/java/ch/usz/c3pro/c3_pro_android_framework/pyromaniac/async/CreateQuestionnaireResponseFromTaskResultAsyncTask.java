package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async;

import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.researchstack.backbone.result.TaskResult;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/02/2016.
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
 * This Async Task will create a FHIR {@link QuestionnaireResponse} from a ResearchStack
 * {@link TaskResult} retrieved from a {@link ch.usz.c3pro.c3_pro_android_framework.questionnaire.ViewQuestionnaireTaskActivity}
 * */
public class CreateQuestionnaireResponseFromTaskResultAsyncTask extends LoadResultAsyncTask<QuestionnaireResponse>{
    private TaskResult result;

    public CreateQuestionnaireResponseFromTaskResultAsyncTask(TaskResult taskResult, String requestID, Callback.QuestionnaireResponseReceiver responseCallback){
        super(requestID, responseCallback);
        result = taskResult;
    }

    @Override
    protected QuestionnaireResponse doInBackground(Void... params) {
        QuestionnaireResponse response = Pyro.getTaskResultAsQuestionnaireResponse(result);
        return response;
    }
}
