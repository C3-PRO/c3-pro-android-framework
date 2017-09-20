package ch.usz.c3pro.c3_pro_android_framework.pyromaniac.logic.consent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.researchstack.backbone.result.TaskResult;
import org.researchstack.skin.task.ConsentTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.usz.c3pro.c3_pro_android_framework.R;


/**
 * Created by manny on 3/26/2017.
 */

public class CreateConsentPDF {

    public static void createPDFfromHTML(Context context, String consentHTML, TaskResult taskResult, String pathToPDF) {
        if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {

            // open a new document
            PrintAttributes printAttributes = new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
            PrintedPdfDocument document = new PrintedPdfDocument(context, printAttributes);

            // start a page
            PdfDocument.Page page = document.startPage(0);

            // draw something on the page
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20,20,20,20);

            TextView content = new TextView(context);
            content.setVisibility(View.VISIBLE);
            content.setWidth(page.getCanvas().getWidth()-40);
            content.setTextSize(new Float(5));
            String[] stringParts = consentHTML.split("signature_here");

            content.setText(Html.fromHtml(stringParts[0], Html.FROM_HTML_MODE_COMPACT));
            layout.addView(content);


            RelativeLayout relLayout = new RelativeLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2f);
            relLayout.setLayoutParams(layoutParams);
            relLayout.setGravity(Gravity.LEFT);

            ImageView imageView = new ImageView(context);

            String signatureEncodeBase64 = (String) taskResult.getStepResult(ConsentTask.ID_SIGNATURE).getResultForIdentifier("ConsentSignatureStep.Signature");
            byte[] decodedString = Base64.decode(signatureEncodeBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            imageView.setImageBitmap(decodedByte);
            relLayout.addView(imageView);
            layout.addView(relLayout);

            if (stringParts.length > 1){
                TextView content2 = new TextView(context);
                content2.setVisibility(View.VISIBLE);
                content2.setWidth(page.getCanvas().getWidth()-40);
                content2.setTextSize(new Float(5));
                content2.setText(Html.fromHtml(stringParts[1], Html.FROM_HTML_MODE_COMPACT));
                layout.addView(content2);
            }

            layout.measure(page.getCanvas().getWidth(), page.getCanvas().getHeight());
            layout.layout(0, 0, page.getCanvas().getWidth(), page.getCanvas().getHeight());


            layout.draw(page.getCanvas());


            // finish the page
            document.finishPage(page);

            // add more pages

            // write the document content
            try {
                //String outpath = Environment.getExternalStorageDirectory() + "/consent§.pdf";
                OutputStream file = new FileOutputStream(new File(pathToPDF));


                document.writeTo(file);

                //close the document
                document.close();
                Log.d("LOG", "pdf saved to " + pathToPDF);


            } catch (IOException e) {
                Log.d("error", "testPdf: " + e);
            }

        }
    }
}