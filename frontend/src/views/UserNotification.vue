<template>
  <div>
  </div>
</template>

<script>
import { onMounted } from 'vue';
import { connectSSE } from '@/services/sse-client';
import { ElNotification } from 'element-plus';

export default {
  name: 'UserNotification',
  setup() {
    onMounted(() => {
      console.log("UserNotification component mounted");

      const currentUserId = localStorage.getItem("userId"); // Lấy userID của người dùng hiện tại
      connectSSE((message) => {
        const messageJson = JSON.parse(message);
        console.log("Received message:", messageJson);

        // Kiểm tra nếu userId của message trùng với userId của người dùng hiện tại
        if (messageJson.userId === currentUserId) {
          localStorage.setItem("plan", messageJson.package);
          const role = localStorage.getItem("role");

          if (role !== "admin") {
            ElNotification({
              title: 'Notification',
              message: "Your package has been updated to: " + messageJson.package,
              type: 'success',
              duration: 5000,
              onClose: () => {
                // Reload màn hình sau khi thông báo đóng
                window.location.reload();
              }
            });
          }
        }
      });
    });
  }
};
</script>
