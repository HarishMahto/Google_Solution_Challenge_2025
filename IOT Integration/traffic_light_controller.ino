#include "esp_camera.h"
#include <WiFi.h>
#include <ArduinoJson.h>
#include <WebServer.h>
#include <WiFiUDP.h>  // Fixed capitalization here

// WiFi credentials
const char* ssid = "Satan Monk";
const char* password = "freewifi";

// Web Server
WebServer server(80);

// UDP Server - Fixed capitalization
WiFiUDP UDP;  // Changed from 'udp' to 'UDP'
unsigned int localUDPPort = 8888;
char incomingPacket[255];

// Traffic light pins
const int RED_PIN = 12;    // GPIO12
const int YELLOW_PIN = 13; // GPIO13
const int GREEN_PIN = 15;  // GPIO15

// Camera pins for AI-THINKER ESP32-CAM
#define PWDN_GPIO_NUM     32
#define RESET_GPIO_NUM    -1
#define XCLK_GPIO_NUM      0
#define SIOD_GPIO_NUM     26
#define SIOC_GPIO_NUM     27
#define Y9_GPIO_NUM       35
#define Y8_GPIO_NUM       34
#define Y7_GPIO_NUM       39
#define Y6_GPIO_NUM       36
#define Y5_GPIO_NUM       21
#define Y4_GPIO_NUM       19
#define Y3_GPIO_NUM       18
#define Y2_GPIO_NUM        5
#define VSYNC_GPIO_NUM    25
#define HREF_GPIO_NUM     23
#define PCLK_GPIO_NUM     22

void handle_jpg_stream(void) {
    WiFiClient client = server.client();
    String response = "HTTP/1.1 200 OK\r\n";
    response += "Content-Type: multipart/x-mixed-replace; boundary=frame\r\n\r\n";
    server.sendContent(response);

    while (1) {
        camera_fb_t * fb = esp_camera_fb_get();
        if (!fb) {
            Serial.println("Frame buffer could not be acquired");
            delay(500);
            continue;
        }

        String head = "--frame\r\n";
        head += "Content-Type: image/jpeg\r\n\r\n";

        server.sendContent(head);
        server.sendContent((const char *)fb->buf, fb->len);
        server.sendContent("\r\n");

        esp_camera_fb_return(fb);

        if (!client.connected()) break;
        delay(10);
    }
}

void handle_jpg(void) {
    camera_fb_t * fb = esp_camera_fb_get();
    if (!fb) {
        Serial.println("Frame buffer could not be acquired");
        server.send(500, "text/plain", "Camera capture failed");
        return;
    }

    server.sendHeader("Content-Type", "image/jpeg");
    server.sendHeader("Content-Length", String(fb->len));
    server.send_P(200, "image/jpeg", (const char *)fb->buf, fb->len);

    esp_camera_fb_return(fb);
}

void setupCamera() {
    camera_config_t config;
    config.ledc_channel = LEDC_CHANNEL_0;
    config.ledc_timer = LEDC_TIMER_0;
    config.pin_d0 = Y2_GPIO_NUM;
    config.pin_d1 = Y3_GPIO_NUM;
    config.pin_d2 = Y4_GPIO_NUM;
    config.pin_d3 = Y5_GPIO_NUM;
    config.pin_d4 = Y6_GPIO_NUM;
    config.pin_d5 = Y7_GPIO_NUM;
    config.pin_d6 = Y8_GPIO_NUM;
    config.pin_d7 = Y9_GPIO_NUM;
    config.pin_xclk = XCLK_GPIO_NUM;
    config.pin_pclk = PCLK_GPIO_NUM;
    config.pin_vsync = VSYNC_GPIO_NUM;
    config.pin_href = HREF_GPIO_NUM;
    config.pin_sscb_sda = SIOD_GPIO_NUM;
    config.pin_sscb_scl = SIOC_GPIO_NUM;
    config.pin_pwdn = PWDN_GPIO_NUM;
    config.pin_reset = RESET_GPIO_NUM;
    config.xclk_freq_hz = 20000000;
    config.pixel_format = PIXFORMAT_JPEG;

    if (psramFound()) {
        config.frame_size = FRAMESIZE_VGA;
        config.jpeg_quality = 10;  // 0-63, lower means higher quality
        config.fb_count = 2;
    } else {
        config.frame_size = FRAMESIZE_SVGA;
        config.jpeg_quality = 12;
        config.fb_count = 1;
    }

    esp_err_t err = esp_camera_init(&config);
    if (err != ESP_OK) {
        Serial.printf("Camera init failed with error 0x%x", err);
        return;
    }
}

void controlTrafficLight(int greenTime) {
    // Yellow light for 3 seconds
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, HIGH);
    digitalWrite(GREEN_PIN, LOW);
    delay(3000);

    // Green light for specified duration
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, HIGH);
    delay(greenTime * 1000);

    // Yellow light for 3 seconds
    digitalWrite(RED_PIN, LOW);
    digitalWrite(YELLOW_PIN, HIGH);
    digitalWrite(GREEN_PIN, LOW);
    delay(3000);

    // Back to red
    digitalWrite(RED_PIN, HIGH);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);
    delay(5000);
}

void setup() {
    Serial.begin(115200);

    // Initialize traffic light pins
    pinMode(RED_PIN, OUTPUT);
    pinMode(YELLOW_PIN, OUTPUT);
    pinMode(GREEN_PIN, OUTPUT);

    // Start with red light
    digitalWrite(RED_PIN, HIGH);
    digitalWrite(YELLOW_PIN, LOW);
    digitalWrite(GREEN_PIN, LOW);

    // Connect to WiFi
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("WiFi connected");
    Serial.print("Camera Stream Ready! Go to: http://");
    Serial.println(WiFi.localIP());

    // Start web server
    server.on("/stream", HTTP_GET, handle_jpg_stream);
    server.on("/capture", HTTP_GET, handle_jpg);
    server.begin();

    // Start UDP server
    UDP.begin(localUDPPort);  // Changed from 'udp' to 'UDP'
    Serial.printf("UDP Server listening on port %d\n", localUDPPort);

    // Initialize camera
    setupCamera();
}

void loop() {
    server.handleClient();

    int packetSize = UDP.parsePacket();  // Changed from 'udp' to 'UDP'
    if (packetSize) {
        int len = UDP.read(incomingPacket, 255);  // Changed from 'udp' to 'UDP'
        if (len > 0) {
            incomingPacket[len] = 0;

            StaticJsonDocument<200> doc;
            DeserializationError error = deserializeJson(doc, incomingPacket);

            if (!error) {
                int greenTime = doc["green_time"];
                bool isEmergency = doc["emergency"];

                if (isEmergency) {
                    // Emergency vehicle detected - immediate green light
                    digitalWrite(RED_PIN, LOW);
                    digitalWrite(YELLOW_PIN, LOW);
                    digitalWrite(GREEN_PIN, HIGH);
                    delay(greenTime * 1000);
                } else {
                    // Normal traffic flow
                    controlTrafficLight(greenTime);
                }
            }
        }
    }
}