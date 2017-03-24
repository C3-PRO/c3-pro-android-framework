Questionnaire
-------------
The Questionnaire module can take a FHIR `Questionnaire` to conduct a survey rendered by ResearchStack and return a FHIR `QuestionnaireResponse`.
The class also sets up a `JobManager` in order to perform background tasks, this is why it has to be initialized with a Context.

### Module Interface

IN
- FHIR `Questionnaire`

OUT
- FHIR `QuestionnaireResponse`

##### ViewQuestionnaireTaskActivity

Although the `ViewQuestionnaireTaskActivity` is not a subclass of the ViewTaskActivity, it does work in the same way. It can be set up with a FHIR `Questionnaire` and then be shown to the user through the `startActivityForResult` method:

```java
    private void launchSurvey(Questionnaire questionnaire) {

        Intent intent = ViewQuestionnaireTaskActivity.newIntent(this, questionnaire);

        startActivityForResult(intent, 222);
    }
```

When the activity returns, the `QuestionnaireResponse` can be read from the Extras:

```java
@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_OK) {

            switch (requestCode) {
               case 222:
                    QuestionnaireResponse response = (QuestionnaireResponse) data.getExtras().get(ViewQuestionnaireTaskActivity.EXTRA_QUESTIONNAIRE_RESPONSE);
                    printQuestionnaireAnswers(response);
                    break;
            }
        } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {

        }
    }
```

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
