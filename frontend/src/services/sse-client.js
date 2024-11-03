// sse-client.js
export function connectSSE(callback) {
  const uri = process.env.VITE_PUBLIC_BASE_URI != null ? process.env.VITE_PUBLIC_BASE_URI : "http://localhost:8088";
  const eventSource = new EventSource(uri + "/api/subscribe"); // Thay đổi URL nếu cần

  eventSource.onmessage = function(event) {
    console.log("Notification:", event.data);
    callback(event.data); // Gọi hàm callback với dữ liệu sự kiện
  };

  eventSource.onerror = function(error) {
    console.error("SSE connection error:", error);
  };
}
