package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Strings;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Contract;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Group;
import org.hl7.fhir.dstu3.model.Organization;
import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BooleanAnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.DateAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.model.ConsentDocument;
import org.researchstack.backbone.model.ConsentSection;
import org.researchstack.backbone.model.ConsentSignature;
import org.researchstack.backbone.step.ConsentDocumentStep;
import org.researchstack.backbone.step.ConsentSharingStep;
import org.researchstack.backbone.step.ConsentSignatureStep;
import org.researchstack.backbone.step.ConsentVisualStep;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout;
import org.researchstack.skin.step.PassCodeCreationStep;

import java.util.ArrayList;
import java.util.List;

import ch.usz.c3pro.c3_pro_android_framework.R;
import ch.usz.c3pro.c3_pro_android_framework.consent.ViewConsentTaskActivity;
import ch.usz.c3pro.c3_pro_android_framework.errors.Logging;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ConditionalOrderedTask;
import ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.questionnaire.ResultRequirement;

/**
 * C3-PRO
 * <p>
 * Created by manny Weber on 08/02/2016.
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
 * This class provides the tools to convert a FHIR / C3-PRO contract to a task that assesses the
 * eligibility of the user and guides them through the consenting process.
 */
public class ContractAsTask {
    // debug
    public static boolean skip = false;

    private static String kContractTermConsentSectionType = "http://researchkit.org/docs/Constants/ORKConsentSectionType";
    private static String kContractTermConsentSectionExtension = "http://fhir-registry.smarthealthit.org/StructureDefinition/ORKConsentSection";

    public static final String ID_ELIGIBILITY_AND_CONSENT_TASK = "ID_ELIGIBILITY_AND_CONSENT_TASK";
    public static final String ID_CONSENT_TASK = "ID_CONSENT_TASK";
    public static final String ID_ELIGIBILITY_TASK = "ID_ELIGIBILITY_TASK";
    public static final String ID_VISUAL = "ID_VISUAL";
    public static final String ID_ELIGIBILITY_INSTRUCTION_STEP = "ID_ELIGIBILITY_INSTRUCTION_STEP";
    public static final String ID_ELIGIBILITY_FORM_STEP = "ID_ELIGIBILITY_FORM_STEP";
    public static final String ID_ELIGIBILITY_ASSESSMENT_STEP = "ID_ELIGIBILITY_ASSESSMENT_STEP";
    public static final String ID_QUIZ_RESULT = "ID_QUIZ_RESULT";
    public static final String ID_SHARING = "ID_SHARING";
    public static final String ID_CONSENT_STEP = "ID_CONSENT_STEP";
    public static final String ID_CONSENT_DOC = "consent_review_doc";
    public static final String ID_FORM = "ID_FORM";
    public static final String ID_FORM_NAME = "ID_FORM_NAME";
    public static final String ID_FORM_DOB = "ID_FORM_DOB";
    public static final String ID_SIGNATURE = "ID_SIGNATURE";
    public static final String ID_PASSCODE_INSTRUCTION = "ID_PASSCODE_INSTRUCTION";
    public static final String ID_PASSCODE_STEP = "ID_PASSCODE_STEP";

    /**
     * Returns a {@link ConditionalOrderedTask} that can be presented by a {@link ViewConsentTaskActivity}
     * checking the user's eligibility and getting their consent.
     */
    public static Task getContractAsTask(Context context, Contract contract, ConsentTaskOptions options) {
        ConsentDocument consentDocument = getConsentDocumentFromContract(context, contract, options);
        List<ConsentSection> sections = consentDocument.getSections();

        List<Step> steps = new ArrayList<>();

        // add Eligibility Steps
        initEligibilitySteps(context, steps, contract);

        // add Visual Consent Steps
        innitVisualSteps(context, steps, sections);

        //quiz?
        //initQuizSteps(steps);

        // sharing step
        if (options.askForSharing()) {
            innitConsentSharingStep(context, steps, contract, options.getShareMoreInfoDocument());
        }

        // consent review step
        initConsentReviewSteps(context, steps, consentDocument);

        // set passcode step
        if (options.askToCreatePasscode()) {
            initPassCodeSteps(context, steps);
        }

        ConditionalOrderedTask orderedTask = new ConditionalOrderedTask(ID_ELIGIBILITY_AND_CONSENT_TASK, steps);
        return orderedTask;
    }

