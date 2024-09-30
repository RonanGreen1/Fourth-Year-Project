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
			- Power Consumption: Efficient inference and lack of nertwork connections. 
	- LiteRT is represented in a format known as [Flat Buffers.](https://flatbuffers.dev/)  

[LiteRT Overview](https://ai.google.dev/edge/litert) 
[LiteRT On Android](https://developer.android.com/ai/custom) 
- **Google Cloud Vision API** 

### AI Model
- **Tensorflow/Keras**: Use this to train a custom model for ingredient detection
- **Yolo/MobileNet**: These are pre trained models

### Integration with Chat GPT
- **OpenAI API**: To analyze and describe ingredients based on image and also recommending healthier options. 

### Barcode Scanning
- **Google ML Kit**
- **ZXing**: An open source barcode scanning library can be used to scan barcodes from food items

### Database for barcode lookup
- **Open Food Facts API**
- **Edamam Food Database API**

### Machine Learning Framework for Recipe Recommendations
- **Tensorflow lite**: For on-device recipe recommendations
- **Google AutoML**: For training recommendation model

### Datasets
 - **Food.com Recipes Dataset**
 - **Kaggles Recipe Ingredients Dataset**

### Recipe Recommendations APIs
- **Spoonacular API**
- **Edamam API**

### Machine Learning Framework for Shopping List Generation
- **Tensorflow/Scikit-Learn**: For training a model to predict frequently used ingredients and generate shopping lists based on historical data. 

### Data Storage
- **Firebase Firestore**: To store ingredient history and scanned items.

### Shopping List Feature
- **Koitlin List Management**

### Machine Learning Healthier Option APIs
- **Reinforcement Learning/Collaborative Filtering**: Use these techniques with **Tensorflow Lite** to suggest healthier food options. 

### Database Healthier Options API
- **Spoonacular/Nutritionix**: Use these APIs to fetch nutritional information and suggest healthier food options. 

### Biometric Authentication 
- **BiometricAPI**: To scan users fingerprints. 

### Application Permissions

### User Data Security Encryption
