<template>
  <div class="app-wrapper">
    <!-- Sidebar component -->
    <LoveBellSidebar />
    <div class="main-content-head">
      <h1>LOVE BELL RADAR</h1>
      <!-- Main content area -->
      <div class="main-content">
        <!-- OpenStreetMap container bên trái -->
        <div id="map"></div>

        <!-- Information and button section bên phải -->
        <div class="info-section">
          <div class="info">
            <h3>Looking for someone?</h3>
            <p>{{ users.length }} users are close to you, let's find them !!</p>

            <!-- Range slider -->
            <label for="range-slider">Within {{ range }} meters</label>
            <input
              type="range"
              id="range-slider"
              v-model="range"
              min="1000"
              max="100000"
              step="500"
              @input="updateSliderStyle"
              class="range-slider"
            />

            <p>
              Make sure to look & check, then decide whether you should catch up
              with them!
            </p>
          </div>
          <button
            class="scan-btn"
            :class="{ scanning: isScanning }"
            @click="startScanning"
            :disabled="isScanning"
          >
            {{ isScanning ? "Scanning..." : "Re-scanning" }}
          </button>

          <!-- User details displayed below scan button -->
          <div v-if="selectedUser" class="user-details">
            <button class="close-button" @click="closeProfile">×</button>

            <div
              v-if="selectedUser.photoUrls && selectedUser.photoUrls.length"
              class="user-photos"
            >
              <div class="photo-gallery">
                <button @click="prevPhoto" class="nav-button">❮</button>
                <img
                  :src="selectedUser.photoUrls[currentPhotoIndex]"
                  alt="User Photo"
                  class="user-photo"
                />
                <button @click="nextPhoto" class="nav-button">❯</button>
              </div>
            </div>
            <h2>{{ selectedUser.name }} - {{ selectedUser.age }}</h2>
            <p>{{ selectedUser.bio || "No bio available" }}</p>

            <div class="action-buttons-modal">
              <button
                class="button dislike-button"
                @click="handleUnlike(selectedUser.userId)"
              >
                <i class="fas fa-times"></i>
              </button>
              <button class="button super-like-button" @click="superLike">
                <i class="fas fa-star"></i>
              </button>
              <button
                class="button like-button"
                @click="handleLike(selectedUser.userId)"
              >
                <i class="fas fa-heart"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import LoveBellSidebar from "@/views/sidebar/LoveBellSidebar.vue";
import {
  checkUserLocation,
  fetchNearbyUsers,
} from "@/services/location-service";
import { ElNotification } from "element-plus";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { swipeAction } from "@/services/swipe-service";

