package ch.usz.c3pro.c3_pro_android_framework.questionnaire.logic;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BooleanAnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.DateAnswerFormat;
import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.usz.c3pro.c3_pro_android_framework.utils.StringUtil;

/**
 * C3PRO
 * <p/>
 * Created by manny Weber on 05/02/16.
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
 * This class provides the tools to create ResearchStack {@link org.researchstack.backbone.step.Step}s from FHIR
 * {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent}s.
 * Referenced ValueSets in getOptions() of ChoiceQuestions can only be resolved if included in the
 * FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire} file.
 */
public class Items2Steps {

    /**
     * Returns a list of {@link Step}s and {@link ConditionalStep}s that can be added to a
     * {@link ConditionalOrderedTask}. The Steps are created recursively from the passed List of
     * {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent}s. If a group item
     * has {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} conditions, these conditions
     * are added to all the child steps as {@link ResultRequirement}s
     *
     * @param items List of FHIR items that may contain more items
     * @return List containing {@link Step}s and {@link ConditionalStep}s that can be added to a {@link ConditionalOrderedTask}
     */
    public static List<Step> items2Steps(List<Questionnaire.QuestionnaireItemComponent> items) {
        List<Step> steps = new ArrayList<>();

        for (Questionnaire.QuestionnaireItemComponent item : items) {

            if (item.getType() == Questionnaire.QuestionnaireItemType.GROUP) {
                List<Step> newSteps = items2Steps(item.getItem());
                if (item.hasEnableWhen()) {
                    List<ResultRequirement> reqs = getRequirementsFor(item);
                    for (Step step : newSteps) {
                        if (step instanceof QuestionStep) {
                            ConditionalQuestionStep newStep = (ConditionalQuestionStep) step;
                            newStep.addRequirements(reqs);
                            steps.add(newStep);
                        } else if (step instanceof InstructionStep) {
                            ConditionalInstructionStep newStep = (ConditionalInstructionStep) step;
                            newStep.addRequirements(reqs);
                            steps.add(newStep);
                        }
                    }
                } else {
                    steps.addAll(newSteps);
                }
            } else {
                Step newStep = Items2Steps.item2Step(item);

                if (item.hasEnableWhen()) {
                    List<ResultRequirement> reqs = getRequirementsFor(item);
                    ((ConditionalStep) newStep).addRequirements(reqs);
                }
                steps.add(newStep);
            }

        }
        return steps;
    }

    /**
     * Returns a {@link org.researchstack.backbone.step.Step} or {@link ConditionalStep} that can be added to a
     * {@link ConditionalOrderedTask}. The Step is created from the passed
     * {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent}. The item must be
     * a leaf; child items are ignored. (Group items have to be passed in a list to
     * {@link #items2Steps(List)}.) If the Item has {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent}
     * conditions, these conditions are added to the step as {@link ResultRequirement}s.
     *
     * @param item FHIR item, not of the type "group"
     * @return {@link Step} or {@link ConditionalStep} that can be added to a {@link ConditionalOrderedTask}
     */
    public static Step item2Step(Questionnaire.QuestionnaireItemComponent item) {

        String linkId = item.getLinkId();
        String id = StringUtil.isNotNullOrEmpty(linkId) ? linkId : UUID.randomUUID().toString();

        //TODO create nice title and text

        String itemText = item.getText();
        String text = StringUtil.isNotNullOrEmpty(itemText) ? itemText : getAlternativeTextForItem(item);
        if (!StringUtil.isNotNullOrEmpty(id)) {
            id = itemText;
        }

        if (item.getType() == Questionnaire.QuestionnaireItemType.DISPLAY) {
            if (item.hasEnableWhen()) {
                return new ConditionalInstructionStep(id, "", itemText);
            } else {
                return new InstructionStep(id, "", itemText);
            }
        } else {

            AnswerFormat fmt = getAnswerformat(item);

            if (item.hasEnableWhen()) {
                ConditionalQuestionStep step = new ConditionalQuestionStep(id, text, fmt);
                step.setOptional(!item.getRequired());
                return step;

            } else {
                QuestionStep step = new QuestionStep(id, text, fmt);
                step.setOptional(!item.getRequired());
                return step;
            }
        }
    }

