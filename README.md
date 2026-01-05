# 📱 AI-Based Gadget Price Prediction Android App

An Android application that predicts the **price of Mobile Phones, Laptops, and Tablets** using **Machine Learning models (TensorFlow Lite)**.  
The app supports **user authentication**, **on-device ML inference**, and a clean **Material Design UI**.

---

## 🚀 Features

- 🔐 User Registration & Login (Firebase Authentication)
- 💾 Remember Me (SharedPreferences)
- 🧠 On-device Machine Learning (TensorFlow Lite)
- 📱 Mobile Price Prediction
- 💻 Laptop Price Prediction
- 📟 Tablet Price Prediction
- ⚡ Fast & Offline Predictions
- 🎨 Material UI with CardView & Toolbar
- 🚪 Logout functionality

---

## 🧠 Technologies Used

### Android
- Java
- XML
- Android Studio
- Material Components
- SharedPreferences

### Machine Learning
- Python
- Pandas, NumPy
- Scikit-learn
- TensorFlow / Keras
- TensorFlow Lite (.tflite)

### Backend / Services
- Firebase Authentication

---app/
├── java/com/example/prctc/
│ ├── SplashActivity.java
│ ├── LoginActivity.java
│ ├── RegisterActivity.java
│ ├── MainActivity.java
│ ├── LaptopPredictorActivity.java
│ ├── MobilePredictorActivity.java
│ ├── TabletPredictorActivity.java
│ └── SharedPrefManager.java
│
├── res/layout/
│ ├── activity_splash.xml
│ ├── activity_login.xml
│ ├── activity_register.xml
│ ├── activity_main.xml
│ ├── activity_laptop_predictor.xml
│ ├── activity_mobile_predictor.xml
│ └── activity_tablet_predictor.xml
│
├── assets/
│ ├── laptop_price_model.tflite
│ ├── mobile_price_model.tflite
│ └── tablet_price_model.tflite


---

## 🔄 Application Flow

1. **Splash Screen**
   - Checks login session
   - Redirects to Login or Main Activity

2. **Authentication**
   - Login using email & password
   - Register new user
   - Remember Me support

3. **Main Dashboard**
   - Select gadget type (Mobile / Laptop / Tablet)
   - Logout option

4. **Prediction Screens**
   - Enter device specifications
   - Run TensorFlow Lite model
   - Display predicted price

---

## 🧮 Machine Learning Workflow

### Dataset
- Separate datasets for:
  - Mobile Phones
  - Laptops
  - Tablets

### Training
- Data preprocessing using Pandas & NumPy
- Regression models trained using TensorFlow
- Feature order preserved for inference

### Conversion to TensorFlow Lite
```bash
python convert_to_tflite.py


## 📂 Project Structure

