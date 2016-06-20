package ch.usz.c3pro.c3_pro_android_framework.questionnaire.logic;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.QuestionStep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * C3PRO
 *
 * Created by manny Weber on 05/18/16.
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
 * This Class extends the ResearchStack {@link org.researchstack.backbone.step.QuestionStep} and implements the C3PRO
 * {@link ConditionalStep}.
 * {@link ConditionalStep}s provide the logic to add Steps to a {@link ConditionalOrderedTask} that
 * are only shown to the user when certain conditions are met.
 * The logic is derived from the FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} element.
 * {@link ResultRequirement}s can be added to the step and will be verified every time before
 * displaying the step to the user.
 */
public class ConditionalQuestionStep extends QuestionStep implements ConditionalStep, Serializable {

    private List<ResultRequirement> requirements;

    /**
     * The parent class {@link org.researchstack.backbone.step.QuestionStep} has no default constructor, so we have to provide one.
     * This constructor should not be used.
     */
    private ConditionalQuestionStep() {
        super("");
    }

    /**
     * Constructor.
     * Returns an initialized ConditionalQuestionStep.
     *
     * @param identifier    ID of the QuestionStep. Should be identical to the LinkId of the
     *                      corresponding FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent}
     * @param title         Question text that is displayed to the user.
     * @param format        The {@link org.researchstack.backbone.answerformat.AnswerFormat} used to get the user input.
     */
    public ConditionalQuestionStep(String identifier, String title, AnswerFormat format) {
        super(identifier, title, format);
    }

    /**
     * Adds a {@link ResultRequirement} which has to be met in order for the {@link ConditionalStep}
     * to be displayed to the user.
     * */
    @Override
    public void addRequirement(ResultRequirement req) {
        if (requirements == null) {
            requirements = new ArrayList<>();
        }
        requirements.add(req);
    }

    /**
     * Adds a List of {@link ResultRequirement}s which have to be met in order for the
     * {@link ConditionalStep} to be displayed to the user.
     * */
    @Override
    public void addRequirements(List<ResultRequirement> reqs) {
        if (requirements == null) {
            requirements = new ArrayList<>();
        }
        requirements.addAll(reqs);
    }

    /**
     * Checks if all the {@link ResultRequirement}s of a {@link ConditionalStep} are met by the
     * answers given by the user up to the point of the check.
     * */
    @Override
    public boolean requirementsAreSatisfiedBy(TaskResult result) {
        for (ResultRequirement req : requirements) {
            if (req.isSatisfiedBy(result) == false) {
                return false;
            }
        }
        return true;
    }
}