package ch.usz.c3pro.c3_pro_android_framework.questionnaire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.Serializable;

import ch.usz.c3pro.c3_pro_android_framework.C3PROErrorCode;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;

/**
 * C3PRO
 *
 * Created by manny Weber on 06/09/16.
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
 * The QuestionnaireFragment will represent and manage a FHIR Questionnaire. It will start a
 * ResearchStack ViewTaskActivity to ask the user for their answers, so the ViewTaskActivity is
 * declared in the library's AndroidManifest. Fragments need to be added to the FragmentManager of the
 * parent activity.
 */
public class QuestionnaireFragment extends Fragment {
    public static final String LTAG = "C3P";
    public static final int TASKVIEW_REQUEST_ID = 12345;

    private Questionnaire questionnaire;
    private Task mTask;
    private QuestionnaireFragmentListener fragmentListener;


    /**
     * The QuestionnaireFragmentListener is needed for the callbacks from the QuestionnaireFragment.
     * It will be notified when the Questionnaire has been converted to a Task and is ready to be
     * viewed. When the Questionnaire has been completed, the Listener will receive the answers in
     * a QuestionnaireResponse.
     */
    public interface QuestionnaireFragmentListener extends Serializable {
        void whenTaskReady(String requestID);

        void whenCompleted(String requestID, QuestionnaireResponse questionnaireResponse);

        void whenCancelledOrFailed(C3PROErrorCode code);
    }


    /**
     * TODO: proper instance hanling with bundle etc.
     */
    public void newInstance(Questionnaire FHIRQuestionnaire, QuestionnaireFragmentListener listener) {
        questionnaire = FHIRQuestionnaire;
        fragmentListener = listener;
    }

    public void prepareTaskViewActivity() {
        if (mTask == null) {
            Pyro.getQuestionnaireAsTaskAsync(questionnaire, questionnaire.getId(), new Callback.TaskCallback() {
                @Override
                public void onSuccess(String requestID, Task result) {
                    mTask = result;
                    fragmentListener.whenTaskReady(requestID);
                }

                @Override
                public void onFail(String requestID, C3PROErrorCode code) {
                    fragmentListener.whenCancelledOrFailed(code);
                }
            });
        } else {
            fragmentListener.whenTaskReady(questionnaire.getId());
        }
    }

    public void startTaskViewActivity() {
        if (getContext() == null) {
            fragmentListener.whenCancelledOrFailed(C3PROErrorCode.QUESTIONNAIRE_FRAGMENT_CONTEXT_NULL);
        } else if (mTask == null) {
            fragmentListener.whenCancelledOrFailed(C3PROErrorCode.QUESTIONNAIRE_FRAGMENT_TASK_NULL);
        } else {
            Intent intent = ViewTaskActivity.newIntent(getContext(), mTask);
            startActivityForResult(intent, TASKVIEW_REQUEST_ID);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TASKVIEW_REQUEST_ID) {
            switch (resultCode) {
                case AppCompatActivity.RESULT_OK:
                    final TaskResult taskResult = (TaskResult) data.getExtras().get(ViewTaskActivity.EXTRA_TASK_RESULT);

                    Pyro.getTaskResultAsQuestionnaireResponseAsync(taskResult, taskResult.getIdentifier(), new Callback.QuestionnaireResponseReceiver() {
                        @Override
                        public void onSuccess(String requestID, QuestionnaireResponse result) {
                            fragmentListener.whenCompleted(taskResult.getIdentifier(), result);
                        }

                        @Override
                        public void onFail(String requestID, C3PROErrorCode code) {
                            fragmentListener.whenCancelledOrFailed(code);
                        }
                    });

                    break;
                case AppCompatActivity.RESULT_CANCELED:
                    fragmentListener.whenCancelledOrFailed(C3PROErrorCode.RESULT_CANCELLED);
            }
        }
    }

    //fragment stuff
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    public String toString() {
        if (questionnaire != null) {
            return questionnaire.getId();
        } else {
            return "no questionnaire set";
        }
    }
}
