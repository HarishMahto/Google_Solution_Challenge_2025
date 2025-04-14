# DirecTo🚦 Smart Traffic Management System Using Edge Computing  
### *Google Solution Challenge 2025 – Open Innovation*
<img src="https://github.com/user-attachments/assets/51a3469b-7e6e-4f44-bc2d-94d94b8b4f04" height="400"/>



## 🌍 Overview  
A real-time AI-powered system designed to **reduce urban traffic congestion**, **prioritize emergency vehicles**, and **predict traffic patterns** using **Edge Computing** and **Machine Learning**. Built for smart cities, this solution enables scalable, responsive traffic control without relying solely on cloud infrastructure.

---

## 🔧 Features

- 🛑 **Real-Time Signal Control**  
  Uses live camera feeds and YOLO object detection on edge devices (ESP32-CAM & Raspberry Pi) to control traffic lights based on vehicle density.

- 🚑 **Emergency Vehicle Prioritization**  
  Detects emergency vehicles and creates a **Green Corridor** by automatically altering signal states across intersections.

- 🚗 **Vehicle Classification**  
  Classifies two-wheelers, cars, buses, and trucks using YOLOv8 to determine traffic weightage and manage green time efficiently.

- 🔮 **Traffic Forecasting with AI**  
  Uses **Gemini AI** and **LSTM models** to predict future traffic congestion and optimize routing decisions.

- 📱 **Android App for Users**  
  Displays live signal status, voice-based navigation, emergency alert system, and route suggestions using real-time data.

- ☁️ **Cloud Integration via Firebase**  
  Syncs traffic data, alerts, and signal state information in real-time for coordination between edge devices and the app.

---

## 🧠 Tech Stack

| Component          | Tools & Tech                                  |
|--------------------|-----------------------------------------------|
| **Hardware**        | ESP32-CAM, Ultrasonic Sensors, Raspberry Pi 4 |
| **AI/ML**           | YOLOv8 (Object Detection), Gemini + LSTM      |
| **Programming**     | Python (OpenCV, TensorFlow), C++ (Arduino), Java (Android Studio) |
| **Cloud**           | Firebase (Realtime DB)                        |
| **Edge Processing** | Raspberry Pi 4, ESP32 with onboard camera     |
| **Frontend (App)**  | Android Studio                                |

---

## 🎯 Alignment with United Nations Sustainable Development Goals (SDGs)

🌆 **Goal 11: Sustainable Cities**  
Smarter traffic flow and cleaner air quality through optimized signaling  

🏥 **Goal 3: Good Health**  
Quicker ambulance routing, reducing emergency response delays  

🏗️ **Goal 9: Industry & Infrastructure**  
Leverages AI and IoT for next-gen smart road infrastructure  

🌱 **Goal 13: Climate Action**  
Reduced idle time leads to fewer emissions  

♻️ **Goal 12: Responsible Consumption**  
Efficient use of resources and minimal system overhead  

💼 **Goal 8: Decent Work & Economic Growth**  
Enhances urban productivity and creates new tech-based job roles  


---

## 📸 App Screenshots / Videos

>  YouTube links here


<img src="https://github.com/user-attachments/assets/eae46e8e-348a-4133-9981-9b3e00c0540a" width="150"/>
<img src="https://github.com/user-attachments/assets/1179c22e-660b-4cab-9286-49c5b8f9a39a" width="150"/>
<img src="https://github.com/user-attachments/assets/bd008866-348e-418f-a82e-4304115d861d" width="150"/>
<img src="https://github.com/user-attachments/assets/c5b997b1-4491-4e84-81c1-75b3d1ee1ac5" width="150"/>
<img src="https://github.com/user-attachments/assets/099d8fd5-71fb-451c-98f3-e076a38d0f1a" width="150"/>
<img src="https://github.com/user-attachments/assets/05f18afa-385c-4f10-b7a9-a6553dfee3dd" width="150"/>
---

## 🛠️ How It Works

```mermaid
graph TD
A[Vehicle Detected via Camera] --> B[YOLOv8 Detection & Classification on Edge]
B --> C[Traffic Weight Calculation]
C --> D[Signal Timer Adjustment]
B --> E[Emergency Vehicle Detected]
E --> F[Green Corridor Creation]
D & F --> G[Firebase Sync]
G --> H[Android App Display]
```


## **Team Members**  
<p align="center">
    <a href="https://github.com/HarishMahto">
        <img src="https://github.com/HarishMahto.png" width="80" height="80" style="border-radius: 50%;" alt="HarishMahto">
    </a>
    <a href="https://github.com/Somie12">
        <img src="https://github.com/Somie12.png" width="80" height="80" style="border-radius: 50%;" alt="Somie12">
    </a>
    <a href="https://github.com/Diksha566">
        <img src="https://github.com/Diksha566.png" width="80" height="80" style="border-radius: 50%;" alt="Diksha566">
    </a>
    <a href="https://github.com/YashaswiniMishra">
        <img src="https://github.com/YashaswiniMishra.png" width="80" height="80" style="border-radius: 50%;" alt="YashaswiniMishra">
    </a>
    
    
</p>

