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

### GPT Research
#### Machine Learning 
### **1. Reinforcement Learning (Already Mentioned)**

- **Purpose**: In the context of your app, reinforcement learning can be used to **optimize recommendations** based on feedback from users. For example, as users select or reject suggested recipes, the model can learn and adapt to provide better recommendations over time.
- **Use Case**: Continuous learning and improvement in recipe suggestions based on user actions (e.g., a user makes a dish, likes/dislikes it, etc.).

### **2. Collaborative Filtering (Already Mentioned)**

- **Purpose**: Collaborative filtering is commonly used in recommendation systems by finding patterns in user behavior. It uses similarities between users or items (e.g., recipes) to make recommendations.
- **Use Case**: Suggesting recipes to users based on what other users with similar preferences have chosen or liked in the past.

### **3. Content-Based Filtering** (Complement to Collaborative Filtering)

- **Purpose**: Content-based filtering recommends items based on the properties of the items themselves (e.g., recipe ingredients, cuisine type, nutritional content). The model looks at features of past liked recipes and suggests similar ones.
- **Use Case**: Recommending recipes with ingredients similar to what the user has previously enjoyed or based on specific dietary preferences.
- **Example**: If a user likes pasta dishes, the system can recommend recipes with similar ingredients (e.g., pasta, tomatoes, garlic).

### **4. Matrix Factorization**

- **Purpose**: Matrix factorization is a technique commonly used for recommendation systems, especially with collaborative filtering. It factors a large matrix (e.g., users vs. recipes) into smaller matrices that represent latent features of users and items.
- **Use Case**: This helps in making better recommendations when explicit user-item ratings are not available by finding hidden patterns in the data.
- **Example**: Predicting the likelihood of a user liking a recipe based on sparse interactions with recipes or other users.

### **5. Natural Language Processing (NLP)** for Recipe and Ingredient Analysis

- **Purpose**: NLP techniques can be used to understand the content of recipes and ingredients. This could be helpful if you’re analyzing textual descriptions of recipes (e.g., parsing instructions or identifying allergens).
- **Use Case**: Extracting information from recipe descriptions, such as cooking methods, ingredient pairings, and user reviews.
- **Example**: Parsing recipes to identify key ingredients and suggesting alternatives for dietary restrictions (e.g., replacing dairy in a recipe for lactose-intolerant users).

### **6. Association Rule Learning** (e.g., Apriori or FP-Growth)

- **Purpose**: Association rule learning discovers relationships between items in large datasets. This is particularly useful for identifying patterns in shopping behavior or ingredient combinations.
- **Use Case**: Generating shopping list suggestions based on common ingredient pairings or frequent co-occurrence of ingredients in recipes.
- **Example**: If users frequently buy flour and sugar together, the system might suggest sugar when the user adds flour to their shopping list.

### **7. Clustering Algorithms** (e.g., K-Means, Hierarchical Clustering)

- **Purpose**: Clustering is useful for grouping similar users or recipes together. This could help segment users based on their taste profiles or cluster recipes based on ingredients or cooking style.
- **Use Case**: Grouping users with similar preferences to provide more tailored recommendations or identifying types of recipes (e.g., healthy, quick, gourmet).
- **Example**: Grouping recipes into clusters such as "quick meals," "vegetarian," or "high-protein" and recommending them based on user preferences.

### **8. Neural Networks and Deep Learning** (e.g., Feedforward Neural Networks, RNNs)

- **Purpose**: Neural networks can handle large datasets and complex patterns in data. For recommendations, deep learning models (e.g., **autoencoders**, **RNNs**) can capture intricate relationships between users and recipes.
- **Use Case**: Neural networks could improve recipe recommendations by understanding complex user behaviors and preferences beyond what collaborative filtering or content-based systems could handle.
- **Example**: A deep learning model could predict a user’s recipe preference by learning from both user behavior and recipe attributes.

### **9. Hybrid Models (Combining Techniques)**

- **Purpose**: Hybrid models combine multiple recommendation techniques (e.g., collaborative filtering + content-based filtering) to leverage the strengths of each.
- **Use Case**: Improve the accuracy of recipe recommendations by combining user similarity (collaborative filtering) with content similarity (content-based filtering).
- **Example**: Recommending a recipe that is both similar to the user’s past choices and popular among similar users.