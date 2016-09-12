package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire;

import org.researchstack.backbone.result.TaskResult;

import java.util.List;

/**
 * C3-PRO
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
 * This Interface defines the methods used for ConditionalSteps which can be added to
 * {@link ConditionalOrderedTask}s and are only shown to the user when certain conditions are met.
 * The logic is derived from the FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} element.
 * {@link ResultRequirement}s can be added to the step and will be verified every time before
 * displaying the step to the user.
 */
public interface ConditionalStep {
    /**
     * Adds a {@link ResultRequirement} which has to be met in order for the ConditionalStep to be
     * displayed to the user.
     * */
    void addRequirement(ResultRequirement req);

    /**
     * Adds a List of {@link ResultRequirement}s which have to be met in order for the ConditionalStep
     * to be displayed to the user.
     * */
    void addRequirements(List<ResultRequirement> reqs);

    /**
     * Checks if all the {@link ResultRequirement}s of a ConditionalStep are met by the answers
     * given by the user up to the point of the check.
     * */
    boolean requirementsAreSatisfiedBy(TaskResult result);
}
