<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <h1>Manage Users</h1>
      <!-- Search bar -->
      <el-input
          v-model="searchQuery"
          placeholder="Search by Email, Phone"
          style="width: 300px;"
          @input="filteredUsers"
      >
        <template #prepend>
          <el-button :icon="Search" />
        </template>
      </el-input>
    </div>

    <table>
      <thead>
      <tr>
        <th>ID</th>
        <th>Email</th>
        <th>Name</th>
        <th>Address</th>
        <th>Status</th>
        <th>Role Name</th>
        <th>Package Name</th>
        <th style="width: 350px">Operations</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="user in filteredUsers" :key="user.userId">
        <td>{{ user.userId }}</td>
        <td>{{ user.email }}</td>
        <td>{{ user.name }}</td>
        <td>{{ user.address }}</td>
        <td>{{ user.status }}</td>
        <td>{{ user.roleName }}</td>
        <td>{{ user.packageName }}</td>
        <td>
          <el-button type="primary" @click="viewUserDetails(user)">View</el-button>
          <el-button type="danger" @click="lockOrUnLockUser(user.userId)">
            {{ user.status === 'INACTIVE' ? 'Activate' : 'Deactivate' }}
          </el-button>
          <el-button type="warning" @click="openChangePackageDialog(user)">Change Package</el-button>

        </td>
      </tr>
      </tbody>
    </table>

    <!-- User Details Modal -->
    <el-dialog v-model="isDialogVisible" width="50%" title="User Details">
      <div v-if="selectedUser">
        <el-form label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="ID">
                <el-input v-model="selectedUser.userId" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Email">
                <el-input v-model="selectedUser.email" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- Additional form fields here -->

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Status">
                <el-switch v-model="selectedUser.status"
                           active-value="ACTIVE"
                           inactive-value="INACTIVE"
                           disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Role">
                <el-input v-model="selectedUser.roleName" disabled />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Bio">
                <el-input v-model="selectedUser.bio" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Gender">
                <el-input v-model="selectedUser.gender" disabled />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Phone">
                <el-input v-model="selectedUser.phone" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Package Name">
                <el-input v-model="selectedUser.packageName" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="Photos">
            <el-row :gutter="20">
              <el-col v-for="url in selectedUser.photoUrls" :key="url" :span="6">
                <el-image
                    :src="url"
                    fit="cover"
                    :preview-src-list="selectedUser.photoUrls"
                    lazy
                />
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>
      <span class="dialog-footer">
        <el-button @click="isDialogVisible = false">Close</el-button>
      </span>
    </el-dialog>

    <el-dialog v-model="changePackageDialogVisible" title="Change User Package" width="30%">
      <el-form>
        <el-form-item label="Select New Package">
          <el-select v-model="selectedPackage" placeholder="Select package">
            <el-option v-for="pkg in availablePackages" :key="pkg.planId" :label="pkg.name" :value="pkg.planId" />
          </el-select>
        </el-form-item>
      </el-form>
      <span class="dialog-footer">
    <el-button type="success" @click="changeUserPackage">Update</el-button>
    <el-button type="info" @click="changePackageDialogVisible = false">Cancel</el-button>
  </span>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref, computed} from 'vue';
import {ElMessage, ElNotification} from 'element-plus';
import {
  changeUserPackageAPI,
  getAllUsers,
  lockOrUnLockUser as lockOrUnLockUserAPI
} from '@/services/admin/admin-user-service';
import {
  getAllSubscriptionPlans
} from '@/services/admin/admin-subscriptionPlan-service.js';
import {Search} from "@element-plus/icons-vue";

const users = ref([]);
const searchQuery = ref('');
const isDialogVisible = ref(false);
const selectedUser = ref(null);
const changePackageDialogVisible = ref(false);
const selectedPackage = ref('');
const selectedUserForPackageChange = ref(null);
const availablePackages = ref([]);

// Computed property to filter users based on search
const filteredUsers = computed(() => {
  const query = searchQuery.value.toLowerCase();
  return users.value.filter(user =>
      user.email.toLowerCase().includes(query) ||
      user.name?.toLowerCase().includes(query)
  );
});

// Load users when component is mounted
onMounted(async () => {
  await fetchUsers();
  try {
    availablePackages.value = await getAllSubscriptionPlans();
  } catch (error) {
    console.error('Error loading packages:', error.message);
  }
});

// Fetch users
const fetchUsers = async () => {
  try {
    users.value = await getAllUsers();
  } catch (error) {
    ElMessage.error('Failed to fetch users. Please try again later.');
  }
};

// Toggle user status (Activate/Deactivate)
const lockOrUnLockUser = async (id) => {
  const action = users.value.find(user => user.userId === id).status === 'INACTIVE' ? 'Activate' : 'Deactivate';
  if (confirm(`Are you sure you want to ${action} this user?`)) {
    try {
      await lockOrUnLockUserAPI(id);
      await fetchUsers();
      ElNotification({
        title: 'Success',
        message: `User successfully ${action}d.`,
        type: 'success',
      });
    } catch (error) {
      ElNotification({
        title: 'Error',
        message: error?.message,
        type: 'error',
      });
    }
  }
};

// View user details
const viewUserDetails = (user) => {
  selectedUser.value = user;
  isDialogVisible.value = true;
};

const openChangePackageDialog = (user) => {
  selectedUserForPackageChange.value = user;
  selectedPackage.value = user.packageName; // Gán gói hiện tại của người dùng
  changePackageDialogVisible.value = true;
};

const changeUserPackage = async () => {
  const planId = selectedPackage.value;

  if (planId === selectedUserForPackageChange.value.packageName) {
    ElNotification({
      title: 'Warning',
      message: 'Please select another package type.',
      type: 'warning',
    });
    return;
  }

  try {
    await changeUserPackageAPI(selectedUserForPackageChange.value.userId, planId);
    await fetchUsers();
    ElNotification({
      title: 'Success',
      message: 'Package changed successfully.',
      type: 'success',
    });
    changePackageDialogVisible.value = false;
  } catch (error) {
    ElNotification({
      title: 'Error',
      message: error?.message,
      type: 'error',
    });
  }
};
</script>
