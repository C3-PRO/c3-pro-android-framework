package ch.usz.c3pro.c3_pro_android_framework.questionnaire;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.hl7.fhir.dstu3.model.Questionnaire;

import java.util.ArrayList;

/**
 * C3-PRO
 *
 * Created by manny Weber on 06/27/2016.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This Adapter can be used to add an Array of Questionnaires directly to a ListView.
 * The id of the Questionnaires will be used for display.
 */
public class QuestionnaireAdapter extends ArrayAdapter<Questionnaire> {

    private static class ViewHolder {
        private TextView itemView;
    }

    public QuestionnaireAdapter(Context context, int textViewResourceId, ArrayList<Questionnaire> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {

            convertView = LayoutInflater.from(this.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);

            viewHolder = new ViewHolder();

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            ViewGroup.LayoutParams params = textView.getLayoutParams();
            params.height = 100;
            textView.setLayoutParams(params);
            viewHolder.itemView = textView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Questionnaire item = getItem(position);
        if (item != null) {
            if (!Strings.isNullOrEmpty(item.getId())) {
                viewHolder.itemView.setText(item.getId().replace("Questionnaire/", ""));
            } else {
                viewHolder.itemView.setText("Questionnaire with no ID");
            }
        }
        return convertView;
    }
}