export default {
  components: {
    LoveBellSidebar,
  },
  data() {
    return {
      range: 1000, // Distance range
      users: [], // Store user positions
      isScanning: false, // Controls scanning animation
      userId: localStorage.getItem("userId"),
      map: null, // The Leaflet map object
      selectedUser: null, // Người dùng được chọn để hiển thị chi tiết
      currentPhotoIndex: 0, // Chỉ số của ảnh đang hiển thị
    };
  },
  mounted() {
    this.checkUserLocation();
    this.initMap(); // Initialize the map when the component is mounted
  },
  methods: {
    // Initialize the map and set center to user's current location
    initMap() {
      // Kiểm tra nếu trình duyệt hỗ trợ geolocation
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;

            // Tạo bản đồ với trung tâm là vị trí hiện tại
            this.map = L.map("map", {
              minZoom: 10, // Mức zoom tối thiểu
              maxZoom: 18, // Mức zoom tối đa
            }).setView([latitude, longitude], 13); // Đặt trung tâm tại vị trí hiện tại

            L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
              attribution:
                '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            }).addTo(this.map);

            // Đánh dấu vị trí hiện tại trên bản đồ
            L.marker([latitude, longitude])
              .addTo(this.map)
              .bindPopup("You are here.")
              .openPopup();

            setTimeout(() => {
              this.map.closePopup();
            }, 5000);
          },
          (error) => {
            console.error("Error retrieving location:", error);
            this.setDefaultMapCenter(); // Đặt trung tâm mặc định nếu không thể lấy tọa độ
          }
        );
      } else {
        console.warn("Geolocation is not supported by this browser.");
        this.setDefaultMapCenter(); // Đặt trung tâm mặc định nếu không hỗ trợ geolocation
      }
    },

    // Đặt trung tâm mặc định nếu không thể lấy vị trí hiện tại
    setDefaultMapCenter() {
      this.map = L.map("map", {
        minZoom: 10,
        maxZoom: 18,
      }).setView([10.762622, 106.660172], 13); // Trung tâm mặc định là TP. Hồ Chí Minh

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      }).addTo(this.map);
    },

    // Chuyển đến ảnh trước đó
    prevPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex =
          (this.currentPhotoIndex - 1 + this.selectedUser.photoUrls.length) %
          this.selectedUser.photoUrls.length;
      }
    },

    // Chuyển đến ảnh tiếp theo
    nextPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex =
          (this.currentPhotoIndex + 1) % this.selectedUser.photoUrls.length;
      }
    },

    // Khi chọn người dùng mới, đặt lại chỉ số ảnh về 0
    onMarkerClick(user) {
      this.selectedUser = user;
      this.currentPhotoIndex = 0;
    },

    async startScanning() {
      this.isScanning = true; // Bắt đầu hiệu ứng scan
      const markers = document.querySelectorAll(".leaflet-marker-icon");
      markers.forEach((marker) => marker.classList.add("scanning-marker"));
      // Tạo hiệu ứng scan ảo trong 5 giây trước khi hiển thị kết quả thực
      await this.fakeScanAnimation(5000);

      // Thực hiện việc quét thực tế sau hiệu ứng scan ảo
      const nearbyUsers = await fetchNearbyUsers(this.userId, this.range);
      if (nearbyUsers?.data) {
        this.isScanning = false;
        markers.forEach((marker) => marker.classList.remove("scanning-marker"));

        ElNotification({
          title: "Success",
          message: "Fetch Nearby Users Successfully.",
          type: "success",
        });

        // Lọc người dùng để loại trừ chính mình
        const filteredUsers = nearbyUsers.data.filter(
          (user) => user.userId !== this.userId
        );

        // Clear existing markers
        this.clearMap();

        // Add users to the map
        filteredUsers.forEach((user) => {
          const { latitude, longitude, userId: userOnMapId } = user;

          if (latitude && longitude) {
            const marker = L.marker([latitude, longitude]).addTo(this.map);
            const isCurrentUser = this.userId == userOnMapId;

            if (!isCurrentUser) {
              marker.on("click", () => {
                this.onMarkerClick(user); // Hiển thị chi tiết người dùng khi click
              });
            }
          } else {
            console.warn("Missing latitude or longitude for user:", user);
          }
        });

        // Cập nhật danh sách người dùng hiển thị (không bao gồm chính mình)
        this.users = filteredUsers;

        // Cập nhật số lượng người dùng hiển thị
        if (filteredUsers.length === 0) {
          ElNotification({
            title: "Info",
            message: "No users found nearby except you.",
            type: "info",
          });
        }
      }
    },

    // Function to create a fake scan animation (tạo độ trễ trước khi quét thật)
    async fakeScanAnimation(duration) {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve();
        }, duration);
      });
    },

    // Clear all markers from the map
    clearMap() {
      if (this.map) {
        this.map.eachLayer((layer) => {
          if (layer instanceof L.Marker) {
            this.map.removeLayer(layer);
          }
        });
      } else {
        console.error("Map is not initialized or has been destroyed.");
      }
    },
    async checkUserLocation() {
      try {
        const userId = localStorage.getItem("userId");
        const response = await checkUserLocation(userId);

        if (response?.message === "Location not configured for user") {
          ElNotification({
            title: "Missing Location",
            message:
              "Please set your location to continue using Love Bell Radar.",
            type: "warning",
          });

          setTimeout(() => {
            this.$router.push("/getLocation");
          }, 2000);
        }
      } catch (error) {
        console.error("Error checking user location:", error);
      }
    },
    async handleLike(userId) {
      try {
        const response = await swipeAction(userId, true);
        console.log("Swipe action (like) completed:", response);
        ElNotification({
          title: "Liked",
          message: `You have liked user ${userId}`,
          type: "success",
        });

        // Thực hiện hành động khác nếu cần, ví dụ: đóng hồ sơ hoặc chuyển đến người dùng khác
        this.closeProfile();
      } catch (error) {
        console.error("Error during like action:", error.message);
        ElNotification({
          title: "Error",
          message: "Có lỗi xảy ra khi thực hiện hành động like.",
          type: "error",
        });
      }
    },

    async handleUnlike(userId) {
      try {
        const response = await swipeAction(userId, false);
        console.log("Swipe action (unlike) completed:", response);
        ElNotification({
          title: "Unliked",
          message: `You have unliked user ${userId}`,
          type: "success",
        });

        // Thực hiện hành động khác nếu cần, ví dụ: đóng hồ sơ hoặc chuyển đến người dùng khác
        this.closeProfile();
      } catch (error) {
        console.error("Error during unlike action:", error.message);
        ElNotification({
          title: "Error",
          message: "Có lỗi xảy ra khi thực hiện hành động unlike.",
          type: "error",
        });
      }
    },
    updateSliderStyle() {
      const slider = document.getElementById("range-slider");
      const percentage = ((this.range - 500) / (100000 - 1000)) * 100;
      slider.style.background = `linear-gradient(90deg, #ff99ff ${percentage}%, #ffffff ${percentage}%)`;
    },
    closeProfile() {
      this.selectedUser = null;
    },
  },
};
</script>