    private static void initEligibilitySteps(Context context, List<Step> steps, Contract contract) {

        if ((Group) contract.getSubjectFirstRep().getResource() != null) {
            List<Group.GroupCharacteristicComponent> characteristicComponents = ((Group) contract.getSubjectFirstRep().getResource()).getCharacteristic();

            // only add eligibility steps if at least one eligibility criteria exists
            if (!characteristicComponents.isEmpty()) {
                InstructionStep instructionStep = new InstructionStep(ID_ELIGIBILITY_INSTRUCTION_STEP, context.getString(R.string.c3_eligibility_instruction_title), context.getString(R.string.c3_eligibility_instruction_text));
                instructionStep.setStepTitle(R.string.rss_eligibility);
                steps.add(instructionStep);


                FormStep formStep = new FormStep(ID_ELIGIBILITY_FORM_STEP, "", "");
                formStep.setStepTitle(R.string.rss_eligibility);
                formStep.setOptional(false);

                BooleanAnswerFormat booleanAnswerFormat = new BooleanAnswerFormat("Yes", "No");

                List<ResultRequirement> eligibleRequirements = new ArrayList<>();
                List<QuestionStep> questionSteps = new ArrayList<>();

                int i = 0;
                for (Group.GroupCharacteristicComponent component : characteristicComponents) {
                    String id = "eligibility " + Integer.toString(i);
                    QuestionStep questionStep = new QuestionStep(id, component.getCode().getText(), booleanAnswerFormat);
                    questionSteps.add(questionStep);

                    EligibilityRequirement requirement = new EligibilityRequirement(id, component.getValue());
                    eligibleRequirements.add(requirement);
                    i++;
                }

                formStep.setFormSteps(questionSteps);
                steps.add(formStep);

                EligibilityAssessmentStep eligibilityAssessmentStep = new EligibilityAssessmentStep(ID_ELIGIBILITY_ASSESSMENT_STEP, context.getString(R.string.c3_eligible_title), context.getString(R.string.c3_eligible_text), context.getString(R.string.c3_not_eligible_title), context.getString(R.string.c3_not_eligible_text), eligibleRequirements);
                steps.add(eligibilityAssessmentStep);
            }
        }
    }

    private static ConsentDocument getConsentDocumentFromContract(Context context, Contract contract, ConsentTaskOptions options) {
        // Create consent signature object and set what info is required
        ConsentSignature signature = new ConsentSignature();
        signature.setRequiresName(options.requiresName());
        signature.setRequiresSignatureImage(options.requiresSignature());
        signature.setRequiresBirthDate(options.requiresBirthday());

        ConsentDocument document = new ConsentDocument();
        document.setTitle(context.getString(R.string.rsb_consent));
        document.setSignaturePageTitle(R.string.rsb_consent);

        document.addSignature(signature);

        // Add contract terms as consent sections and create consent review html
        StringBuilder reviewHTML = new StringBuilder();

        if (contract.hasTerm()) {
            List<ConsentSection> consentSections = new ArrayList<ConsentSection>();
            List<Contract.TermComponent> terms = contract.getTerm();
            for (Contract.TermComponent termComponent : terms) {
                consentSections.add(getContractTermAsConsentSection(context, termComponent));
                reviewHTML.append(getContractTermAsHTMLString(context, termComponent));
            }
            document.setSections(consentSections);
        } else {
            Log.e(Logging.logTag, "Contract provides no terms for consent process");
        }

        // set the review document from options when provided, otherwise from contract terms
        if (!Strings.isNullOrEmpty(options.getReviewConsentDocument())) {
            document.setHtmlReviewContent(ResourcePathManager.getResourceAsString(context, "html/" + options.getReviewConsentDocument() + ".html"));
        } else {
            document.setHtmlReviewContent(reviewHTML.toString());
        }

        return document;
    }

