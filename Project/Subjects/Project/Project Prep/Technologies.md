## Research

### Camera Integration
- CameraX
	- CameraX is a jetpack library used to make development easier when working with a camera in android app development.  
	- CameraX works with Android5.0(API level 21) and higher. The current is [Android 15](https://www.howtogeek.com/345250/whats-the-latest-version-of-android/)
	- Common use cases for CameraX
		- Preview: View image on display
		- Image Analysis: Access a buffer seamlessly for use in algorithms
		- Image Capture: Save Images
		- Video Capture: Save Video and Audio
	- CameraX covers discrepancies between different cameras and their behavior. This includes, aspect ratio, orientation, rotation, preview size and image size. This is done by an automated CameraX test lab which run on an ongoing basis to fix a wide range of issues.
	- CameraX includes and Extension API allowing for extra features such as HDR and night mode. 

[Android Web](https://developer.android.com/media/camera/camerax)
[Video On CameraX](https://youtu.be/I4rDx90Nlus)
[Documentation](https://developer.android.com/media/camera/camerax#docs) 

### Image Processing
- **LiteRT**: Used for running machine learning models on-device, or integrate **Google Cloud Vision API** for image recognition and labeling. 
	- LiteRT is used for machine learning problems offering high flexibility and Customizability.
	- Key Features
		- Optimized for on device machine learning
			- Latency: no round trip to a server.
			- Privacy: Personal data does not leave device.
			- Connectivity: Internet connectivity is not required. 
			- Size: Reduced model for binary size.
			- Power Consumption: Efficient inference and lack of network connections. 
	- LiteRT is represented in a format known as [Flat Buffers.](https://flatbuffers.dev/)  
	- LiteRT can be used in the following ways
		- Using an existing LiteRT model: This is using a LiteRT model already in the `.tflite` format.
		- Convert into liteRT model: TensforFlow Converter can be used to convert models to FlatBuffers `.tflite` and run them in LiteRT.
	- A LiteRT model can optionally include metadata that contains human readable model descriptions and machine readable data for automatic generation of pre- and post-processing pipelines during on-device inference. 
	- LiteRT models can run inferences completely on mobile devices. 
	- LiteRT contains APIs for Kotlin.
	- You can improve hardware acceleration using GPU delegate.

[LiteRT Overview](https://ai.google.dev/edge/litert) 
[LiteRT On Android](https://developer.android.com/ai/custom) 
[On Device Training](https://ai.google.dev/edge/litert/models/ondevice_training) 

- **Google Cloud Vision API**: Allows the developers to easily integrate vision detection features within applications including image labeling, face and landmark detection, optical character recognition and tagging of explicit content. 
	- Has a cost attached: [pricing](https://cloud.google.com/vision/pricing) 

[Google Cloud Vision API Docs](https://cloud.google.com/vision/docs/) 

### AI Model
- **Tensorflow/Keras**: Use this to train a custom model for ingredient detection
	- Keras provides an approachable, highly-productive interface for solving ML problems. Keras covers everything from data processing to hyperparameter turning to deployment. 
		- Keras reduces work load by:
			- Offer simple and consistent interfaces.
			- Minimize the number of actions required for common use cases. 
			- Provide clear and actionable error messages. 
			- Follow the principle of progressive disclosure of complexity. 
			- Helps write concise readable code. 
[Guide for Keras](https://www.tensorflow.org/guide/keras) 
[Guide for training models using tensorflow](https://www.tensorflow.org/guide/keras/training_with_built_in_methods) 
[Convert from tensorflow to LiteRT](https://www.tensorflow.org/api_docs/python/tf/lite/TFLiteConverter) 
- **Yolo/MobileNet**: These are pre trained models
	- Yolo
		- Is an object detection algorithm that can be used to identify/detect multiple objects in images or videos with high speed and accuracy. 
		- It operate by breaking the image up into a grid and then having each cell of the grid responsible for detecting objects. 
		- the name Yolo stands for 'You Only Look Once' indicating towards one of its key features which is it preforms detection in a single pass instead of multiple stages. 
		- Highly useful for detecting multiple objects in complex images.
		- It has a smaller more compact version known as Tiny Yolo for mobile and emebeddd devices. 
	- MobileNet
		- MobileNet is a Convolutional Neural Network ([CNN](https://www.geeksforgeeks.org/introduction-convolution-neural-network/)) architecture optimized for mobile and embedded devices. 
		- The main uses for Mobilenet is image classification and feature extraction using minimal resources. 
		- It is lightweight and efficient as it uses [Depthwise Seperable Convolutions](https://towardsdatascience.com/understanding-depthwise-separable-convolutions-and-the-efficiency-of-mobilenets-6de3d6b62503) which reduced the number of parameters and operations. 
		- It can be paired with object detection frameworks like Single Shot Detector([SSD](https://towardsdatascience.com/review-ssd-single-shot-detector-object-detection-851a94607d11)) to perform detection on mobile devices.


| Models    | Main Use Case                               | Accuracy                                      | Speed           | Efficiency | Size  |
| --------- | ------------------------------------------- | --------------------------------------------- | --------------- | ---------- | ----- |
| Yolo      | Object Detection                            | High                                          | Fast(real-time) | Heavy      | Large |
| MobileNet | Image Classification/<br>Feature Extraction | Low for detection/<br>High for classification | Extremely Fast  | Light      | Small |
[Yolo documentation](https://docs.ultralytics.com/)
[Yolo with Keras](https://keras.io/examples/vision/yolov8/) 
[Mobilenet documentation](https://keras.io/api/applications/mobilenet/) 
[Mobilenet explanation](https://medium.com/@godeep48/an-overview-on-mobilenet-an-efficient-mobile-vision-cnn-f301141db94d) 

- **Google ML Kit**: ML Kit is a mobile SDK that brings googles on device machine learning expertise to Android. 
	- It allows for real-time use cases where you want you want to process images. 
	- is somewhat similar in terms of functionality to Yolo and Mobilenet, but it is a higher-level framework designed specifically for mobile applications, providing pre-built machine learning models for tasks like image recognition, object detection, text recognition, and more.
	- Unlike YOLO or MobileNet, which require you to implement or train models, ML Kit offers out-of-the-box machine learning. 
[ML Kit Guide](https://developers.google.com/ml-kit/guides) 
### Integration with Chat GPT
- **OpenAI API**: To analyze and describe ingredients based on image and also recommending healthier options. 
	- Comes with a cost for use. 
[OpenAI API Spec](https://platform.openai.com/docs/overview) 

### Barcode Scanning
- **ZXing**: An open source barcode scanning library can be used to scan barcodes from food items
	- ZXing ("zebra crossing") is an open-source, multi-format 1D/2D barcode image processing library implemented in Java, with ports to other languages.
[Zxing Documentation](https://github.com/zxing/zxing)

### Database for barcode lookup
- **Open Food Facts API**: A globally community run database for foods and their details. 
- **Edamam Food Database API**: A  professionally curated database for foods details and recipes. 

| Databases                | Coverage                       | Features                                     | Consistency                      | Cost |
| ------------------------ | ------------------------------ | -------------------------------------------- | -------------------------------- | ---- |
| Open Food Facts API      | Global coverage(Mainly Europe) | Nutritional info, eco-scores, product labels | May vary as users input the data | Free |
| Edamam Food Database API | Global Coverage(Mainly USA)    | Nutritional info, health labels, recipes     | Consistent and standardised      | Paid |

[Open Food Facts API Doc](https://openfoodfacts.github.io/openfoodfacts-server/api/) 
[Edamam Food Database API Docs](https://api.edamam.com/)
### Machine Learning Framework for Recipe Recommendations
- **LiteRT**: For on-device recipe recommendations

### Datasets
 - **Kaggles Recipe Ingredients Dataset**
	 - [Recipes](https://www.kaggle.com/datasets/paultimothymooney/recipenlg) 
	 - [Food Images](https://www.kaggle.com/datasets/kmader/food41) 
	 - [Alternatives](https://www.kaggle.com/datasets/thedevastator/the-nutritional-content-of-food-a-comprehensive) 
	 - [Allergies](https://www.kaggle.com/datasets/boltcutters/food-allergens-and-allergies)

### Recipe Recommendations APIs
- **Spoonacular API**: Offers a vast recipe database as well as food and ingredient information. 
	- It is a paid service. 
	- It has various details on food knowing to keep away from certain foods to avoid allergies.
	- Provides meal planning, grocery list generation, cost estimation. 
	- Allows recipes based on nutritional goals. 
	- Has extensive cooking details. 
[Spoonacular API Docs](https://spoonacular.com/food-api) 
### Machine Learning Framework for Shopping List Generation
- **Tensorflow: For training a model to predict frequently used ingredients and generate shopping lists based on historical data. 

### Data Storage
- **Firebase Firestore**: To store ingredient history and scanned items.
	- Is easily integrated with Kotlin
	- Provides SDKs that allow direct communication from android app to Firestore. 
	- Has real time synchronization.
	- Supports offline data access
[Firestore Docs](https://firebase.google.com/docs/firestore/) 
### Shopping List Feature
- **Koitlin List Management**

### Database Healthier Options API
- **Spoonacular**: Use these APIs to fetch nutritional information and suggest healthier food options. 

### Biometric Authentication 
- **BiometricAPI**: To scan users fingerprints. 

### Application Permissions

### User Data Security Encryption


### Possible Technologies
### APIs
- Nutritionix API: provides more detailed nutritional information about food items and restaurant meals.

