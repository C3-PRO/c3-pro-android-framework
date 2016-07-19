package ch.usz.c3pro.c3_pro_android_framework.questionnaire.logic;

import org.hl7.fhir.dstu3.model.Questionnaire;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;

import java.util.List;

/**
 * C3PRO
 *
 * Created by manny Weber on 05/02/16.
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
 * This is a static class that will provide the conversion of a FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire} Resource
 * to a ResearchStack {@link org.researchstack.backbone.task.Task} that can be used with a ResearchStack
 * {@link org.researchstack.backbone.ui.ViewTaskActivity} to conduct a survey.
 * Referenced ValueSets in getOptions() of ChoiceQuestions can only be resolved if included in the
 * FHIR questionnaire file.
 * Not all QuestionTypes are supported yet.
 * @see org.researchstack.backbone.answerformat.AnswerFormat
 */
public class Questionnaire2Task {

    /**
    * Returns a ResearchStack {@link org.researchstack.backbone.task.Task} that can be viewed by a
     * {@link org.researchstack.backbone.ui.ViewTaskActivity} based on a FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire}.
     * If the items have {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent}s, the returned
     * {@link Task} will be a {@link ConditionalOrderedTask}.
     *
     * @param questionnaire a HAPI FHIR Questionnaire Resource
     * @return              a ResearchStack Task
    */
    public static Task questionnaire2Task (Questionnaire questionnaire){

        List<Questionnaire.QuestionnaireItemComponent> items = questionnaire.getItem();
        String identifier = questionnaire.getId();

        List<Step> steps = Items2Steps.items2Steps(items);

        return new ConditionalOrderedTask(identifier, steps);
    }
}