    /**
     * Matches the FHIR question type with the appropriate ResearchStack {@link org.researchstack.backbone.answerformat.AnswerFormat}.
     * Not all required AnswerFormats are yet implemented by ResearchStack, so matches are made as
     * sensible as possible.
     *
     * @param item The item from which a Step will be created.
     * @return An AnswerFormat that can be used to create a {@link org.researchstack.backbone.step.QuestionStep}
     */
    private static AnswerFormat getAnswerformat(Questionnaire.QuestionnaireItemComponent item) {

        /*
        *     FHIR Question Types
        *     --------------------------------------------------------------------
        *     BOOLEAN("boolean", "http://hl7.org/fhir/answer-format"),
        *     DECIMAL("decimal", "http://hl7.org/fhir/answer-format"),
        *     INTEGER("integer", "http://hl7.org/fhir/answer-format"),
        *     DATE("date", "http://hl7.org/fhir/answer-format"),
        *     DATE_TIME("dateTime", "http://hl7.org/fhir/answer-format"),
        *     INSTANT("instant", "http://hl7.org/fhir/answer-format"),
        *     TIME("time", "http://hl7.org/fhir/answer-format"),
        *     STRING("string", "http://hl7.org/fhir/answer-format"),
        *     TEXT("text", "http://hl7.org/fhir/answer-format"),
        *     URL("url", "http://hl7.org/fhir/answer-format"),
        *     CHOICE("choice", "http://hl7.org/fhir/answer-format"),
        *     OPEN_CHOICE("open-choice", "http://hl7.org/fhir/answer-format"),
        *     ATTACHMENT("attachment", "http://hl7.org/fhir/answer-format"),
        *     REFERENCE("reference", "http://hl7.org/fhir/answer-format"),
        *     QUANTITY("quantity", "http://hl7.org/fhir/answer-format");
        *
        *
        *     Researchstack Answer Formats
        *     --------------------------------------------------------------------
        *     BooleanAnswerFormat
        *     ChoiceAnswerFormat
        *     DateAnswerFormat
        *     DecimalAnswerFormat
        *     EmailAnswerFormat
        *     FormAnswerFormat
        *     IntegerAnswerFormat
        *     TextAnswerFormat
        *
        * */


        switch (item.getType()) {
            case BOOLEAN:
                return new BooleanAnswerFormat("Yes", "No");
            case DECIMAL:
                // for decimal, there is no implementedStepBody.class, have to use integer for now
                return new IntegerAnswerFormat(0, 1000);
            case INTEGER:
                List<Extension> minVals = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/minValue");
                List<Extension> maxVals = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/maxValue");
                /**Default Answer not yet used in available AnswerFormats*/
                //Extension dflt = defaultAnswer(item);

                if (!minVals.isEmpty() && !maxVals.isEmpty()) {
                    //Type sMinVal = minVals.get(0).getValue();
                    String sMinVal = minVals.get(0).getValue().primitiveValue();
                    String sMaxVal = maxVals.get(0).getValue().primitiveValue();

                    int minVal = Integer.parseInt(sMinVal);
                    int maxVal = Integer.parseInt(sMaxVal);

                    /**Default Answer not yet used in available AnswerFormats
                     int def = minVal;
                     if (dflt != null) {
                     String sDef = dflt.getValue().primitiveValue();
                     def = Integer.parseInt(sDef);
                     }
                     */

                    // scale answer format not yet available, so have to use Integer
                    //return new IntegerAnswerFormat(AnswerFormat. some scale answer style)
                    return new IntegerAnswerFormat(minVal, maxVal);
                } else {
                    return new IntegerAnswerFormat(0, 1000);
                }
            case DATE:
                return new DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date);
            case DATETIME:
                //not implementedStepBody
                //return new DateAnswerFormat(AnswerFormat.DateAnswerStyle.DateAndTime);
                return new DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date);
            //case "instant": return new DateAnswerFormat();
            //case "time":
            case TIME:
                //not implemented yet
                return new TextAnswerFormat(5);
            case STRING:
                return new TextAnswerFormat(300);
            case TEXT:
                return new TextAnswerFormat();
            case CHOICE:
                if (item.hasRepeats() && item.getRepeats()) {
                    return new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                            resolveAnswerChoices(item));
                } else {
                    return new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice,
                            resolveAnswerChoices(item));
                }
            case OPENCHOICE:
                return new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.MultipleChoice,
                        resolveAnswerChoices(item));
            //case "attachment":
            //case "reference":
            case QUANTITY:
                return new IntegerAnswerFormat(0, 1000);
            default:
                return new TextAnswerFormat();
        }
    }

    /**
     * Returns the minimum number of times the item must appear or the minimum number of answers
     * for a question - when greater than 1
     */
    private static int getQuestionMin(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-minOccurs");
        if (!list.isEmpty()) {
            return Integer.parseInt(list.get(0).getValue().toString());
        } else {
            return 1;
        }
    }

    /**
     * Returns the maximum number of times the group must appear or the minimum number of answers
     * for a question - when greater than 1 and not unlimited
     */
    private static int questionMaxOccurs(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-maxOccurs");
        if (!list.isEmpty()) {
            return Integer.parseInt(list.get(0).getValue().toString());
        } else {
            return 1;
        }
    }

    /**
     * Returns the text proving instructions intended to be rendered with the item explaining
     * how the content of the item is to be completed.
     */
    private static String questionInstruction(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-instruction");
        if (!list.isEmpty()) {
            return list.get(0).getValue().toString();
        } else {
            return null;
        }
    }

    /**
     * Returns the content intended for display if a user selects an icon or performs some other
     * action seeking help about the element.
     */
    private static String questionHelpText(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-help");
        if (!list.isEmpty()) {
            return list.get(0).getValue().toString();
        } else {
            return null;
        }
    }

    /**
     * Returns the units in which the question's answer should be captured.
     */
    private static String numericAnswerUnit(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-units");
        if (!list.isEmpty()) {
            return list.get(0).getValue().toString();
        } else {
            return null;
        }
    }

    /**
     * Returns the value that should be pre-populated when rendering the questionnaire for user input.
     */
    private static Extension defaultAnswer(Questionnaire.QuestionnaireItemComponent item) {
        List<Extension> list = item.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-defaultValue");
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns a text created from the item's instruction text or help text that can be used in case
     * the item itself does not have a proper text to display.
     */
    private static String getAlternativeTextForItem(Questionnaire.QuestionnaireItemComponent item) {
        String instr = questionInstruction(item);
        String hlp = questionHelpText(item);

        String txt = StringUtil.isNotNullOrEmpty(instr) ? instr : hlp;
        return StringUtil.isNotNullOrEmpty(txt) ? txt : "no Text";
    }

    /**
     * Returns an Array of {@link org.researchstack.backbone.model.Choice}s created from either the item's getOption() or getOptions().
     * Referenced ValueSets in getOptions() can only be resolved if included in the FHIR
     * questionnaire file.
     */
    private static Choice[] resolveAnswerChoices(Questionnaire.QuestionnaireItemComponent item) {

        // where we possibly find options
        List<Questionnaire.QuestionnaireItemOptionComponent> option = item.getOption();
        Reference reference = item.getOptions();

        // choices we are going to return

        /*
        * if options contains codings, we fill the options into the choiceList
        * does this happen at all?
        * */
        if (!option.isEmpty()) {
            List<Choice> choiceList = new ArrayList<>();

            for (Questionnaire.QuestionnaireItemOptionComponent c : option) {
                String text = c.getValue().primitiveValue();
                Type value = c.getValue();
                choiceList.add(new Choice<Type>(text, value));
            }
            return choiceList.toArray(new Choice[choiceList.size()]);
        }


        /*
        * if a reference exists, resolve valueSet
        * for now assuming that only internal references exist
        * */
        else if (reference.getResource() != null) {
            ValueSet vSet = (ValueSet) reference.getResource();

            // this happens with included options // valueset contained
            List<ValueSet.ConceptSetComponent> includes = vSet.getCompose().getInclude();
            if (!includes.isEmpty()) {
                List<Choice> choiceList = new ArrayList<>();
                for (ValueSet.ConceptSetComponent include : includes) {
                    String system = include.getSystem();
                    List<ValueSet.ConceptReferenceComponent> concepts = include.getConcept();
                    for (ValueSet.ConceptReferenceComponent concept : concepts) {
                        String text = concept.getDisplay();
                        String code = concept.getCode();
                        Coding coding = new Coding();
                        coding.setDisplay(text);
                        coding.setSystem(system);
                        coding.setCode(code);
                        choiceList.add(new Choice<Type>(text, coding));
                        //choiceList.add(new Choice<Type>(text, new StringType(system + "#" + code)));
                    }
                }
                return choiceList.toArray(new Choice[choiceList.size()]);
            }


            // does this happen at all?
            List<ValueSet.ValueSetExpansionContainsComponent> expansion = vSet.getExpansion().getContains();
            if (!expansion.isEmpty()) {
                List<Choice> choiceList = new ArrayList<>();
                for (ValueSet.ValueSetExpansionContainsComponent contain : expansion) {
                    String text = contain.getDisplay();
                    String code = contain.getCode();
                    String system = contain.getSystem();
                    Coding coding = new Coding();
                    coding.setSystem(system);
                    coding.setCode(code);
                    coding.setDisplay(text);
                    choiceList.add(new Choice<Type>(text, coding));

                    //choiceList.add(new Choice<Type>(text, new StringType(system + "#" + code)));
                }
                return choiceList.toArray(new Choice[choiceList.size()]);
            }
            /* old version:
            List<ValueSet.ExpansionContains> expansion = vSet.getExpansion().getContains();
            if (!expansion.isEmpty()) {
                List<Choice> choiceList = new ArrayList<Choice>();
                for (ValueSet.ExpansionContains contain : expansion) {
                    String text = contain.getDisplay();
                    String code = contain.getCode();
                    choiceList.add(new Choice(text, code));
                }
                return choiceList.toArray(new Choice[choiceList.size()]);
            }*/

        }


        /*
        * noob error handling, don't try this at home, do it right
        * */
        else {
            Choice[] choiceArray = {new Choice<String>("no choices found", "N/A")};
            return choiceArray;
        }
        Choice[] choiceArray = {new Choice<String>("no choices found", "N/A")};
        return choiceArray;
    }

    /**
     * Returns a list with a {@link ResultRequirement} for every
     * {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} of the item.
     */
    private static List<ResultRequirement> getRequirementsFor(Questionnaire.QuestionnaireItemComponent item) {
        List<ResultRequirement> reqs = new ArrayList<>();
        for (Questionnaire.QuestionnaireItemEnableWhenComponent enableWhen : item.getEnableWhen()) {

            String question = enableWhen.getQuestion();
            Type answer = enableWhen.getAnswer();

            reqs.add(new ResultRequirement(question, answer));
        }
        return reqs;
    }
}
