import sys
import json
import glob
import os
from watson_developer_cloud import VisualRecognitionV3
import settings


def main():

   if not hasattr(settings, 'API_KEY') or not hasattr(settings, 'CLASSIFIER_ID') or not hasattr(settings, 'TEST_IMAGES_DIR'):
      print("Error: API_KEY, CLASSIFIER_ID and/or TEST_IMAGES_DIR missing. Terminating ...")
      sys.exit(1)
   else:
      api_key =  settings.API_KEY
      classifier_id = settings.CLASSIFIER_ID
      test_images_dir = settings.TEST_IMAGES_DIR
      if hasattr(settings, 'API_VERSION'):
         api_version = settings.API_VERSION
      else:
         api_version = '2018-03-19'

   #parameters = json.dumps({'threshold': 0.72, 'owners': ['me'],'classifier_ids': [classifier_id]})
   parameters = json.dumps({'threshold': 0.72})
   true_positives = 0
   false_positives = 0
   true_negatives = 0
   false_negatives = 0
   files_processed = 0
   false_negative_list = []
   false_positive_list = []
   visual_recognition = VisualRecognitionV3(api_version, iam_api_key=api_key)

   test_files = glob.glob(test_images_dir + '/**/*.???', recursive=True)
   print('Classifying %d test images ...' % (len(test_files)))
   for filename in  test_files:
        with open(os.path.abspath(filename), 'rb') as image_file:
            results = visual_recognition.classify(images_file=image_file, threshold='0.6', classifier_ids=classifier_id)
            if '/other/' in filename:
                #print(results["images"][0]["classifiers"][0])
                if not results["images"][0]["classifiers"][0]["classes"]:
                    true_negatives += 1
                else:
                    print('False positive: ' + filename + ' ' + str(results["images"][0]["classifiers"][0]["classes"][0]["score"]))
                    false_positives += 1
                    false_positive_list.append(filename + ' classified as ' + str(results["images"][0]["classifiers"][0]["classes"][0]["class"]))
            elif '/morgan_stanley/' in filename:
                if results["images"][0]["classifiers"][0]["classes"]:
                    if next((item for item in results["images"][0]["classifiers"][0]["classes"] if item["class"] == "morgan_stanley"), None):
                    #if results["images"][0]["classifiers"][0]["classes"][-1]["class"] == "morgan_stanley")):
                        true_positives += 1
                    else:
                        false_negatives += 1
                        false_negative_list.append(filename)
                else:
                    false_negatives += 1
                    false_negative_list.append(filename)
            elif '/ibm/' in filename:
                if results["images"][0]["classifiers"][0]["classes"]:
                    if next((item for item in results["images"][0]["classifiers"][0]["classes"] if item["class"] == "ibm"), None):
                    #if results["images"][0]["classifiers"][0]["classes"][-1]["classifier_id"] == "ibm"), None):
                        true_positives += 1
                    else:
                        false_negatives += 1
                        false_negative_list.append(filename)
                else:
                    false_negatives += 1
                    false_negative_list.append(filename)

            elif '/apple/' in filename:
                if results["images"][0]["classifiers"][0]["classes"]:
                    if next((item for item in results["images"][0]["classifiers"][0]["classes"] if item["class"] == "apple"), None):
                        true_positives += 1
                    else:
                        false_negatives += 1
                        false_negative_list.append(filename)
                else:
                    false_negatives += 1
                    false_negative_list.append(filename)

        files_processed += 1
        if (files_processed % 10) == 0:
            print("Finished classifying  {:d} images".format(files_processed))

   print("Number of files {:d}".format(len(test_files)))
   print("True positives {:d}".format(true_positives))
   print("True negatives {:d}".format(true_negatives))
   print("False positives {:d}".format(false_positives))
   print("False negatives {:d}".format(false_negatives))
   accuracy = (true_positives + true_negatives)/len(test_files)
   print("Accuracy {:6.2f}%".format(accuracy*100))

   if false_positive_list:
      print("False positive list")
      for name in false_positive_list:
          print(name)

   if false_negative_list:
      print("False negative list")
      for name in false_negative_list:
          print(name)

if __name__ == "__main__":
   main()
