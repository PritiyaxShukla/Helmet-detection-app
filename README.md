# Bike Helmet Detection App
This is an Android app that uses computer vision to detect helmets, bikes, and number plates from images. The app interacts with a FastAPI backend, where the prediction model is hosted and performs real-time detection. The model was trained on a custom, self-annotated dataset to ensure accuracy.

# Features
Helmet Detection: Identifies if a bike rider is wearing a helmet.
Bike Detection: Detects the presence of a bike in the image.
Number Plate Detection: Detects vehicle number plates in the image.
FastAPI Backend: Utilizes FastAPI to handle image predictions efficiently and return results to the app.
Real-time Prediction: The app sends an image to the backend, which processes the image and returns results immediately for visualization.
Project Overview
The model used for detection was trained using YOLOV11 and fine-tuned on a custom dataset of bike helmets, bikes, and number plates. The model performance is evaluated using the following metrics:

mAP@50 (Mean Average Precision at IoU = 0.5): 69.9%
mAP@50-95 (Mean Average Precision averaged over IoU = 0.5 to 0.95): 43.6%

# Technologies Used
Mobile App: Android (Java/Kotlin)
Backend API: FastAPI
Deployment: Local and cloud-hosted FastAPI instance
Version Control: Git, GitHub


# API GITHUB LINK
https://github.com/PritiyaxShukla/BIKE_HELMET_DETECTION
