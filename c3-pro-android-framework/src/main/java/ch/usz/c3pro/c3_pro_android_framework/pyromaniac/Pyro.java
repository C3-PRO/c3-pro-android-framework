package ch.usz.c3pro.c3_pro_android_framework.pyromaniac;

import android.content.Context;
import android.content.Intent;

import org.hl7.fhir.dstu3.model.Contract;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.CreateQuestionnaireResponseFromTaskResultAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.CreateTaskFromContractAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.CreateTaskFromQuestionnaireAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ConsentTaskOptions;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ContractAsTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ConditionalOrderedTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.QuestionnaireItemAsStep;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.TaskResultAsQuestionnaireResponse;

/**
 * C3-PRO
 * <p>
 * Created by manny Weber on 08/02/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Pyro {

    private static FhirContext fhirContext;

    public static FhirContext getFhirContext(){
        if (fhirContext == null){
            fhirContext = FhirContext.forDstu3();
        }
        return fhirContext;
    }

    /////////////////////////////////////////////
    // Questionnaire and QuestionnaireResponse //
    /////////////////////////////////////////////

    /**
     * Returns a ResearchStack {@link org.researchstack.backbone.task.Task} that can be viewed by a
     * {@link org.researchstack.backbone.ui.ViewTaskActivity} based on a FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire}.
     * If the items have {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent}s, the returned
     * {@link Task} will be a {@link ConditionalOrderedTask}.
     *
     * @param questionnaire a HAPI FHIR Questionnaire Resource
     * @return              a ResearchStack Task
     */
    public static Task getQuestionnaireAsTask (Questionnaire questionnaire){

        List<Questionnaire.QuestionnaireItemComponent> items = questionnaire.getItem();
        String identifier = questionnaire.getId();

        List<Step> steps = QuestionnaireItemAsStep.getQuestionnaireItemsAsSteps(items);

        return new ConditionalOrderedTask(identifier, steps);
    }

    public static void getQuestionnaireAsTaskAsync(Questionnaire questionnaire, String requestID, Callback.TaskReceiver questionnaireReceiver){
        new CreateTaskFromQuestionnaireAsyncTask(questionnaire, requestID, questionnaireReceiver).execute();
    }


    /**
     * Returns a FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} based on the passed
     * ResearchStack {@link org.researchstack.backbone.result.TaskResult}
     *
     * @param taskResult The taskResult of the conducted survey produced by a {@link org.researchstack.backbone.ui.ViewTaskActivity}
     * @return A FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} that contains all the
     * question IDs with corresponding answers given by the user
     */
    public static QuestionnaireResponse getTaskResultAsQuestionnaireResponse(TaskResult taskResult) {
        return TaskResultAsQuestionnaireResponse.getTaskResultAsQuestionnaireResponse(taskResult);
    }

    public static void getTaskResultAsQuestionnaireResponseAsync(TaskResult taskResult, String requestID, Callback.QuestionnaireResponseReceiver responseReceiver){
        new CreateQuestionnaireResponseFromTaskResultAsyncTask(taskResult, requestID, responseReceiver).execute();
    }

    /**
     * Returns a {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} based on the
     * ResearchStack {@link org.researchstack.backbone.result.StepResult} within the passed
     * {@link Intent}. This is the Intent returned in the onActivityResult() method
     * of the parent activity of the {@link org.researchstack.backbone.ui.ViewTaskActivity}
     *
     * @param data The {@link Intent} returned by the {@link org.researchstack.backbone.ui.ViewTaskActivity}
     * @return A {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} containing the answers given by the user
     */
    public static QuestionnaireResponse getResultIntentAsQuestionnaireResponse(Intent data) {

        if (data != null) {
            TaskResult taskResult = (TaskResult) data.getExtras().get(ViewTaskActivity.EXTRA_TASK_RESULT);
            if (taskResult != null) {
                return TaskResultAsQuestionnaireResponse.getTaskResultAsQuestionnaireResponse(taskResult);
            }
        }
        return null;
    }

    /////////////////////////////
    // Consent and Eligibility //
    /////////////////////////////

    public static Task getContractAsTask(Context context, Contract contract, ConsentTaskOptions options){
        return ContractAsTask.getContractAsTask(context, contract, options);
    }

    public static void getContractAsTaskAsync (Context context, Contract contract, ConsentTaskOptions options, String requestID, Callback.TaskReceiver taskReceiver){
        new CreateTaskFromContractAsyncTask(context, contract, options, requestID, taskReceiver).execute();
    }
}
