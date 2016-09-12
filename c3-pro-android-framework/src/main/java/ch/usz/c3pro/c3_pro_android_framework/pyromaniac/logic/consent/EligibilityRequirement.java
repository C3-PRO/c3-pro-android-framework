package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Type;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;

import java.util.Map;

import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ResultRequirement;

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
 * The EligibilityRequirement works with the questions in the FormStep of the eligibility assessment
 * from a FHIR / C3-PRO contract.
 * */
public class EligibilityRequirement extends ResultRequirement {

    /**
     * Constructor.
     * Returns an initialized ResultRequirement for the questionID with of the question to be checked
     * and the answer to that question for the Requirement to be met
     *
     * @param questionID       LinkId of the question to be checked
     * @param enableWhenAnswer Required answer to the question for the Requirement to be met.
     */
    public EligibilityRequirement(String questionID, Type enableWhenAnswer) {
        super(questionID, enableWhenAnswer);
    }

    @Override
    public boolean isSatisfiedBy(TaskResult result) {
        StepResult resultAnswer = result.getStepResult(ContractAsTask.ID_ELIGIBILITY_FORM_STEP);
        if (resultAnswer != null) {
            Map results = resultAnswer.getResults();

            if (results.containsKey(questionIdentifier)) {

                StepResult stepResult = (StepResult) results.get(questionIdentifier);

                if (stepResult != null) {
                    BooleanType reqBoolType = (BooleanType) reqAnswer;
                    if (reqBoolType != null) {
                        boolean reqBool = reqBoolType.booleanValue();
                        Boolean ansBool = (Boolean) stepResult.getResult();
                        if (ansBool != null) {
                            return reqBool == ansBool;
                        }
                    }
                }
            }
        }
        return false;
    }
}
