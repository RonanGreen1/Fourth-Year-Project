### Camera X
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

### Lite RT
- **LiteRT**: Used for running machine learning models on-device.
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

### TensorFlow/Keras
- **Tensorflow/Keras**: Use this to train a custom model for ingredient detection
	- Keras provides an approachable, highly-productive interface for solving ML problems. Keras covers everything from data processing to hyperparameter turning to deployment. 
		- Keras reduces work load by:
			- Offer simple and consistent interfaces.
			- Minimize the number of actions required for common use cases. 
			- Provide clear and actionable error messages. 
			- Follow the principle of progressive disclosure of complexity. 
			- Helps write concise readable code. 
		-  Keras Applications are deep learning models that are made available alongside pre-trained weights. These models can be used for prediction, feature extraction, and fine-tuning.

[Guide for Keras](https://www.tensorflow.org/guide/keras) 
[Guide for training models using tensorflow](https://www.tensorflow.org/guide/keras/training_with_built_in_methods) 
[Convert from tensorflow to LiteRT](https://www.tensorflow.org/api_docs/python/tf/lite/TFLiteConverter) 

#### MobileNet
- MobileNet
	- MobileNet is a Convolutional Neural Network ([CNN](https://www.geeksforgeeks.org/introduction-convolution-neural-network/)) architecture optimized for mobile and embedded devices.
	- The main uses for Mobilenet is image classification and feature extraction using minimal resources.
	- It is lightweight and efficient as it uses [Depthwise Seperable Convolutions](https://towardsdatascience.com/understanding-depthwise-separable-convolutions-and-the-efficiency-of-mobilenets-6de3d6b62503) which reduced the number of parameters and operations.
	- It can be paired with object detection frameworks like Single Shot Detector([SSD](https://towardsdatascience.com/review-ssd-single-shot-detector-object-detection-851a94607d11)) to perform detection on mobile devices.
	- mobilenet is trained off ImageNet 

[Mobilenet documentation](https://keras.io/api/applications/mobilenet/) 
[Mobilenet explanation](https://medium.com/@godeep48/an-overview-on-mobilenet-an-efficient-mobile-vision-cnn-f301141db94d) 

### Chat GPT
- **OpenAI API**: To analyze and describe ingredients based on image and also recommending healthier options. 
	- Comes with a cost for use. 
[OpenAI API Spec](https://platform.openai.com/docs/overview) 

### ZXing
- **ZXing**: An open source barcode scanning library can be used to scan barcodes from food items
	- ZXing ("zebra crossing") is an open-source, multi-format 1D/2D barcode image processing library implemented in Java, with ports to other languages.
[Zxing Documentation](https://github.com/zxing/zxing) 

### Open FoodFacts API
- A globally community run database for foods and their details. 

[Open Food Facts API Doc](https://openfoodfacts.github.io/openfoodfacts-server/api/) 

### Datasets
 - **Kaggles Recipe Ingredients Dataset**
	 - [Recipes](https://www.kaggle.com/datasets/paultimothymooney/recipenlg) 
	 - [Food Images](https://www.kaggle.com/datasets/kmader/food41) 
	 - [Alternatives](https://www.kaggle.com/datasets/thedevastator/the-nutritional-content-of-food-a-comprehensive) 
	 - [Allergies](https://www.kaggle.com/datasets/boltcutters/food-allergens-and-allergies) 

### Spoonacular API
- **Spoonacular API**: Offers a vast recipe database as well as food and ingredient information. 
	- It is a paid service. 
	- It has various details on food knowing to keep away from certain foods to avoid allergies.
	- Provides meal planning, grocery list generation, cost estimation. 
	- Allows recipes based on nutritional goals. 
	- Has extensive cooking details. 
[Spoonacular API Docs](https://spoonacular.com/food-api) 

### Firebase
- **Firebase Firestore**: To store ingredient history and scanned items.
	- Is easily integrated with Kotlin
	- Provides SDKs that allow direct communication from android app to Firestore. 
	- Has real time synchronization.
	- Supports offline data access
	-  Benefits a less structured database which is needed for storying recipes. 
	- Better for scaling 
[Firestore Docs](https://firebase.google.com/docs/firestore/) 