    private static void innitVisualSteps(Context context, List<Step> steps, List<ConsentSection> sections) {

        if (skip) {
            // visual steps for all consent sections
            ConsentVisualStep consentVisualStep = new ConsentVisualStep("visual: " + sections.get(0).getTitle());
            consentVisualStep.setStepTitle(R.string.rsb_consent);
            consentVisualStep.setSection(sections.get(0));
            consentVisualStep.setNextButtonString("next");
            steps.add(consentVisualStep);
        } else {

            int i = 0;
            int size = sections.size();
            for (ConsentSection section : sections) {
                ConsentVisualStep consentVisualStep = new ConsentVisualStep(ID_VISUAL + i);
                consentVisualStep.setStepTitle(R.string.rsb_consent);
                consentVisualStep.setSection(section);

                String nextString = context.getString(R.string.rsb_next);
                if (section.getType() == ConsentSection.Type.Overview) {
                    nextString = context.getString(R.string.rsb_get_started);
                } else if (i == size - 1) {
                    nextString = context.getString(R.string.rsb_done);
                }
                consentVisualStep.setNextButtonString(nextString);

                steps.add(consentVisualStep);
                i++;
            }
        }
    }

    private static void innitConsentSharingStep(Context context, List<Step> steps, Contract contract, String shareMoreInfoDocument) {

        ConsentSharingStep consentSharingStep = new ConsentSharingStep(ID_SHARING);
        consentSharingStep.setStepTitle(R.string.rsb_consent);
        consentSharingStep.setTitle(context.getString(R.string.rsb_consent_share_title));
        if (!Strings.isNullOrEmpty(shareMoreInfoDocument)) {
            String learnMore = ResourcePathManager.getResourceAsString(context, "html/" + shareMoreInfoDocument + ".html");
            consentSharingStep.setText(learnMore);
        }

        String teamName = ((Organization) contract.getAuthorityFirstRep().getResource()).getName();
        String shareWidely = context.getString(R.string.rsb_consent_share_widely, teamName);
        Choice<BooleanType> shareWidelyChoice = new Choice<>(shareWidely, new BooleanType(true), null);

        String shareRestricted = context.getString(R.string.rsb_consent_share_only, teamName);
        Choice<BooleanType> shareRestrictedChoice = new Choice<>(shareRestricted, new BooleanType(false), null);

        consentSharingStep.setAnswerFormat(new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice, shareWidelyChoice, shareRestrictedChoice));

