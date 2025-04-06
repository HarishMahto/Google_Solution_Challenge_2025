# File: traffic_management.py

import cv2
import numpy as np
from ultralytics import YOLO
import socket
import json
import threading
from flask import Flask, Response, render_template
import time
import requests
from datetime import datetime
import logging

class TrafficManagementSystem:
    def __init__(self):
        # Set up logging
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler('traffic_system.log'),
                logging.StreamHandler()
            ]
        )
        self.logger = logging.getLogger(__name__)
        
        # Initialize YOLO model
        try:
            self.model = YOLO('yolov8n.pt')
            self.logger.info("YOLO model initialized successfully")
        except Exception as e:
            self.logger.error(f"Failed to initialize YOLO model: {str(e)}")
            raise
        
        # Camera settings
        self.ESP32_CAM_URL = "http://192.168.136.109"  # Update with your ESP32's IP
        self.video_stream_url = f"{self.ESP32_CAM_URL}:80/stream"
        self.still_image_url = f"{self.ESP32_CAM_URL}/capture"
        
        # Traffic parameters
        self.MIN_GREEN_TIME = 5
        self.MAX_GREEN_TIME = 28
        self.YELLOW_TIME = 3
        self.RED_TIME = 5
        
        self.vehicle_crossing_times = {
            'car': 2.5,
            'motorcycle': 1.5,
            'bus': 4.0,
            'truck': 4.0,
            'rickshaw': 2.0
        }
        
        self.class_mapping = {
            2: 'car',
            3: 'motorcycle', 
            5: 'bus',
            7: 'truck',
            1: 'rickshaw'
        }
        
        # System state
        self.number_of_lanes = 4
        self.frame = None
        self.processed_stats = {
            'vehicle_counts': {},
            'green_time': self.MIN_GREEN_TIME,
            'emergency_detected': False,
            'last_update': datetime.now()
        }

        '''
        # Detection zone (adjust based on your camera view)
        self.detection_zone = np.array([
            [200, 300], [600, 300],  # Top points
            [700, 500], [100, 500]   # Bottom points
        ], np.int32)
        '''

        # Updated detection zone to work with both VGA (640x480) and SVGA (800x600)
        # For VGA: Using most of the vertical space (80-420 out of 480)
        # For SVGA: Will still work well as the proportions are similar
        self.detection_zone = np.array([
            [160, 80],   [480, 80],    # Top points (leaving margin from top)
            [560, 420],  [80, 420]     # Bottom points (leaving margin from bottom)
        ], np.int32)


        # Threading and synchronization
        self.lock = threading.Lock()
        self.frame_lock = threading.Lock()
        

        # Initialize Flask app
        self.app = Flask(__name__)
        self.setup_routes()
        
        # UDP communication
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.esp32_address = (self.ESP32_CAM_URL.split('//')[1], 8888)
        
        # Camera connection settings
        self.max_retries = 5
        self.retry_delay = 2
        
    def setup_routes(self):
        """Set up Flask routes"""
        self.app.add_url_rule('/', 'index', self.index)
        self.app.add_url_rule('/video_feed', 'video_feed', self.video_feed)
        self.app.add_url_rule('/stats', 'stats', self.get_stats)
        
    def index(self):
        """Render main monitoring page"""
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Traffic Management System</title>
            <style>
                body { 
                    font-family: Arial, sans-serif;
                    margin: 20px;
                    background-color: #f0f0f0;
                }
                .container {
                    max-width: 1200px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: white;
                    border-radius: 10px;
                    box-shadow: 0 0 10px rgba(0,0,0,0.1);
                }
                .video-container {
                    margin-top: 20px;
                    text-align: center;
                }
                .stream {
                    width: 100%;
                    max-width: 800px;
                    border-radius: 5px;
                }
                .stats {
                    margin-top: 20px;
                    padding: 20px;
                    background-color: #f8f9fa;
                    border-radius: 5px;
                }
                .emergency {
                    color: red;
                    font-weight: bold;
                }
            </style>
            <script>
                function updateStats() {
                    fetch('/stats')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('stats').innerHTML = `
                                <h3>Traffic Statistics</h3>
                                <p>Green Time: ${data.green_time}s</p>
                                <p>Vehicle Counts:</p>
                                <ul>
                                    ${Object.entries(data.vehicle_counts)
                                        .map(([k,v]) => `<li>${k}: ${v}</li>`)
                                        .join('')}
                                </ul>
                                ${data.emergency_detected ? 
                                    '<p class="emergency">Emergency Vehicle Detected!</p>' : ''}
                                <p>Last Update: ${data.last_update}</p>
                            `;
                        });
                }
                
                setInterval(updateStats, 1000);
            </script>
        </head>
        <body>
            <div class="container">
                <h1>Traffic Management System</h1>
                <div class="video-container">
                    <img class="stream" src="/video_feed">
                </div>
                <div id="stats" class="stats">
                    Loading statistics...
                </div>
            </div>
        </body>
        </html>
        """
    
    def get_stats(self):
        """Return current traffic statistics"""
        with self.lock:
            return json.dumps(self.processed_stats)
    
    def connect_camera(self):
        """Establish connection to ESP32-CAM with retry mechanism"""
        for attempt in range(self.max_retries):
            try:
                cap = cv2.VideoCapture(self.video_stream_url)
                ret, frame = cap.read()
                if ret:
                    self.logger.info("Camera connection successful")
                    return cap
                else:
                    cap.release()
                    raise Exception("Failed to get frame")
            except Exception as e:
                self.logger.warning(f"Camera connection attempt {attempt + 1}/{self.max_retries} failed: {str(e)}")
                if attempt < self.max_retries - 1:
                    time.sleep(self.retry_delay)
        
        self.logger.error("Failed to connect to camera after maximum retries")
        raise Exception("Camera connection failed")

    def get_frame(self):
        """Get frame from ESP32-CAM with fallback options"""
        try:
            # Try streaming first
            cap = cv2.VideoCapture(self.video_stream_url)
            ret, frame = cap.read()
            cap.release()
            
            if ret:
                return frame
            
            # Fallback
            # Fallback to still image
            response = requests.get(self.still_image_url)
            if response.status_code == 200:
                nparr = np.frombuffer(response.content, np.uint8)
                frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                return frame
            
        except Exception as e:
            self.logger.error(f"Error getting frame: {str(e)}")
            return None

    def process_frame(self, frame):
        """Process a single frame for vehicle detection and traffic analysis"""
        try:
            # Create mask for detection zone
            mask = np.zeros(frame.shape[:2], dtype=np.uint8)
            cv2.fillPoly(mask, [self.detection_zone], 255)
            masked_frame = cv2.bitwise_and(frame, frame, mask=mask)
            
            # Perform YOLO detection
            results = self.model(masked_frame, imgsz=640, conf=0.4)
            
            # Count vehicles and check for emergency vehicles
            vehicle_counts = self.count_vehicles_by_class(results[0])
            emergency_detected = self.detect_emergency_vehicle(results[0])
            
            # Calculate green time
            green_time = (self.MAX_GREEN_TIME if emergency_detected 
                         else self.calculate_green_time(vehicle_counts))
            
            # Update processed stats
            with self.lock:
                self.processed_stats = {
                    'vehicle_counts': vehicle_counts,
                    'green_time': green_time,
                    'emergency_detected': emergency_detected,
                    'last_update': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                }
            
            # Send timing to ESP32
            self.send_signal_timing(green_time, emergency_detected)
            
            # Draw visualization
            return self.draw_visualization(frame, results[0], vehicle_counts, green_time, emergency_detected)
            
        except Exception as e:
            self.logger.error(f"Error processing frame: {str(e)}")
            return frame

    def count_vehicles_by_class(self, results):
        """Count detected vehicles by class"""
        vehicle_counts = {vehicle_type: 0 for vehicle_type in self.vehicle_crossing_times.keys()}
        
        if results.boxes.cls is not None:
            for class_id in results.boxes.cls:
                class_id = int(class_id)
                if class_id in self.class_mapping:
                    vehicle_class = self.class_mapping[class_id]
                    vehicle_counts[vehicle_class] += 1
        
        return vehicle_counts

    def detect_emergency_vehicle(self, results):
        """Check for emergency vehicles"""
        if results.boxes.cls is not None and results.boxes.conf is not None:
            for cls, conf in zip(results.boxes.cls, results.boxes.conf):
                # Check for emergency vehicles (class 3 with high confidence)
                if int(cls) == 3 and conf > 0.8:
                    return True
        return False

    def calculate_green_time(self, vehicle_counts):
        """Calculate optimal green signal time"""
        total_crossing_time = sum(
            count * self.vehicle_crossing_times[vehicle_type]
            for vehicle_type, count in vehicle_counts.items()
        )
        
        # Calculate green time based on vehicle counts and crossing times
        green_time = (total_crossing_time*5) / self.number_of_lanes
        
        # Constrain within min and max limits
        return int(max(min(green_time, self.MAX_GREEN_TIME), self.MIN_GREEN_TIME))

    def draw_visualization(self, frame, results, vehicle_counts, green_time, emergency):
        """Draw detection visualization on frame"""
        # Draw detection zone
        cv2.polylines(frame, [self.detection_zone], True, (0, 255, 0), 2)
        
        # Draw bounding boxes for detected vehicles
        if results.boxes is not None:
            for box, cls, conf in zip(results.boxes.xyxy, results.boxes.cls, results.boxes.conf):
                if int(cls) in self.class_mapping:
                    x1, y1, x2, y2 = map(int, box[:4])
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (255, 0, 0), 2)
                    cv2.putText(frame, f"{self.class_mapping[int(cls)]} {conf:.2f}",
                              (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 0, 0), 2)
        
        # Draw statistics
        y_offset = 30
        cv2.putText(frame, f"Green Time: {green_time}s",
                   (10, y_offset), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
        
        for vehicle_type, count in vehicle_counts.items():
            y_offset += 30
            cv2.putText(frame, f"{vehicle_type}: {count}",
                       (10, y_offset), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
        
        if emergency:
            y_offset += 30
            cv2.putText(frame, "EMERGENCY VEHICLE DETECTED",
                       (10, y_offset), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
        
        return frame

    def send_signal_timing(self, duration, is_emergency=False):
        """Send timing information to ESP32"""
        try:
            data = {
                "green_time": duration,
                "yellow_time": self.YELLOW_TIME,
                "red_time": self.RED_TIME,
                "emergency": is_emergency
            }
            self.sock.sendto(json.dumps(data).encode(), self.esp32_address)
        except Exception as e:
            self.logger.error(f"Error sending data to ESP32: {str(e)}")

    def video_feed(self):
        """Generate video feed for web interface"""
        def generate():
            while True:
                try:
                    with self.frame_lock:
                        if self.frame is not None:
                            success, encoded_frame = cv2.imencode('.jpg', self.frame)
                            if success:
                                yield (b'--frame\r\n'
                                      b'Content-Type: image/jpeg\r\n\r\n' + 
                                      encoded_frame.tobytes() + b'\r\n')
                except Exception as e:
                    self.logger.error(f"Error in video feed: {str(e)}")
                time.sleep(0.1)
        
        return Response(generate(),
                       mimetype='multipart/x-mixed-replace; boundary=frame')

    def run(self):
        """Main processing loop"""
        try:
            while True:
                # Get frame
                frame = self.get_frame()
                if frame is None:
                    self.logger.warning("No frame received, retrying...")
                    time.sleep(1)
                    continue
                
                # Process frame
                processed_frame = self.process_frame(frame)
                
                # Update frame buffer
                with self.frame_lock:
                    self.frame = processed_frame
                
                # Optional: Display frame for debugging
                cv2.imshow('Traffic Management System', processed_frame)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
                    
        except KeyboardInterrupt:
            self.logger.info("Shutting down traffic management system")
        except Exception as e:
            self.logger.error(f"Error in main loop: {str(e)}")
        finally:
            cv2.destroyAllWindows()

def main():
    # Set up logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler('traffic_system.log'),
            logging.StreamHandler()
        ]
    )
    logger = logging.getLogger(__name__)
    
    try:
        # Create and run traffic management system
        traffic_system = TrafficManagementSystem()
        
        # Start Flask server in a separate thread
        flask_thread = threading.Thread(
            target=lambda: traffic_system.app.run(host='0.0.0.0', port=5000, threaded=True)
        )
        flask_thread.daemon = True
        flask_thread.start()
        
        # Start main processing loop
        traffic_system.run()
        
    except Exception as e:
        logger.error(f"System error: {str(e)}")
        
if __name__ == "__main__":
    main()
