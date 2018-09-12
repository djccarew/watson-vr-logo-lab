package com.ibm.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.util.HttpLogging;

import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;


// This Watson Java SDK example uses the Visual Recognition  service to test a custom
// classifier trained to recognize company logos. The companion file settings.properties is where the // service credentials, custom classifier id  and location of the test images are provided to
// this application
public class WatsonVRTester {

public static void main(String[] args) throws IOException {

        // Getting required properties
        // API_KEY, TEST_IMAGE_DIR and CLASSIFIER_ID
        final Properties properties = new Properties();
        try (final InputStream stream = WatsonVRTester.class.getResourceAsStream("/settings.properties")) {
                properties.load(stream);
        }

        // Exit if properties not present
        if (properties.getProperty("API_KEY") == null || properties.getProperty("CLASSIFIER_ID") == null
            || properties.getProperty("TEST_IMAGE_DIR") == null) {
                System.err.println("Error: API_KEY, CLASSIFIER_ID and/or TEST_IMAGES_DIR missing. Terminating ...");
                System.exit(1);
        }

        // Create service client
        IamOptions iamOps = new IamOptions.Builder()
                            .apiKey(properties.getProperty("API_KEY"))
                            .build();
        VisualRecognition service = new VisualRecognition("2018-03-19", iamOps);
        service.setEndPoint("https://gateway.watsonplatform.net/visual-recognition/api");


        // Turn off HTTP logging
        // Default is BASIC which  logs all HTTP requests to the Watson services
        // Commenting out these 2 lines will revert to  the  default behavior
        HttpLoggingInterceptor httpLogger = HttpLogging.getLoggingInterceptor();
        httpLogger.setLevel(Level.NONE);


        // Testing stats
        int truePositives = 0;
        int falsePositives = 0;
        int trueNegatives = 0;
        int falseNegatives = 0;
        int filesProcessed = 0;

        // Save false positives and false negatives so they can be displkayed at the end
        List<String> falseNegativesList = new ArrayList<String>();
        List<String> falsePositivesList = new ArrayList<String>();

        // Get all jog files in test image folder
        File dir = new File(properties.getProperty("TEST_IMAGE_DIR"));
        String[] extensions = new String[] { "jpg", "png" };

        if (!dir.isDirectory()) {
                dir = new File(properties.getProperty("TEST_DATA_DIR").replaceAll("\\.\\.", ""));
        }

        List<File> files = (List<File>)FileUtils.listFiles(dir, extensions, true);

        System.out.println("Classifying " + files.size() + " test images");

        // Classify each file
        for (File file : files) {
                ClassifyOptions options = new ClassifyOptions.Builder().imagesFile(file)
                                          .addClassifierId(properties.getProperty("CLASSIFIER_ID")).threshold(0.60f).build();
                ClassifiedImages result = service.classify(options).execute();
                ClassifiedImage image = result.getImages().get(0);
                ClassifierResult classifier  = image.getClassifiers().get(0);
                List<ClassResult> classes = classifier.getClasses();
                ClassResult classResult = null;

                // Handle test images that are not wedding photos
                if (file.getPath().contains("other")) {
                        // true negative because it isn't a wedding photo and classifier thinks it isn't
                        // one too
                        if (classes.isEmpty()) {
                                ++trueNegatives;
                        }
                        // false positive because it's not a wedding photo but classifier thinks it is
                        else {
                                classResult = classes.get(classes.size() - 1);
                                System.out.println("False positive: " + file.getCanonicalPath() + " score: " + classes.get(0).getScore() + " class: " + classResult.getClassName());
                                ++falsePositives;
                                falsePositivesList.add(file.getCanonicalPath());
                        }

                }
                // Handle test images that are actual wedding photos
                else if (file.getPath().contains("morgan_stanley")){
                        // false negative because it is a wedding photo but classifier thinks it isn't
                        if (classes.isEmpty()) {
                                System.out.println("False negative: " + file.getCanonicalPath() + " no class matched");
                                ++falseNegatives;
                                falseNegativesList.add(file.getCanonicalPath());
                        }
                        // true positive because it is a wedding photo and classifier thinks it is too
                        else {
                                classResult = classes.get(classes.size() - 1);
                                if (!classResult.getClassName().equals("morgan_stanley")) {
                                      System.out.println("False negative: " + file.getCanonicalPath() + " score: " + classes.get(0).getScore() + " class: " + classResult.getClassName());
                                      ++falseNegatives;
                                      falseNegativesList.add(file.getPath());
                                }
                                else
                                   ++truePositives;
                        }
                }
                else if (file.getPath().contains("ibm")){
                        // false negative because it is a wedding photo but classifier thinks it isn't
                        if (classes.isEmpty()) {
                                System.out.println("False negative: " + file.getPath() + " no class matched");
                                ++falseNegatives;
                                falseNegativesList.add(file.getPath());
                        }
                        // true positive because it is a wedding photo and classifier thinks it is too
                        else {
                                classResult = classes.get(classes.size() - 1);
                                if (!classResult.getClassName().equals("ibm")) {
                                      System.out.println("False negative: " + file.getPath() + " score: " + classes.get(0).getScore() + " class: " + classResult.getClassName());
                                      ++falseNegatives;
                                      falseNegativesList.add(file.getPath());
                                }
                                else
                                   ++truePositives;
                        }
                }

                else if (file.getPath().contains("apple")){
                        // false negative because it is a wedding photo but classifier thinks it isn't
                        if (classes.isEmpty()) {
                                System.out.println("False negative: " + file.getPath() + " no class matched");
                                ++falseNegatives;
                                falseNegativesList.add(file.getPath());
                        }
                        // true positive because it is a wedding photo and classifier thinks it is too
                        else {
                                classResult = classes.get(classes.size() - 1);
                                if (!classResult.getClassName().trim().equals("apple")) {
                                      System.out.println("False negative: " + file.getPath() + " score: " + classes.get(0).getScore() + " class:***" + classResult.getClassName()+"***");
                                      ++falseNegatives;
                                      falseNegativesList.add(file.getPath());
                                }
                                else
                                   ++truePositives;
                        }
                }


                if (++filesProcessed % 10 == 0)
                        System.out.println("Finished classifying " + filesProcessed + " images");
        }

        // Output final stats and names of misclassified images
        System.out.println("Number of files " + filesProcessed);
        System.out.println("True positives " + truePositives);
        System.out.println("True negatives " + trueNegatives);
        System.out.println("False positives " + falsePositives);
        System.out.println("False negatives " + falseNegatives);

        float accuracy = (truePositives + trueNegatives) / (float) filesProcessed;
        System.out.println("Accuracy " + String.format("%.2f", accuracy * 100));

        if (falsePositivesList.size() > 0) {
                System.out.println("False positive list");
                for (String falsePositiveImageName : falsePositivesList) {
                        System.out.println(falsePositiveImageName);

                }
        }

        if (falseNegativesList.size() > 0) {
                System.out.println("False negative list");
                for (String falseNegativeImageName : falseNegativesList) {
                        System.out.println(falseNegativeImageName);

                }
        }

}
}