<style scoped>
/* Căn chỉnh toàn bộ ứng dụng */
.app-wrapper {
  display: flex;
  height: 100vh;
}
.main-content-head h1 {
  text-align: center;
  margin-top: 30px;
  font-size: 35px;
  color: #ff6699;
  font-size: 40px;
  font-weight: 600;
}

/* Main content area */
.main-content {
  display: flex;
  flex-direction: row;
  flex: 1;
  padding-left: 20px;
}

/* Định dạng bản đồ thành hình tròn và căn bên trái */
/* Đặt bản đồ ở trạng thái tương đối để định vị quét chính xác */
#map {
  height: 700px;
  width: 700px;
  overflow: hidden;
  box-shadow: 0px 0px 15px rgba(0, 0, 0, 0.2);
  margin: 0px 0px 0px 50px;
  position: relative;
}

/* Định dạng phần thông tin và nút */
.info-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  padding-left: 20px;
  align-items: center;
}

.info {
  font-size: 21px;
  text-align: center;
}

.info h3 {
  font-weight: bold;
  font-size: 35px;
}

.scan-btn {
  width: 30%;
  padding: 10px 20px;
  font-size: 20px;
  background-color: #ff99ff;
  border: none;
  color: white;
  border-radius: 5px;
  cursor: pointer;
  margin-bottom: 20px;
}

.scan-btn:disabled {
  background-color: #ddd;
}

.scan-btn:hover:not(:disabled) {
  background-color: #ff66ff;
}

/* Định dạng phần chi tiết người dùng */
.user-details {
  background-color: #f9f9f9;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 15px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
  width: 60%;
  position: relative; /* Để định vị nút "Close" */
}

.user-photo {
  display: block;
  margin: 0 auto; /* Căn giữa theo chiều ngang */
  width: 200px; /* Đặt kích thước ảnh tùy ý */
  height: 250px;
  object-fit: cover; /* Đảm bảo ảnh không bị méo */
}

.photo-gallery {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-button {
  background: none;
  border: none;
  font-size: 30px;
  color: #ff6699;
  cursor: pointer;
  margin: 0 10px;
  transition: color 0.3s;
}

.nav-button:hover {
  color: #ff3399;
}

.nav-button:focus {
  outline: none;
}

.range-slider {
  -webkit-appearance: none;
  width: 80%;
  height: 10px;
  background: linear-gradient(90deg, #ff99ff 0%, #ffffff 0%);
  border-radius: 5px;
  outline: none;
  transition: background 0.3s;
}

.range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  background: #ff66ff;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0px 0px 5px rgba(0, 0, 0, 0.2);
}

.range-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  background: #ff66ff;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0px 0px 5px rgba(0, 0, 0, 0.2);
}

/* Nút close */
.close-button {
  background: none;
  border: none;
  font-size: 20px;
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
}
/* Action buttons giống với Profile Popup */
.action-buttons-modal {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

/* Action buttons */
.button {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  cursor: pointer;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease;
  border: none;
}

.dislike-button {
  background-color: #ff5a5f; /* Red color for dislike */
  box-shadow: 0 0 15px rgba(255, 90, 95, 0.3);
}

.super-like-button {
  background-color: #3498db; /* Blue color for super-like */
  box-shadow: 0 0 15px rgba(52, 152, 219, 0.3);
}

.like-button {
  background-color: #2ecc71; /* Green color for like */
  box-shadow: 0 0 15px rgba(46, 204, 113, 0.3);
}

.button:hover {
  transform: scale(1.15);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
}

.button:active {
  transform: scale(0.95);
}

.button i {
  font-size: 1.5rem;
  color: white; /* Set icon color to white */
}

.button:hover {
  transform: scale(1.1);
}

.button:active {
  transform: scale(0.95);
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.scan-btn.scanning {
  animation: pulse 1.5s infinite;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-details {
  animation: slideIn 0.5s ease-out;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.leaflet-marker-icon {
  transition: transform 0.5s ease;
}

.map .scanning-marker {
  animation: rotate 2s infinite linear;
}

@keyframes slideInLeft {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}

@keyframes slideOutLeft {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-100%);
  }
}

.app-wrapper .sidebar {
  animation: slideInLeft 0.5s forwards;
}

.app-wrapper .sidebar.hidden {
  animation: slideOutLeft 0.5s forwards;
}
</style>
