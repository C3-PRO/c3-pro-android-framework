package ch.usz.c3pro.c3_pro_android_framework.questionnaire.jobs;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.researchstack.backbone.task.Task;

import ch.usz.c3pro.c3_pro_android_framework.dataqueue.DataQueue;
import ch.usz.c3pro.c3_pro_android_framework.dataqueue.jobs.Priority;
import ch.usz.c3pro.c3_pro_android_framework.questionnaire.logic.Questionnaire2Task;

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
 * This job is used by the DataQueue to convert a FHIR questionnaire to a ResearchStack Task in a
 * background thread. The handler will move the result to the main (UI) thread, so it can be used
 * to update the UI.
 */
public class PrepareTaskJob extends Job {
    private static int HANDLER_MESSAGE_TASK_READY = 3;
    private Questionnaire questionnaire;
    private Handler dataHandler;

    /**
     * The FHIR questionnaire provided will be converted to a ResearchStack Task in a background
     * thread and passed back to the taskReceiver when done.
     * */
    public PrepareTaskJob(Questionnaire FHIRQuestionnaire, final  String requestID, final DataQueue.TaskReceiver taskReceiver) {
        super(new Params(Priority.HIGH));
        questionnaire = FHIRQuestionnaire;
        dataHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_MESSAGE_TASK_READY) {
                    Task task = (Task) msg.obj;
                    taskReceiver.receiveTask(requestID, task);
                } else {
                    //TODO error handling
                }
            }
        };
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Task task = Questionnaire2Task.questionnaire2Task(questionnaire);
        Message msg = new Message();
        msg.what = HANDLER_MESSAGE_TASK_READY;
        msg.obj = task;
        dataHandler.sendMessage(msg);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
