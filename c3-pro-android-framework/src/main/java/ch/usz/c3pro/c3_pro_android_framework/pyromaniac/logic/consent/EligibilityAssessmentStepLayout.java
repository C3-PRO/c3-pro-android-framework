package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.ViewWebDocumentActivity;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.InstructionStepLayout;
import org.researchstack.backbone.ui.step.layout.TextViewLinkHandler;
import org.researchstack.backbone.ui.views.SubmitBar;
import org.researchstack.backbone.utils.TextUtils;

import ch.usz.c3pro.c3_pro_android_framework.R;
import rx.functions.Action1;

/**
 * C3-PRO
 * <p/>
 * Created by manny Weber on 08/09/2016.
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
 * Layout Class for the {@link EligibilityAssessmentStep} in order to change next / done button depending
 * on eligibility and to save eligibility information as StepResult.
 * */
public class EligibilityAssessmentStepLayout extends InstructionStepLayout {
    private StepCallbacks callbacks;
    private Step step;
    private boolean eligible;

    public EligibilityAssessmentStepLayout(Context context) {
        super(context);
    }

    public EligibilityAssessmentStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EligibilityAssessmentStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        this.step = step;
        initializeStep();
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks)
    {
        super.setCallbacks(callbacks);
        this.callbacks = callbacks;
    }

    @Override
    public boolean isBackEventConsumed() {

        /**
         * saves the eligibility in the StepResult when user presses back button or ends the activity.
         * */
        StepResult<Boolean> eligibilityResult = new StepResult<Boolean>(step);
        eligibilityResult.setResult(new Boolean(((EligibilityAssessmentStep) step).isEligible()));

        if(((EligibilityAssessmentStep)step).isEligible()){
            callbacks.onSaveStep(StepCallbacks.ACTION_PREV, step, eligibilityResult);
            return false;
        }
        callbacks.onSaveStep(StepCallbacks.ACTION_END, step, eligibilityResult);
        return true;
    }

    /**
     * Changes the button to "next" or "done" depending on eligibility. Saves the eligibility as a
     * Boolean in the StepResult.
     * */
    private void initializeStep() {
        if (step != null) {

            // StepResult containing eligibility to add when saving step
            final StepResult<Boolean> eligibilityResult = new StepResult<Boolean>(step);
            eligibilityResult.setResult(new Boolean(((EligibilityAssessmentStep) step).isEligible()));

            // Set Title
            if (!TextUtils.isEmpty(step.getTitle())) {
                TextView title = (TextView) findViewById(R.id.rsb_intruction_title);
                title.setVisibility(View.VISIBLE);
                title.setText(step.getTitle());
            }

            // Set Summary
            if (!TextUtils.isEmpty(step.getText())) {
                TextView summary = (TextView) findViewById(R.id.rsb_intruction_text);
                summary.setVisibility(View.VISIBLE);
                summary.setText(Html.fromHtml(step.getText()));
                summary.setMovementMethod(new TextViewLinkHandler() {
                    @Override
                    public void onLinkClick(String url) {
                        String path = ResourcePathManager.getInstance().
                                generateAbsolutePath(ResourcePathManager.Resource.TYPE_HTML, url);
                        Intent intent = ViewWebDocumentActivity.newIntentForPath(getContext(),
                                step.getTitle(),
                                path);
                        getContext().startActivity(intent);
                    }
                });
            }

            SubmitBar submitBar = (SubmitBar) findViewById(R.id.rsb_submit_bar);
            if (((EligibilityAssessmentStep) step).isEligible()) {
                submitBar.setPositiveTitle(R.string.rsb_next);
                submitBar.setPositiveAction(new Action1() {
                    @Override
                    public void call(Object o) {
                        callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, eligibilityResult);
                    }
                });
            } else {
                // Set Next / Skip
                submitBar.setPositiveTitle(R.string.rsb_done);

                submitBar.setPositiveAction(new Action1() {
                    @Override
                    public void call(Object o) {

                        //TODO add stepresult????


                        callbacks.onSaveStep(StepCallbacks.ACTION_END, step, eligibilityResult);
                    }
                });
            }

            submitBar.getNegativeActionView().setVisibility(View.GONE);
        }
    }
}
