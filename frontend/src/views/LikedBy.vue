<template>
  <div id="app" class="app-container">
    <!-- Sidebar for matches on the left -->
    <aside class="sidebarMatch">
      <div class="sidebar-header">
        <h3>Những người đã thích bạn</h3>
      </div>
      <div class="matches-grid" v-if="likedMe && likedMe.length > 0">
        <!-- Display matched profiles -->
        <div
          class="match-item"
          v-for="like in likedMe"
          :key="like.profileId"
          @click="selectUser(like)"
        >
          <img
            :src="getAuthorizedImageUrl(like.avatar)"
            class="match-image"
            alt="Match Avatar"
          />
          <div class="match-info">
            <span class="match-name">{{ like.name }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- User Details Section on the right, only visible when a user is selected -->
    <div v-if="selectedUser" class="user-details">
      <button class="close-button" @click="closeProfile">×</button>
      <div v-if="selectedUser.photoUrls && selectedUser.photoUrls.length" class="user-photos">
        <div class="photo-gallery">
          <button @click="prevPhoto" class="nav-button">❮</button>
          <img :src="selectedUser.photoUrls[currentPhotoIndex]" alt="User Photo" class="user-photo" />
          <button @click="nextPhoto" class="nav-button">❯</button>
        </div>
      </div>
      <h2>{{ selectedUser.name }} - {{ selectedUser.age }}</h2>
      <p>{{ selectedUser.bio || "No bio available" }}</p>

      <div class="action-buttons-modal">
        <button class="button dislike-button" @click="handleUnlike(selectedUser.userId)">
          <i class="fas fa-times"></i>
        </button>
        <button class="button super-like-button" @click="superLike">
          <i class="fas fa-star"></i>
        </button>
        <button class="button like-button" @click="handleLike(selectedUser.userId)">
          <i class="fas fa-heart"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllLikedMe } from "@/services/like-service";
import { swipeAction } from "@/services/swipe-service";
import { ElNotification } from "element-plus";
export default {
  name: "App",
  data() {
    return {
      likedMe: [], // Stores profiles that liked the user
      selectedUser: null, // Holds the selected user's details
      currentPhotoIndex: 0, // Current photo index in the gallery
      userId: localStorage.getItem("userId"),
    };
  },
  methods: {
    async loadlikedMe() {
      try {
        const likedMeData = await getAllLikedMe();
        this.likedMe = likedMeData;
      } catch (error) {
        console.error("Error loading matches:", error.message);
      }
    },
    getAuthorizedImageUrl(url) {
      const token = localStorage.getItem("userToken");
      if (token) {
        const separator = url.includes("?") ? "&" : "?";
        return `${url}${separator}Authorization=Bearer ${token}`;
      } else {
        console.error("User token not found.");
        return url;
      }
    },
    selectUser(user) {
      this.selectedUser = {
        ...user,
        photoUrls: user.photos.map(photo => photo.url),
      };
      this.currentPhotoIndex = 0; // Reset photo index when a new user is selected
    },
    closeProfile() {
      this.selectedUser = null; // Clear selected user to hide details
    },
    prevPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex = (this.currentPhotoIndex - 1 + this.selectedUser.photoUrls.length) % this.selectedUser.photoUrls.length;
      }
    },
    nextPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex = (this.currentPhotoIndex + 1) % this.selectedUser.photoUrls.length;
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
    superLike() {
      console.log("Super liked the user!");
    },
  },
  async mounted() {
    await this.loadlikedMe();
  },
};
</script>

<style scoped>
/* App container layout */
.app-container {
  display: flex;
  height: 100vh;
}

/* Sidebar */
.sidebarMatch {
  width: 30%;
  background-color: #f6f6f6;
  border-right: 1px solid #e0e0e0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  animation: slideInLeft 0.6s ease-out;
}

.sidebar-header {
  text-align: center;
  margin: 10px;
  font-size: 24px;
  font-weight: bold;
}

/* Grid Layout for Matching Objects */
.matches-grid {
  padding-bottom: 300px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding-top: 20px;
  animation: fadeIn 0.8s ease-in-out;
}

.match-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.match-item:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  padding: 10px;
}

.match-image {
  width: 100px;
  height: 100px;
  border-radius: 20%;
  object-fit: cover;
  margin-bottom: 10px;
  animation: popIn 0.5s ease-out;
}

.match-name {
  font-size: 16px;
  font-weight: bold;
  font-family: Arial, Helvetica, sans-serif;
}

/* User details section styling on the right */
.user-details {
  width: 70%;
  background-color: #f9f9f9;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 15px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
  position: relative;
  animation: slideIn 0.5s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* Photo gallery styling */
.user-photo {
  display: block;
  margin: 0 auto;
  width: 200px;
  height: 250px;
  object-fit: cover;
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

.close-button {
  background-color: #ff5a5f;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 20px;
  width: 30px;
  height: 30px;
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.close-button:hover {
  background-color: #ff3338;
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.close-button:active {
  background-color: #ff1a1e;
  transform: scale(0.9);
}

/* Action buttons styling */
.action-buttons-modal {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

.button {
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
  background-color: #ff5a5f;
  box-shadow: 0 0 15px rgba(255, 90, 95, 0.3);
}

.super-like-button {
  background-color: #3498db;
  box-shadow: 0 0 15px rgba(52, 152, 219, 0.3);
}

.like-button {
  background-color: #2ecc71;
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
  color: white;
}

@keyframes slideInLeft {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes popIn {
  from {
    transform: scale(0.7);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
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
</style>