        steps.add(consentSharingStep);
    }

    private static void initConsentReviewSteps(Context context, List<Step> steps, ConsentDocument document) {

        ConsentDocumentStep step = new ConsentDocumentStep(ID_CONSENT_STEP);
        //
        step.setStepTitle(R.string.rsb_consent_review_title);
        step.setTitle(context.getString(R.string.rsb_consent_review_title));

        // Create the title and instruction for the review of the consent document
        StringBuilder docBuilder = new StringBuilder(
                "</br><div style=\"padding: 10px 10px 10px 10px;\" class='header'>");
        String title = context.getString(R.string.rsb_consent_review_title);
        docBuilder.append(String.format(
                "<h1 style=\"text-align: center;\">%1$s</h1>",
                title));
        String detail = context.getString(R.string.rsb_consent_review_instruction);
        docBuilder.append(String.format("<p style=\"text-align: center\">%1$s</p>", detail));
        docBuilder.append("</div></br>");
        docBuilder.append(document.getHtmlReviewContent());

        step.setConsentHTML(docBuilder.toString());
        step.setConfirmMessage(context.getString(R.string.c3_confirm_consent));
        steps.add(step);

        // Add full-name input
        boolean requiresName = document.getSignature(0).requiresName();
        boolean requiresBirthDate = document.getSignature(0).requiresBirthDate();
        if (requiresName || requiresBirthDate) {
            List<QuestionStep> formSteps = new ArrayList<>();
            if (requiresName) {
                TextAnswerFormat format = new TextAnswerFormat();
                format.setIsMultipleLines(false);

                String placeholder = context.getString(R.string.rsb_consent_name_placeholder);
                String nameText = context.getString(R.string.rsb_consent_name_full);
                formSteps.add(new QuestionStep(ID_FORM_NAME, nameText, format));
            }

            if (requiresBirthDate) {
                DateAnswerFormat dateFormat = new DateAnswerFormat(AnswerFormat.DateAnswerStyle.Date);
                String dateText = "Day of Birth";
                formSteps.add(new QuestionStep(ID_FORM_DOB, dateText, dateFormat));
            }

            String formTitle = context.getString(R.string.rsb_consent_form_title);
            FormStep formStep = new FormStep(ID_FORM, formTitle, step.getText());
            formStep.setStepTitle(R.string.rsb_consent);
            formStep.setOptional(false);
            formStep.setFormSteps(formSteps);
            steps.add(formStep);
        }

        // Add signature input
        if (document.getSignature(0).requiresSignatureImage()) {
            ConsentSignatureStep signatureStep = new ConsentSignatureStep(ID_SIGNATURE);
            signatureStep.setStepTitle(R.string.rsb_consent);
            signatureStep.setTitle(context.getString(R.string.rsb_consent_signature_title));
            signatureStep.setText(context.getString(R.string.rsb_consent_signature_instruction));
            signatureStep.setOptional(false);
            signatureStep.setSignatureDateFormat(document.getSignature(0).getSignatureDateFormatString());
            signatureStep.setStepLayoutClass(ConsentSignatureStepLayout.class);
            steps.add(signatureStep);
        }
    }

    private static void initPassCodeSteps(Context context, List<Step> steps) {
        InstructionStep instructionStep = new InstructionStep(ID_PASSCODE_INSTRUCTION, context.getString(R.string.rsb_pincode_enter_title), context.getString(R.string.c3_passcode_instruction));

        instructionStep.setStepTitle(R.string.rsb_consent);
        steps.add(instructionStep);
        PassCodeCreationStep passCodeCreationStep = new PassCodeCreationStep(ID_PASSCODE_STEP, R.string.rsb_consent);
        steps.add(passCodeCreationStep);
    }

    private static ConsentSection getContractTermAsConsentSection(Context context, Contract.TermComponent termComponent) {
        ConsentSection consentSection = new ConsentSection(getTypeForComponent(termComponent));

        consentSection.setSummary(termComponent.getText());
        List<Extension> sectionExtensions = termComponent.getExtensionsByUrl(kContractTermConsentSectionExtension);
        List<Extension> extensions = sectionExtensions.get(0).getExtension();
        for (Extension extension : extensions) {
            switch (extension.getUrl()) {
                case "title":
                    consentSection.setTitle(extension.getValue().primitiveValue());
                    break;
                case "htmlContent":
                    consentSection.setHtmlContent(extension.getValue().primitiveValue());
                    break;
                case "htmlContentFile":
                    String htmlContent = ResourcePathManager.getResourceAsString(context, "html/" + extension.getValue().primitiveValue() + ".html");
                    consentSection.setHtmlContent(htmlContent);
                    break;
                case "image":
                    //TODO image
                    Log.d(Logging.logTag, "can't set customImageName, private field with no setter =(");
                    break;
                case "animation":
                    //TODO animation
                    Log.d(Logging.logTag, "can't set customAnimation for ConsentSections =(");
                    break;
                default:
                    Log.d(Logging.logTag, "no matching extension for " + extension.getUrl());
            }
        }
        return consentSection;
    }

    /**
     * Gets a TermComponent of a FHIR Contract as a HTML string. TermComponents of type overview will
     * return an empty string.
     */
    private static String getContractTermAsHTMLString(Context context, Contract.TermComponent termComponent) {
        if (getTypeForComponent(termComponent) == ConsentSection.Type.Overview) {
            return "";
        } else {
            List<Extension> sectionExtensions = termComponent.getExtensionsByUrl(kContractTermConsentSectionExtension);
            List<Extension> extensions = sectionExtensions.get(0).getExtension();

            String text = termComponent.getText();
            String title = "";
            String content = "";

            for (Extension extension : extensions) {
                switch (extension.getUrl()) {
                    case "title":
                        title = extension.getValue().primitiveValue();
                        break;
                    case "htmlContent":
                        content = extension.getValue().primitiveValue();
                        break;
                    case "htmlContentFile":
                        content = ResourcePathManager.getResourceAsString(context, "html/" + extension.getValue().primitiveValue() + ".html");
                        break;
                    default:
                        break;
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            if (!Strings.isNullOrEmpty(title)) {
                stringBuilder.append("<h3>" + title + "</h3>");
            }
            if (!Strings.isNullOrEmpty(text)) {
                stringBuilder.append("<p>" + text);
            }
            if (!Strings.isNullOrEmpty(content)) {
                stringBuilder.append("<p>" + content + "<p>");
            }

            return stringBuilder.toString();
        }
    }

    private static ConsentSection.Type getTypeForComponent(Contract.TermComponent termComponent) {
        CodeableConcept concept = termComponent.getType();
        List<Coding> codings = concept.getCoding();
        for (Coding coding : codings) {
            if (coding.getSystem().toLowerCase().equals(kContractTermConsentSectionType.toLowerCase())) {
                switch (coding.getCode()) {
                    case "Overview":
                        return ConsentSection.Type.Overview;
                    case "Privacy":
                        return ConsentSection.Type.Privacy;
                    case "DataGathering":
                        return ConsentSection.Type.DataGathering;
                    case "DataUse":
                        return ConsentSection.Type.DataUse;
                    case "TimeCommitment":
                        return ConsentSection.Type.TimeCommitment;
                    case "StudySurvey":
                        return ConsentSection.Type.StudySurvey;
                    case "StudyTasks":
                        return ConsentSection.Type.StudyTasks;
                    case "Withdrawing":
                        return ConsentSection.Type.Withdrawing;
                    case "Custom":
                        return ConsentSection.Type.Custom;
                    case "OnlyInDocument":
                    default:
                        // TODO error handling
                }

            } else {
                Log.d(Logging.logTag, "ignoring consent section system " + coding.getSystem() + ". Expecting " + kContractTermConsentSectionType);
            }
        }
        return ConsentSection.Type.Custom;
    }

    /**
     * This method can be used to get a {@link FormStep} with the inclusion criteria to be used with
     * the skin framework of ResearchStack. Be aware that in this case all the criteria have to be
     * answered yes to be eligible, there are no explicit requirement checks!
     */
    public static Step getInclusionCriteriaStepFromContract(Context context, Contract contract) {
        FormStep eligibilityFormStep = new FormStep(ID_ELIGIBILITY_FORM_STEP, "", "");
        // Set items on FormStep
        eligibilityFormStep.setStepTitle(R.string.rss_eligibility);
        eligibilityFormStep.setOptional(false);

        BooleanAnswerFormat booleanAnswerFormat = new BooleanAnswerFormat("Yes", "No");

        List<Group.GroupCharacteristicComponent> characteristicComponents = ((Group) contract.getSubjectFirstRep().getResource()).getCharacteristic();
        List<QuestionStep> questionSteps = new ArrayList<>();

        int i = 0;
        for (Group.GroupCharacteristicComponent component : characteristicComponents) {
            String id = "eligibility question " + Integer.toString(i);
            QuestionStep questionStep = new QuestionStep(id, component.getCode().getText(), booleanAnswerFormat);
            questionSteps.add(questionStep);

            i++;
        }

        eligibilityFormStep.setFormSteps(questionSteps);

        return eligibilityFormStep;
    }

    /**
     * Returns a ConditionalOrderedTask including the FormStep with the eligibility questions and
     * an eligibilityAssessmentStep that adds the user's eligibility to the StepResult as a boolean
     */
    public Task getEligibilityTaskFromContract(Context context, Contract contract) {
        List<Step> steps = new ArrayList<>();

        // add Eligibility Steps
        initEligibilitySteps(context, steps, contract);
        ConditionalOrderedTask eligibilityTask = new ConditionalOrderedTask(ID_ELIGIBILITY_TASK, steps);
        return eligibilityTask;
    }

    /**
     * Returns a {@link ConditionalOrderedTask} that can be presented by a {@link ViewConsentTaskActivity}
     * checking the user's eligibility and getting their consent.
     */
    public static Task getConsentTaskFromContract(Context context, Contract contract, ConsentTaskOptions options) {
        ConsentDocument consentDocument = getConsentDocumentFromContract(context, contract, options);
        List<ConsentSection> sections = consentDocument.getSections();

        List<Step> steps = new ArrayList<>();

        // add Visual Consent Steps
        innitVisualSteps(context, steps, sections);

        // sharing step
        if (options.askForSharing()) {
            innitConsentSharingStep(context, steps, contract, options.getShareMoreInfoDocument());
        }

        // consent review step
        initConsentReviewSteps(context, steps, consentDocument);

        // set passcode step
        if (options.askToCreatePasscode()) {
            initPassCodeSteps(context, steps);
        }

        ConditionalOrderedTask orderedTask = new ConditionalOrderedTask(ID_CONSENT_TASK, steps);
        return orderedTask;
    }
}
