import { jwtDecode } from "jwt-decode"; // Import thư viện jwt-decode để giải mã JWT token
import { instance, Login } from "./api-instance-provider"; // Import `instance` để gọi API và `Login` cho URL

// Hàm lưu token, email, role và userId vào localStorage
const saveTokenAndUserInfo = (token, email, role, name, plan) => {
  if (token) {
    localStorage.setItem("userToken", token);
    localStorage.setItem("email", email);
    localStorage.setItem("role", role);
    localStorage.setItem("name", name);
    localStorage.setItem("plan", plan);

    // Giải mã token để lấy userId
    const decodedToken = jwtDecode(token);
    const userId = decodedToken.userId;

    if (userId) {
      localStorage.setItem("userId", userId); // Lưu userId vào localStorage
    }
  }
};

// Phương thức xử lý đăng nhập
import { ElNotification } from 'element-plus';

export const loginUser = async (email, password) => {
  try {
    // Gửi yêu cầu đăng nhập với email và password
    const response = await instance.post(Login.ORIGIN, { email, password });
    const { status, message, data } = response.data;

    // Kiểm tra nếu đăng nhập thành công
    if (status === 200) {
      const { token, role, name, plan } = data;

      // Lưu token và thông tin user vào localStorage
      saveTokenAndUserInfo(token, email, role, name, plan);

      // Xử lý theo vai trò của người dùng
      if (role === "Admin") {
        ElNotification({
          title: 'Success',
          message: 'Admin login successful!',
          type: 'success',
        });
        return "Admin login"; // Điều hướng tới layout admin
      }
      console.log(message)
      // Xử lý trường hợp đăng nhập cho User
      if (message.startsWith("First login")) {
        ElNotification({
          title: 'Notice',
          message: 'First login detected, please change your password.',
          type: 'warning',
        });
        return "First login"; // Điều hướng tới trang đổi mật khẩu
      }

      if (message.startsWith("Second login")) {
        ElNotification({
          title: 'Notice',
          message: 'Second login detected, please update your profile.',
          type: 'warning',
        });
        return "Second login"; // Điều hướng tới trang cập nhật hồ sơ
      }

      if (message === "Login successful") {
        ElNotification({
          title: 'Success',
          message: 'Login successful!',
          type: 'success',
        });
        return "Login successful"; // Điều hướng tới trang homePage
      }

      ElNotification({
        title: 'Unknown',
        message: 'Unknown response from server.',
        type: 'error',
      });
      return "Unknown"; // Trả về nếu thông báo không rõ ràng
    }

    // Nếu đăng nhập thất bại
    ElNotification({
      title: 'Error',
      message: message || 'Đăng nhập thất bại',
      type: 'error',
    });
    return "Failed"; // Trả về nếu đăng nhập thất bại

  } catch (error) {
    const status = error?.response?.status;
    const errorMessage = error?.response?.data?.message || "Failed to login";

    // Xử lý lỗi từ server (ví dụ: 403 Forbidden)
    if (status === 403) {
      ElNotification({
        title: 'Error',
        message: errorMessage,
        type: 'error',
      });
      return "Error"; // Trả về nếu lỗi từ server
    }

    // Xử lý lỗi chung trong quá trình request
    ElNotification({
      title: 'Error',
      message: 'Login failed! Please check your email and password.',
      type: 'error',
    });
    return "Error"; // Trả về nếu có lỗi khác
  }
};

