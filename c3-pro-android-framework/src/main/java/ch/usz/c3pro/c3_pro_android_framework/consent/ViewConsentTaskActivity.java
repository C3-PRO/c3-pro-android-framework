package ch.usz.c3pro.c3_pro_android_framework.consent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.hl7.fhir.dstu3.model.Contract;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.ConsentDocumentStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.PinCodeActivity;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.ui.views.StepSwitcher;
import org.researchstack.skin.task.ConsentTask;

import java.lang.reflect.Constructor;
import java.util.Date;

import ch.usz.c3pro.c3_pro_android_framework.R;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.Pyro;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.Callback;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.async.CreateIntentFromContractAsyncTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ConsentTaskOptions;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.ContractAsTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.CreateConsentPDF;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent.EligibilityAssessmentStep;

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
 * The ViewConsentTaskActivity has the same functionality as the ResearchStack ViewTaskActivity but
 * it can be created directly from a FHIR / C3-PRO Contract. (The TaskViewActivity was not subclassed
 * because some of the necessary functions are declared private)
 * */
public class ViewConsentTaskActivity extends PinCodeActivity implements StepCallbacks {
    public static final String EXTRA_TASK = "ViewConsentTaskActivity.ExtraTask";
    public static final String EXTRA_TASK_RESULT = "ViewConsentTaskActivity.ExtraTaskResult";
    public static final String EXTRA_STEP = "ViewConsentTaskActivity.ExtraStep";

    private StepSwitcher root;

    private Step currentStep;
    private Task task;
    private TaskResult taskResult;

    /**
     * Get an {@link android.content.Intent} based on a FHIR {@link org.hl7.fhir.dstu3.model.Contract}
     * with standard {@link ConsentTaskOptions} which can be started for a Result.
     * */
    public static Intent newIntent(Context context, Contract contract) {
        return newIntent(context, contract, new ConsentTaskOptions());
    }

    /**
     * Get an {@link android.content.Intent} based on a FHIR {@link org.hl7.fhir.dstu3.model.Contract}
     * with provided {@link ConsentTaskOptions} which can be started for a Result.
     * */
    public static Intent newIntent(Context context, Contract contract, ConsentTaskOptions options) {
        Task task = Pyro.getContractAsTask(context, contract, options);
        return newIntent(context, task);
    }

    /**
     * Get an {@link android.content.Intent} based on a FHIR {@link org.hl7.fhir.dstu3.model.Contract}
     * with provided {@link ConsentTaskOptions} which can be started for a Result. The result can be
     * read from the Extras.
     * The Intent will be created in a background task and will be returned to the IntentReceiver
     * when ready.
     * */
    public static void newIntent(Context context, Contract contract, ConsentTaskOptions options, String requestID, Callback.IntentReceiver intentReceiver){
        new CreateIntentFromContractAsyncTask(context, contract, options, requestID, intentReceiver).execute();
    }

