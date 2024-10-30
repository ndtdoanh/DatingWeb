<template>
  <div id="likedBy-container" class="liked-app-container">
    <!-- Sidebar showing people who liked the user -->
    <aside class="liked-sidebarMatch">
      <div class="liked-sidebar-header">
        <h3>People Who Liked You</h3>
      </div>
      <div class="liked-matches-grid" v-if="likedMe && likedMe.length > 0">
        <div
          class="liked-match-item"
          v-for="like in likedMe"
          :key="like.profileId"
          @click="selectUser(like)"
        >
          <img
            :src="getAuthorizedImageUrl(like.avatar)"
            class="liked-match-image"
            alt="User Avatar"
          />
          <div class="liked-match-info">
            <span class="liked-match-name">{{ like.name }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Detailed view of selected user -->
    <div v-if="selectedUser" class="liked-user-details">
      <button class="liked-close-button" @click="closeProfile">×</button>
      <div
        v-if="selectedUser.photoUrls && selectedUser.photoUrls.length"
        class="liked-user-photos"
      >
        <div class="liked-photo-gallery">
          <button @click="prevPhoto" class="liked-nav-button">❮</button>
          <img
            :src="selectedUser.photoUrls[currentPhotoIndex]"
            alt="User Photo"
            class="liked-user-photo"
          />
          <button @click="nextPhoto" class="liked-nav-button">❯</button>
        </div>
      </div>
      <div class="liked-info">
        <h2>{{ selectedUser.name }} - {{ selectedUser.age }}</h2>
        <p>{{ selectedUser.bio || "No bio available" }}</p>
      </div>
      <div class="liked-action-buttons-modal">
        <button class="liked-button liked-dislike-button" @click="handleUnlike(selectedUser.userId)">
          <i class="fas fa-times"></i>
        </button>
        <button
          class="liked-button liked-super-like-button"
          @click="handleSuperLike"
        >
          <i class="fas fa-star"></i>
        </button>
        <button class="liked-button liked-like-button" @click="handleLike(selectedUser.userId)">
          <i class="fas fa-heart"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllLikedMe } from "@/services/like-service";
import { ElNotification } from "element-plus";
import { swipeAction } from "@/services/swipe-service";

export default {
  name: "LikedBy",
  data() {
    return {
      likedMe: [],
      selectedUser: null,
      currentPhotoIndex: 0,
    };
  },
  methods: {
    async loadLikedMe() {
      try {
        // Fetch profiles that liked the current user
        const likedMeData = await getAllLikedMe();
        this.likedMe = likedMeData;
      } catch (error) {
        console.error("Error loading liked profiles:", error.message);
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
      // Show details of the selected user
      this.selectedUser = {
        ...user,
        photoUrls: user.photos.map((photo) => photo.url),
      };
      this.currentPhotoIndex = 0; // Reset photo index when a new user is selected
    },
    closeProfile() {
      // Close the detailed view of a selected user
      this.selectedUser = null;
    },
    prevPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex =
          (this.currentPhotoIndex - 1 + this.selectedUser.photoUrls.length) %
          this.selectedUser.photoUrls.length;
      }
    },
    nextPhoto() {
      if (this.selectedUser && this.selectedUser.photoUrls.length) {
        this.currentPhotoIndex =
          (this.currentPhotoIndex + 1) % this.selectedUser.photoUrls.length;
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
        this.closeProfile();
      } catch (error) {
        console.error("Error during like action:", error.message);
        ElNotification({
          title: "Error",
          message: "An error occurred while liking.",
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
        this.closeProfile();
      } catch (error) {
        console.error("Error during unlike action:", error.message);
        ElNotification({
          title: "Error",
          message: "An error occurred while unliking.",
          type: "error",
        });
      }
    },
  },
  mounted() {
    this.loadLikedMe(); // Load profiles when component is mounted
  },
};
</script>

<style scoped>
/* Container layout */
.liked-app-container {
  display: flex;
  height: 100vh;
}

/* Sidebar */
.liked-sidebarMatch {
  width: 45%;
  background-color: #f6f6f6;
  border-right: 1px solid #e0e0e0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  animation: liked-slideInLeft 0.6s ease-out;
}

.liked-sidebar-header {
  text-align: center;
  margin: 10px;
  font-size: 24px;
  font-weight: bold;
}

/* Grid Layout for Liked Profiles */
.liked-matches-grid {
  padding-bottom: 300px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding-top: 20px;
  animation: liked-fadeIn 0.8s ease-in-out;
}

.liked-match-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.liked-match-item:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  border-radius: 20px;
  padding: 10px;
}

.liked-match-image {
  width: 100px;
  height: 100px;
  border-radius: 20%;
  object-fit: cover;
  margin-bottom: 10px;
  animation: liked-popIn 0.5s ease-out;
}

.liked-match-name {
  font-size: 16px;
  font-weight: bold;
  font-family: Arial, Helvetica, sans-serif;
}

/* User details section */
.liked-user-details {
  width: 55%;
  background-color: #f9f9f9;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 15px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
  position: relative;
  animation: liked-slideIn 0.5s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.liked-user-photo {
  display: block;
  margin: 0 auto;
  width: 400px;
  height: 500px;
  object-fit: cover;
}

.liked-photo-gallery {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.liked-nav-button {
  background: none;
  border: none;
  font-size: 30px;
  color: #ff6699;
  cursor: pointer;
  margin: 0 10px;
  transition: color 0.3s;
}

.liked-nav-button:hover {
  color: #ff3399;
}

.liked-close-button {
  background-color: #ff5a5f;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 25px;
  width: 40px;
  height: 40px;
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

.liked-close-button:hover {
  background-color: #ff3338;
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.liked-info {
  text-align: left;
  width: 100%;
  padding: 20px;
  font-size: 20px;
}

.liked-info h2 {
  margin: 0;
  font-weight: bold;
  font-size: 25px;
  padding-bottom: 10px;
}

/* Action buttons styling */
.liked-action-buttons-modal {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

.liked-button {
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

.liked-dislike-button {
  background-color: #ff5a5f;
  box-shadow: 0 0 15px rgba(255, 90, 95, 0.3);
}

.liked-super-like-button {
  background-color: #3498db;
  box-shadow: 0 0 15px rgba(52, 152, 219, 0.3);
}

.liked-like-button {
  background-color: #2ecc71;
  box-shadow: 0 0 15px rgba(46, 204, 113, 0.3);
}

.liked-button:hover {
  transform: scale(1.15);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
}

.liked-button:active {
  transform: scale(0.95);
}

.liked-button i {
  font-size: 1.5rem;
  color: white;
}

@keyframes liked-slideInLeft {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes liked-fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes liked-popIn {
  from {
    transform: scale(0.7);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes liked-slideIn {
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
