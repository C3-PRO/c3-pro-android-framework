package ch.usz.c3pro.c3_pro_android_framework.questionnaire;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.io.Serializable;

import ch.usz.c3pro.c3_pro_android_framework.C3PRO;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;
import ch.usz.c3pro.c3_pro_android_framework.questionnaire.jobs.PrepareTaskJob;
import ch.usz.c3pro.c3_pro_android_framework.questionnaire.jobs.QuestionnaireResponseJob;

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
    private QuestionnaireFragmentListener mCallback;


    /**
     * The QuestionnaireFragmentListener is needed for the callbacks from the QuestionnaireFragment.
     * It will be notified when the Questionnaire has been converted to a Task and is ready to be
     * viewed. When the Questionnaire has been completed, the Listener will receive the answers in
     * a QuestionnaireResponse.
     */
    public interface QuestionnaireFragmentListener extends Serializable {
        public abstract void whenTaskReady(String requestID);

        public abstract void whenCompleted(String requestID, QuestionnaireResponse questionnaireResponse);

        public abstract void whenCancelledOrFailed();
    }


    /**
     * TODO: proper instance hanling with bundle etc.
     */
    public void newInstance(Questionnaire FHIRQuestionnaire, QuestionnaireFragmentListener listener) {
        questionnaire = FHIRQuestionnaire;
        mCallback = listener;
    }

    public void prepareTaskViewActivity() {
        if (mTask == null) {
            PrepareTaskJob job = new PrepareTaskJob(questionnaire, questionnaire.getId(), new DataQueue.TaskReceiver() {
                @Override
                public void receiveTask(String requestID, Task task) {
                    mTask = task;
                    mCallback.whenTaskReady(requestID);
                }
            });
            C3PRO.getJobManager().addJobInBackground(job);
        } else {
            mCallback.whenTaskReady(questionnaire.getId());
        }
    }

    public void startTaskViewActivity() {
        //TODO error handling when task not prepared yet
        if (getContext() == null) {
            mCallback.whenCancelledOrFailed();
            Log.e(LTAG, "context null in qFragment");
        } else if (mTask == null) {
            mCallback.whenCancelledOrFailed();
            Log.d(LTAG, "no Task prepared yet");
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
                    QuestionnaireResponseJob job = new QuestionnaireResponseJob(taskResult, taskResult.getIdentifier(), new DataQueue.QuestionnaireResponseReceiver() {
                        @Override
                        public void receiveResponse(String requestID, QuestionnaireResponse questionnaireResponse) {
                            mCallback.whenCompleted(taskResult.getIdentifier(), questionnaireResponse);
                        }
                    });
                    C3PRO.getJobManager().addJobInBackground(job);
                    break;
                case AppCompatActivity.RESULT_CANCELED:
                    mCallback.whenCancelledOrFailed();
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
