import { instance } from './api-instance-provider';

// Hàm thêm mới contact form
export const insertContactForm = async (contactFormData) => {
  try {
    const response = await instance.post('/contact-form', contactFormData, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('userToken')}`, // Gửi token trong header
        'Content-Type': 'application/json', // Đảm bảo định dạng JSON
      },
    });
    return response.data; // Trả về data nếu API trả về đúng định dạng
  } catch (error) {
    throw error.response ? error.response.data : { message: 'An error occurred while submitting the contact form' };
  }
};