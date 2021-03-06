package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import org.researchstack.backbone.result.TaskResult;

import java.util.List;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ConditionalInstructionStep;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ResultRequirement;

/**
 * C3-PRO
 *
 * Created by manny Weber on 08/09/2016.
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
 * This {@link org.researchstack.backbone.step.Step} will determine the user's eligibility based on
 * The questions asked in a previous {@link org.researchstack.backbone.step.FormStep}
 * */
public class EligibilityAssessmentStep extends ConditionalInstructionStep {
    private boolean eligible = false;
    private String eligibleTitle;
    private String eligibleText;
    private String notEligibleTitle;
    private String notEligibleText;

    private List<ResultRequirement> requirements;

    public EligibilityAssessmentStep(String identifier, String titleEligible, String detailTextEligible, String titleNotEligible, String detailTextNotEligible, List<ResultRequirement> requirements) {
        super(identifier, "", "");
        eligibleTitle = titleEligible;
        eligibleText = detailTextEligible;
        notEligibleTitle = titleNotEligible;
        notEligibleText = detailTextNotEligible;
        this.requirements = requirements;
        setOptional(false);
    }

    public void setRequirements (List<ResultRequirement> requirements){
        this.requirements = requirements;
    }

    @Override
    public String getText(){
        if(eligible){
            return eligibleText;
        }else{
            return notEligibleText;
        }
    }


    @Override
    public Class getStepLayoutClass()
    {
        return EligibilityAssessmentStepLayout.class;
    }

    @Override
    public boolean requirementsAreSatisfiedBy(TaskResult result) {
        boolean isEligible = true;
        for (ResultRequirement req : requirements){
            if (req.isSatisfiedBy(result) == false){
                isEligible = false;
                break;
            }
        }
        eligible = isEligible;
        return true;
    }

    public boolean isEligible(){
        return eligible;
    }

    public String getEligibleTitle() {
        return eligibleTitle;
    }

    public String getEligibleText() {
        return eligibleText;
    }

    public String getNotEligibleTitle() {
        return notEligibleTitle;
    }

    public String getNotEligibleText() {
        return notEligibleText;
    }
}