    private static Intent newIntent(Context context, Task task) {
        Intent intent = new Intent(context, ViewConsentTaskActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setResult(RESULT_CANCELED);
        super.setContentView(R.layout.rsb_activity_step_switcher);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        root = (StepSwitcher) findViewById(R.id.container);

        if (savedInstanceState == null) {
            task = (Task) getIntent().getSerializableExtra(EXTRA_TASK);
            taskResult = new TaskResult(task.getIdentifier());
            taskResult.setStartDate(new Date());
        } else {
            task = (Task) savedInstanceState.getSerializable(EXTRA_TASK);
            taskResult = (TaskResult) savedInstanceState.getSerializable(EXTRA_TASK_RESULT);
            currentStep = (Step) savedInstanceState.getSerializable(EXTRA_STEP);
        }

        task.validateParameters();
    }

    protected Step getCurrentStep() {
        return currentStep;
    }

    protected void showNextStep() {
        Step nextStep = task.getStepAfterStep(currentStep, taskResult);
        if (nextStep == null) {
            saveAndFinish();
        } else {
            showStep(nextStep);
        }
    }

    protected void showPreviousStep() {
        Step previousStep = task.getStepBeforeStep(currentStep, taskResult);
        if (previousStep == null) {
            finish();
        } else {
            showStep(previousStep);
        }
    }

    private void showStep(Step step) {
        int currentStepPosition = task.getProgressOfCurrentStep(currentStep, taskResult)
                .getCurrent();
        int newStepPosition = task.getProgressOfCurrentStep(step, taskResult).getCurrent();

        StepLayout stepLayout = getLayoutForStep(step);
        stepLayout.getLayout().setTag(R.id.rsb_step_layout_id, step.getIdentifier());
        root.show(stepLayout,
                newStepPosition >= currentStepPosition
                        ? StepSwitcher.SHIFT_LEFT
                        : StepSwitcher.SHIFT_RIGHT);
        currentStep = step;
    }

    protected StepLayout getLayoutForStep(Step step) {
        // Change the title on the activity
        String title = task.getTitleForStep(this, step);
        setActionBarTitle(title);

        // Get result from the TaskResult, can be null
        StepResult result = taskResult.getStepResult(step.getIdentifier());

        // Return the Class & constructor
        StepLayout stepLayout = createLayoutFromStep(step);
        stepLayout.initialize(step, result);
        stepLayout.setCallbacks(this);

        return stepLayout;
    }

    @NonNull
    private StepLayout createLayoutFromStep(Step step) {
        try {
            Class cls = step.getStepLayoutClass();
            Constructor constructor = cls.getConstructor(Context.class);
            return (StepLayout) constructor.newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveAndFinish() {
        Intent resultIntent = new Intent();

        /*ConsentDocumentStep consentDocumentStep = (ConsentDocumentStep) task.getStepWithIdentifier(ContractAsTask.ID_CONSENT_STEP);
        if (consentDocumentStep != null) {
            // get consent doc content
            String consentDoc = consentDocumentStep.getConsentHTML();

            // get signature as bitmap
            String signatureEncodeBase64 = (String) taskResult.getStepResult(ConsentTask.ID_SIGNATURE).getResultForIdentifier("ConsentSignatureStep.Signature");
            byte[] decodedString = Base64.decode(signatureEncodeBase64, Base64.DEFAULT);
            Bitmap signatureBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // create pdf
            CreateConsentPDF.createPDFfromHTML(getApplicationContext(), consentDoc, signatureBitmap, Environment.getExternalStorageDirectory() + "/consent.pdf");


            // add as extra
            //resultIntent.putExtra(EXTRA_CONSENT_PDF_PATH, pathToPDF);
        }*/


        taskResult.setEndDate(new Date());
        resultIntent.putExtra(EXTRA_TASK_RESULT, taskResult);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            notifyStepOfBackPress();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        notifyStepOfBackPress();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_TASK, task);
        outState.putSerializable(EXTRA_TASK_RESULT, taskResult);
        outState.putSerializable(EXTRA_STEP, currentStep);
    }

    private void notifyStepOfBackPress() {
        StepLayout currentStepLayout = (StepLayout) findViewById(R.id.rsb_current_step);
        currentStepLayout.isBackEventConsumed();
    }

    @Override
    public void onDataReady() {
        super.onDataReady();

        if (currentStep == null) {
            currentStep = task.getStepAfterStep(null, taskResult);
        }

        showStep(currentStep);
    }

    @Override
    public void onDataFailed() {
        super.onDataFailed();
        Toast.makeText(this, R.string.rsb_error_data_failed, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onSaveStep(int action, Step step, StepResult result) {
        if (step instanceof EligibilityAssessmentStep && action == StepCallbacks.ACTION_END) {
            finish();
        } else {

            onSaveStepResult(step.getIdentifier(), result);

            onExecuteStepAction(action);
        }
    }

    protected void onSaveStepResult(String id, StepResult result) {
        taskResult.setStepResultForStepIdentifier(id, result);
    }

    protected void onExecuteStepAction(int action) {
        if (action == StepCallbacks.ACTION_NEXT) {
            showNextStep();
        } else if (action == StepCallbacks.ACTION_PREV) {
            showPreviousStep();
        } else if (action == StepCallbacks.ACTION_END) {
            showConfirmExitDialog();
        } else if (action == StepCallbacks.ACTION_NONE) {
            // Used when onSaveInstanceState is called of a view. No action is taken.
        } else {
            throw new IllegalArgumentException("Action with value " + action + " is invalid. " +
                    "See StepCallbacks for allowable arguments");
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isActive() && imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    private void showConfirmExitDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(
                "Are you sure you want to exit?")
                .setMessage(R.string.lorem_medium)
                .setPositiveButton("End Task", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    @Override
    public void onCancelStep() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}
