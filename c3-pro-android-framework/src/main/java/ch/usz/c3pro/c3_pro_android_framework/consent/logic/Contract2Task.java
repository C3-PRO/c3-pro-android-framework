package ch.usz.c3pro.c3_pro_android_framework.consent.logic;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.common.base.Strings;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Contract;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Organization;
import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BirthDateAnswerFormat;
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
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout;
import org.researchstack.skin.step.PassCodeCreationStep;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.usz.c3pro.c3_pro_android_framework.R;
import ch.usz.c3pro.c3_pro_android_framework.consent.ConsentTaskOptions;

/**
 * C3PRO
 * <p>
 * Created by manny Weber on 07/19/2016.
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

public class Contract2Task {
    public static final String LTAG = "LC3P";
    public static boolean skip = false;

    private static String kContractTermConsentSectionType = "http://researchkit.org/docs/Constants/ORKConsentSectionType";
    private static String kContractTermConsentSectionExtension = "http://fhir-registry.smarthealthit.org/StructureDefinition/ORKConsentSection";

    public static final String ID_CONSENT_TASK = "ID_CONSENT_TASK";
    public static final String ID_VISUAL = "ID_VISUAL";
    public static final String ID_FIRST_QUESTION = "question_1";
    public static final String ID_QUIZ_RESULT = "ID_QUIZ_RESULT";
    public static final String ID_SHARING = "ID_SHARING";
    public static final String ID_CONSENT_STEP = "public static final String";
    public static final String ID_CONSENT_DOC = "consent_review_doc";
    public static final String ID_FORM = "ID_FORM";
    public static final String ID_FORM_NAME = "ID_FORM_NAME";
    public static final String ID_FORM_DOB = "ID_FORM_DOB";
    public static final String ID_FORM_BIRTHDATE = "ID_FORM_BIRTHDATE";
    public static final String ID_SIGNATURE = "ID_SIGNATURE";
    public static final String ID_PASSCODE_INSTRUCTION ="ID_PASSCODE_INSTRUCTION";
    public static final String ID_PASSCODE_STEP ="ID_PASSCODE_STEP";

    public static Task Contract2Task(Context context, Contract contract, ConsentTaskOptions options) {
        ConsentDocument consentDocument = getConsentDocumentFromContract(context, contract, options);
        List<ConsentSection> sections = consentDocument.getSections();
        List<Step> steps = new ArrayList<>();

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

        /**
         //Eligibility
         if (contract.hasSubject()) {
         List<Reference> subjectReferences = contract.getSubject();
         for (Reference ref : subjectReferences) {

         }
         }
         */
        OrderedTask orderedTask = new OrderedTask(ID_CONSENT_TASK, steps);
        return orderedTask;
    }

    private static ConsentDocument getConsentDocumentFromContract(Context context, Contract contract, ConsentTaskOptions options) {
        // Create consent signature object and set what info is required
        // TODO with properties etc
        ConsentSignature signature = new ConsentSignature();
        signature.setRequiresName(options.requiresName);
        signature.setRequiresSignatureImage(options.requiresSignature);
        signature.setRequiresBirthDate(options.requiresBirthday);

        //TODO string localization etc.
        ConsentDocument document = new ConsentDocument();
        document.setTitle(context.getString(R.string.rsb_consent));
        document.setSignaturePageTitle(R.string.rsb_consent);

        String review = "no review doc";
        if (!Strings.isNullOrEmpty(options.getReviewConsentDocument())) {
            review = ResourcePathManager.getResourceAsString(context, "html/" + options.getReviewConsentDocument() + ".html");
        }
        document.setHtmlReviewContent(review);

        document.addSignature(signature);

        // Add contract terms as consent sections
        if (contract.hasTerm()) {
            List<ConsentSection> consentSections = new ArrayList<ConsentSection>();
            List<Contract.TermComponent> terms = contract.getTerm();
            for (Contract.TermComponent termComponent : terms) {
                consentSections.add(contractTerm2ConsentSection(context, termComponent));
            }
            document.setSections(consentSections);
            document.setHtmlReviewContent("what if no html found? sections?");

        } else {
            // TODO no terms error
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

        //TODO sharing learn more and teamname etc -> where from?

        ConsentSharingStep consentSharingStep = new ConsentSharingStep(ID_SHARING);
        consentSharingStep.setStepTitle(R.string.rsb_consent);
        consentSharingStep.setTitle(context.getString(R.string.rsb_consent_share_title));
        String learnMore = ResourcePathManager.getResourceAsString(context, "html/" + shareMoreInfoDocument + ".html");
        consentSharingStep.setText(learnMore);

        String teamName = ((Organization) contract.getAuthorityFirstRep().getResource()).getName();
        String shareWidely = context.getString(R.string.rsb_consent_share_widely, teamName);
        Choice<String> shareWidelyChoice = new Choice<>(shareWidely, "sponsors_and_partners", null);

        String shareRestricted = context.getString(R.string.rsb_consent_share_only, teamName);
        Choice<String> shareRestrictedChoice = new Choice<>(shareRestricted, "all_qualified_researchers", null);

        consentSharingStep.setAnswerFormat(new ChoiceAnswerFormat(AnswerFormat.ChoiceAnswerStyle.SingleChoice, shareWidelyChoice, shareRestrictedChoice));

        steps.add(consentSharingStep);
    }

    private static void initConsentReviewSteps(Context context, List<Step> steps, ConsentDocument document) {

        ConsentDocumentStep step = new ConsentDocumentStep(ID_CONSENT_STEP);
        //
        step.setStepTitle(R.string.rsb_consent_review_title);
        step.setText("this title will not appear!!! =(");
        //step.setTitle(context.getString(R.string.rsb_consent_review_title));

        StringBuilder docBuilder = new StringBuilder(
                "</br><div style=\"padding: 10px 10px 10px 10px;\" class='header'>");
        String title = context.getString(R.string.rsb_consent_review_title);
        docBuilder.append(String.format(
                "<h1 style=\"text-align: center; font-family:sans-serif-light;\">%1$s</h1>",
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
                Calendar maxDate = Calendar.getInstance();
                maxDate.add(Calendar.YEAR, -18);
                DateAnswerFormat dobFormat = new BirthDateAnswerFormat(null, 18, 0);
                String dobText = context.getString(R.string.rsb_consent_dob_full);
                formSteps.add(new QuestionStep(ID_FORM_DOB, dobText, dobFormat));
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

    private static ConsentSection contractTerm2ConsentSection(Context context, Contract.TermComponent termComponent) {
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
                    //TODO loading html file
                    String htmlContent = ResourcePathManager.getResourceAsString(context, "html/" + extension.getValue().primitiveValue() + ".html");

                    consentSection.setHtmlContent(htmlContent);
                    break;
                case "image":
                    //TODO image
                    break;
                case "animation":
                    //TODO animation
                    break;
                default:
                    Log.d(LTAG, "no matching extention");
            }
        }
        return consentSection;
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
                Log.d(LTAG, "ignoring consent section system " + coding.getSystem() + ". Expecting " + kContractTermConsentSectionType);
            }
        }
        return ConsentSection.Type.Custom;
    }
}

