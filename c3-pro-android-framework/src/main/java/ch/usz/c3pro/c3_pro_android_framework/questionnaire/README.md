Questionnaire
-------------
The Questionnaire module can take a FHIR `Questionnaire` to conduct a survey rendered by ResearchStack and return a FHIR `QuestionnaireResponse`.
The class also sets up a `JobManager` in order to perform background tasks, this is why it has to be initialized with a Context.

### Module Interface

IN
- FHIR `Questionnaire`
OUT
- FHIR `QuestionnaireResponse`

##### QuestionnaireAdapter

This ArrayAdapter can be used to directly display FHIR `Questionnaire`s in a `ListView`. The questionnaire's ID is used as display string.

```java
        /**
        * Create the Adapter and set up the ListView declared in the layout xml to use it
        */
        ArrayAdapter<Questionnaire> questionnaireAdapter = new QuestionnaireAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<Questionnaire>());
        final ListView surveyListView = (ListView) findViewById(R.id.survey_list);
        surveyListView.setAdapter(questionnaireAdapter);

        /**
        * What happens when a questionnaire in the list is clicked
        */
        surveyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchSurvey((Questionnaire) surveyListView.getItemAtPosition(position));
            }
        });
        
        /**
        * Then add questionnaires to the ListView
        */
        questionnaireAdapter.add(questionnaire);
```
