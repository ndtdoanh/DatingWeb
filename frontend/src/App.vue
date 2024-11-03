<script setup>
import { RouterLink, RouterView } from 'vue-router';
import './assets/common.scss';
import './assets/element-ui.scss';
import Snowflakes from "@/views/layout/Snowflakes.vue";
import Footer from "@/views/layout/AppFooter.vue"; // Import Footer
import SpeedDial from "@/views/SpeedDial.vue";
import UserNotification from "@/views/UserNotification.vue";
import 'bootstrap/dist/css/bootstrap.min.css'; // Import Bootstrap CSS
</script>

<template>
  <header>
    <div class="wrapper">
      <nav>
        <RouterLink to="/homePage"></RouterLink>
      </nav>
    </div>
  </header>

  <UserNotification />

  <RouterView />
  <Snowflakes></Snowflakes>
  <SpeedDial />

  <!-- Thêm Footer vào đây -->
  <Footer></Footer>
</template>
