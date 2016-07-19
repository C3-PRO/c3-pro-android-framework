Questionnaire
-------------
The Questionnaire module can take a FHIR `Questionnaire` to conduct a survey rendered by ResearchStack and return a FHIR `QuestionnaireResponse`.
The class also sets up a `JobManager` in order to perform background tasks, this is why it has to be initialized with a Context.

### Module Interface

IN
- FHIR `Questionnaire`
OUT
- FHIR `QuestionnaireResponse`


##### QuestionnaireFragment

The `QuestionnaireFragment` can be used to represent a Questionnaire and conduct a Survey based on it.
```java
private void launchSurvey(Questionnaire questionnaire) {
        /**
         * Looking up if a fragment for the given questionnaire has been created earlier. if so,
         * the survey is started, assuming that the TaskViewActivity has been created before!!
         * The questionnaire IDs are used for identification, assuming they are unique.
         * */
        QuestionnaireFragment fragment = (QuestionnaireFragment) getSupportFragmentManager().findFragmentByTag(questionnaire.getId());
        if (fragment != null) {
            /**
             * If the fragment has been added before, the TaskViewActivity can be started directly,
             * assuming that it was prepared right after the fragment was created.
             * */
            fragment.startTaskViewActivity();
        } else {
            /**
             * If the fragment does not exist, create it, add it to the fragment manager and
             * let it prepare the TaskViewActivity
             * */
            final QuestionnaireFragment questionnaireFragment = new QuestionnaireFragment();
            questionnaireFragment.newInstance(questionnaire, new QuestionnaireFragment.QuestionnaireFragmentListener() {
                @Override
                public void whenTaskReady(String requestID) {
                    /**
                     * Only when the task is ready, the survey is started
                     * */
                    questionnaireFragment.startTaskViewActivity();
                }

                @Override
                public void whenCompleted(String requestID, QuestionnaireResponse questionnaireResponse) {
                    /**
                     * Where the response for a completed survey is received. In the sample app it is printed
                     * to a TextView defined in the app layout.
                     * */
                    printQuestionnaireAnswers(questionnaireResponse);
                }

                @Override
                public void whenCancelledOrFailed() {
                    /**
                     * If the task can not be prepared, a backup plan is needed.
                     * Here the fragment is removed from the FragmentManager so it can be created
                     * again later
                     * TODO: proper error handling not yet implemented
                     * */
                    getSupportFragmentManager().beginTransaction().remove(questionnaireFragment).commit();
                }
            });

            /**
             * In order for the fragment to get the context and be found later on, it has to be added
             * to the fragment manager.
             * */
            getSupportFragmentManager().beginTransaction().add(questionnaireFragment, questionnaire.getId()).commit();
            /**
             * prepare the TaskViewActivity. As defined above, it will start the survey once the
             * TaskViewActivity is ready.
             * */
            questionnaireFragment.prepareTaskViewActivity();
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