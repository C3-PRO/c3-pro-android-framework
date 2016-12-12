Pyromaniac
-------
This module contains the logic to make ResearchStack work with FHIR with the help of HAPI. 
Pyro is available as a singleton and can be used to get the HAPI FhirContext and, for example, access the tools to create ResearchStack Tasks from FHIR Questionnaires:

```java
FhirContext fhirContext = Pyro.getFhirContext();
//or
Task questionnaireTask = Pyro.getQuestionnaireAsTask(questionnaire);
```

Usually, accessing Pyro directly is not necessary. The Questionnaire and Consent modules do the work for you.
If you use HAPI for other things in your app, accessing the FhirContext as a singleton through Pyro can help save some memory and processing time!
