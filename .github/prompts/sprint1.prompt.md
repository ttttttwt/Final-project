---
mode: agent
---
Tôi đang làm việc trên dự án LEXIA Backend (Spring Boot 3.x, Java 17, PostgreSQL, JWT, Gemini API). Hãy giúp tôi review lại các task đã hoàn thành trong sprint hiện tại.

**Bước chuẩn bị:**
1. Đọc file `docs/plan/current-sprint-status.md` để hiểu trạng thái sprint hiện tại và danh sách task.
2. Đọc file `docs/implement/sprint-X/daily-log.md` để xem nhật ký hàng ngày.
3. Đọc các file session summary trước đó (ví dụ: `docs/implement/sprint-X/session-X-*.md`) để hiểu tiến độ tích lũy.
4. Kiểm tra kết quả test gần nhất: Chạy `./gradlew test` và đảm bảo tất cả test PASS với coverage ≥ 70%.

**Yêu cầu review:**
- Liệt kê tất cả các task đã hoàn thành trong sprint này, bao gồm:
  - Mô tả task (từ current-sprint-status.md).
  - Ngày hoàn thành (từ daily-log.md).
  - Files/code đã tạo/sửa đổi (với số dòng code nếu có thể).
- Đánh giá chất lượng cho từng task:
  - Code compiles và chạy được không?
  - Tests PASS với coverage ≥ 70% (Services ≥ 80%)?
  - Tuân thủ CODE-STANDARDS.md (security, package structure, JavaDoc)?
  - API response < 500ms? Không có security issues?
- Xác định các vấn đề còn lại hoặc cần cải thiện (nếu có).
- Tạo một session summary mới trong `docs/implement/sprint-X/session-X-review.md` với cấu trúc sau:
  1. **What We Accomplished**: Danh sách chi tiết các task hoàn thành.
  2. **Code Generated**: Files tạo/sửa với metrics (LOC, coverage).
  3. **Key Decisions**: Top 3 quyết định kiến trúc quan trọng.
  4. **Challenges Faced**: Vấn đề gặp phải và giải pháp.
  5. **Quality Assessment**: Đánh giá 1-10 với giải thích chi tiết.
  6. **Best Prompts Used**: Prompts hiệu quả để tái sử dụng.
  7. **Next Steps**: Roadmap rõ ràng cho session tiếp theo.
- Cập nhật `docs/plan/current-sprint-status.md` để đánh dấu task hoàn thành và chuyển sang task tiếp theo.
- Đảm bảo commit với conventional format (ví dụ: "feat: complete task X - add JWT login").

**Lưu ý**: Chỉ review task đã hoàn thành, không làm task mới. Nếu có lỗi, ưu tiên fix trước khi commit.