package ch.usz.c3pro.c3_pro_android_framework.questionnaire.logic;

import android.util.Log;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * C3PRO
 * <p>
 * Created by manny Weber on 05/18/16.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class implements the logic of {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent}s.
 * For every {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} of
 * an item, a ResultRequirement should be added to the according {@link org.researchstack.backbone.step.Step}s
 * of the item and its child items.
 */
public class ResultRequirement implements Serializable {
    private String questionIdentifier;
    private Type reqAnswer;

    /**
     * Constructor.
     * Returns an initialized ResultRequirement for the questionID with of the question to be checked
     * and the answer to that question for the Requirement to be met
     *
     * @param questionID       LinkId of the question to be checked
     * @param enableWhenAnswer Required answer to the question for the Requirement to be met.
     */
    public ResultRequirement(String questionID, Type enableWhenAnswer) {
        questionIdentifier = questionID;
        reqAnswer = enableWhenAnswer;
    }

    /**
     * Returns whether or not the ResultRequirement is met by the answers given by the user up to
     * the point of check.
     *
     * @param result TaskResult containing the answers given by the user so far.
     * @return boolean indicating whether the Requirement is met or not.
     */
    public boolean isSatisfiedBy(TaskResult result) {
        StepResult resultAnswer = result.getStepResult(questionIdentifier);
        if (resultAnswer.getAnswerFormat().getQuestionType() == AnswerFormat.Type.MultipleChoice) {

            Object[] resultArray = (Object[]) resultAnswer.getResult();
            for (int i = 0; i < resultArray.length; i++) {
                if (isSatisfiedBy(resultArray[i])) {
                    return true;
                }
            }
            return false;
        } else {
            return isSatisfiedBy(resultAnswer.getResult());
        }
    }

    /**
     * Returns whether or not the ResultRequirement is met by the answers given by the user up to
     * the point of check.
     *
     * @param resultAnswer TaskResult.getResult() Object containing the answer given by the user so far.
     * @return boolean indicating whether the Requirement is met or not.
     */
    public boolean isSatisfiedBy(Object resultAnswer) {


        // TODO all other answerTypes

        /*
        Types of FHIR Questionnaire Answers
        value[x]		0..1		Single-valued answer to the question
        ...... valueBoolean			boolean
        ...... valueDecimal			decimal
        ...... valueInteger			integer
        ...... valueDate			date
        ...... valueDateTime		dateTime
        ...... valueInstant			instant
        ...... valueTime			time
        ...... valueString			string
        ...... valueUri			    uri
        ...... valueAttachment		Attachment
        ...... valueCoding			Coding
        ...... valueQuantity		Quantity
        ...... valueReference		Reference(Any)

        Implemented ResearchStack AnswerTypes
        None(NotImplementedStepBody.class),
        Scale(NotImplementedStepBody.class),
        SingleChoice(SingleChoiceQuestionBody.class),
        MultipleChoice(MultiChoiceQuestionBody.class),
        Decimal(NotImplementedStepBody.class),
        Integer(IntegerQuestionBody.class),
        Boolean(SingleChoiceQuestionBody.class),
        Eligibility(NotImplementedStepBody.class),
        Text(TextQuestionBody.class),
        TimeOfDay(NotImplementedStepBody.class),
        DateAndTime(NotImplementedStepBody.class),
        Date(DateQuestionBody.class),
        TimeInterval(NotImplementedStepBody.class),
        Location(NotImplementedStepBody.class),
        Form(FormBody.class);
        */

        if (reqAnswer instanceof BooleanType) {
            boolean reqBool = ((BooleanType) reqAnswer).booleanValue();
            Boolean ansBool = (Boolean) resultAnswer;

            return reqBool == ansBool;

        } else if (reqAnswer instanceof DecimalType) {
            // Decimal Answer Format not implemented yet; using Integer
            int reqInt = ((IntegerType) reqAnswer).getValue();
            int ansInt = (int) resultAnswer;

            return reqInt == ansInt;

        } else if (reqAnswer instanceof IntegerType) {
            int reqInt = ((IntegerType) reqAnswer).getValue();
            int ansInt = (int) resultAnswer;

            return reqInt == ansInt;

        } else if (reqAnswer instanceof DateType) {
            Date reqDate = ((DateType) reqAnswer).getValue();
            Date ansDate = new Date((long) resultAnswer);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

            return fmt.format(reqDate).equals(fmt.format(ansDate));


        } /*else if (reqAnswer instanceof DateTimeType) {
            // TODO timezones??
            Date reqDate = ((DateType) reqAnswer).getValue();
            Date ansDate = new Date((long) resultAnswer.getResult());
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmm");

            return fmt.format(reqDate).equals(fmt.format(ansDate));
        }*/

        /*Instant not implemented yet*/
        /*Time not implemeted yet

        */
        else if (reqAnswer instanceof StringType) {
            String reqString = ((StringType) reqAnswer).getValue();
            String ansString = (String) resultAnswer;

            return reqString.equals(ansString);

        }

        /*URI not implemented yet*/
        /*Attachment not implemeted yet*/

        else if (reqAnswer instanceof Coding) {
            // single choice
            Coding reqCode = (Coding) reqAnswer;
            Coding ansCode = (Coding) resultAnswer;
            return reqCode.getSystem().equals(ansCode.getSystem()) && reqCode.getCode().equals(ansCode.getCode());

            // TODO multichoice?
        }

        /*Quantity not implemented yet*/
        /*Reference not implemeted yet*/

        return false;
    }
}
