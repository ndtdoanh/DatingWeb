<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <h1>Manage Payments</h1>

      <!-- Search bar -->
      <el-input
          v-model="searchQuery"
          placeholder="Search by Email or Payment Details"
          style="width: 300px;"
          @input="filterPayments"
      >
        <template #prepend>
          <el-button :icon="Search" />
        </template>
      </el-input>
    </div>

    <table>
      <thead>
        <tr>
          <th>Payment ID</th>
          <th>Email</th>
          <th>Amount</th>
          <th>Date</th>
          <th>Address</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="payment in filteredPayments" :key="payment.paymentId">
          <td>{{ payment.paymentId }}</td>
          <td>{{ payment.email }}</td>
          <td>{{ formatCurrency(payment.amount) }}</td>
          <td>{{ formatDate(payment.date) }}</td>
          <td>{{ payment.address != null ? payment.address : '-' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  getAllPayments,
} from '@/services/admin/admin-payment-service';
import { Search } from '@element-plus/icons-vue';
import { format } from 'date-fns';

const payments = ref([]);
const searchQuery = ref('');

// Filter payments based on search query
const filteredPayments = computed(() => {
  const query = searchQuery.value.toLowerCase();
  return payments.value.filter(payment =>
      payment.amount.toString().includes(query) ||
      payment.date.toLowerCase().includes(query)
  );
});

// Load payments when component is mounted
onMounted(async () => {
  await fetchPayments();
});

// Fetch all payments
const fetchPayments = async () => {
  try {
    payments.value = await getAllPayments();
  } catch (error) {
    ElMessage.error('Failed to fetch payments. Please try again later.');
  }
};

// Function to format date using date-fns
const formatDate = (date) => {
  return format(new Date(date), 'dd/MM/yyyy HH:mm:ss'); // Adjust the format string as necessary
};

// Format currency in VND
const formatCurrency = (amount) => {
  return amount.toLocaleString('vi-VN') + ' VNĐ';
};
</script>
