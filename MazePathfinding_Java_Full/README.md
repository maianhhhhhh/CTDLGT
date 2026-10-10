# Đề tài CTDLGT: Mô phỏng và so sánh thuật toán tìm đường trong mê cung

**Ngôn ngữ:** Java 21 · **Giao diện:** Java Swing · **IDE:** Visual Studio Code · **Không cần thư viện ngoài**.

Ứng dụng có 4 thuật toán BFS, DFS, Dijkstra và A*, mê cung tùy chỉnh/được sinh tự động, địa hình có trọng số, hoạt ảnh tìm đường, tạm dừng/tiếp tục, bảng và biểu đồ so sánh.

## 1. Chạy trên Windows (đơn giản nhất)

1. Cài OpenJDK 21 và VS Code + Extension Pack for Java.
2. Giải nén thư mục `MazePathfinding_Java_Full` vào `D:\CTDLGT`.
3. Mở thư mục dự án trong VS Code: **File → Open Folder**.
4. Trong **Terminal → New Terminal**, gõ:

   ```powershell
   .\run.bat
   ```

   Hoặc mở `src/maze/Main.java` rồi nhấn **Run** ngay trên VS Code.

5. Chạy tự động kiểm thử:

   ```powershell
   .\test.bat
   ```

**Đã sửa `run.bat`/`test.bat` (10/10/2026):** Biên dịch trực tiếp bằng `-sourcepath` thay cho `sources.txt`, tránh lỗi mất dấu `\` trong đường dẫn Windows khi `javac` đọc tệp `@argfile`.

**Nếu muốn biên dịch thủ công trên PowerShell:**

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sources = @(Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 -d out $sources
java -cp out maze.Main
```

Để chạy kiểm thử thủ công, thêm cả các file `tests` vào biến `$sources`, biên dịch và chạy `java -ea -cp out maze.tests.AlgorithmTests`.

## 2. Cách sử dụng

- **Chọn thuật toán**: BFS, DFS, Dijkstra hoặc A*.
- **Chế độ vẽ**: Tường, Vùng chi phí 5, đặt Start, đặt End, Xóa ô.
- **Chuột trái**: thao tác theo chế độ vẽ. Có thể giữ và kéo để vẽ tường.
- **Chuột phải**: xóa tường/ô trọng số.
- **Phím S/E/W/R**: đổi chế độ đặt Start / End / Wall / Erase.
- **Sinh mê cung**: tạo mê cung ngẫu nhiên bảo đảm có đường đi; tùy chọn có ô chi phí 5.
- **Mê cung trống**: xóa mọi tường, tạo mê cung mới theo kích thước đã chọn.
- **Chạy thuật toán**: xem quá trình khám phá ô (xanh dương) và đường tìm được (vàng).
- **Tạm dừng**: ngưng/tiếp tục hoạt ảnh.
- **Tốc độ**: số ô được vẽ trên mỗi khung hình (không ảnh hưởng số liệu thời gian xử lý).
- **So sánh cả 4**: chạy mỗi thuật toán 5 lần warm-up + 30 lần đo; cập nhật bảng và biểu đồ.

**Màu:** Xanh lá = Start · Đỏ = End · Xanh lam = ô đã mở rộng · Vàng = đường đi · Xanh đậm = tường · Cam nhạt = ô chi phí 5 · Trắng = ô bình thường.

## 3. Cấu trúc dữ liệu & kiến thức CTDLGT

| Thuật toán | Cấu trúc dữ liệu | Độ phức tạp (đồ thị ô vuông, V ô, E cạnh) | Đảm bảo |
| --- | --- | --- | --- |
| BFS | Queue (`ArrayDeque`) | O(V + E) | Ít bước nhất, không xét trọng số |
| DFS | Stack (`ArrayDeque`) | O(V + E) | Tìm một đường đi, có thể không ngắn nhất |
| Dijkstra | Min PriorityQueue | O((V + E) log V) | Tổng chi phí tối thiểu với trọng số không âm |
| A* | Min PriorityQueue + heuristic Manhattan | Thường hiệu quả hơn; trường hợp xấu có thể mở rộng nhiều nút, chi phí phụ thuộc cách cài đặt | Tổng chi phí tối thiểu với heuristic admissible và consistent |

- Bốn hướng di chuyển: phải, xuống, trái, lên, **không đi chéo**.
- Mỗi bước vào ô trắng tốn **1**, ô màu cam tốn **5**; không tính chi phí vị trí Start.
- Khi mọi ô đều chi phí 1, BFS / Dijkstra / A* cùng tối ưu theo số bước.
- Khi có chi phí 5, BFS chỉ tối ưu *số bước*, còn Dijkstra và A* tối ưu *tổng chi phí*.
- Heuristic A*: `h = |row - endRow| + |col - endCol|` nhân với chi phí tối thiểu là 1. Do đó không ước lượng quá mức.
- **Mở rộng (Expanded)** = số ô lấy ra khỏi hàng đợi/ngăn xếp/hàng đợi ưu tiên để xử lý, khác với số ô mới được đưa vào cấu trúc dữ liệu.
- **Thời gian (ms)** chỉ đo từ lúc bắt đầu thuật toán đến khi dựng xong kết quả, **không bao gồm hoạt ảnh**. Kết quả có dao động theo tốc độ CPU, JIT và tải hệ thống.
- So sánh chạy các thuật toán trên **cùng một snapshot** của mê cung.

## 4. Các file và người phụ trách đề xuất

| Vai trò | File/module | Nhiệm vụ |
| --- | --- | --- |
| TV1 | `ui/MainFrame.java`, `ui/MazePanel.java`, `ui/EditMode.java`, `Main.java` | Swing, tương tác chuột, mô phỏng |
| TV2 | `algorithms/BFS.java`, `DFS.java`, `model/Cell.java`, `Maze.java` | Queue, Stack, cấu trúc dữ liệu mê cung |
| TV3 | `algorithms/Dijkstra.java`, `AStar.java`, `AlgorithmFactory.java` | Trọng số, PriorityQueue, heuristic |
| TV4 | `util/MazeGenerator.java`, `Benchmark.java`, `ui/ComparisonChartPanel.java`, `tests/...` | Sinh mê cung, thống kê, kiểm thử |

Các file dùng chung: `algorithms/Pathfinding.java`, `SearchUtils.java`, `model/SearchResult.java`. Thống nhất hợp đồng kiểu dữ liệu trước khi tự thay đổi code.

## 5. Tài liệu báo cáo

Có thể triển khai bố cục:
1. Lý do chọn đề tài, mục tiêu, phạm vi.
2. Kiến thức cấu trúc dữ liệu: đồ thị, ma trận, Stack, Queue, PriorityQueue.
3. Phân tích 4 thuật toán; pseudocode và độ phức tạp.
4. Thiết kế hệ thống: mô hình dữ liệu, giao diện, sơ đồ lớp, luồng xử lý.
5. Kiểm thử trên: mê cung trống, chướng ngại, không có đường đi, mê cung có trọng số, kích cỡ khác nhau.
6. Bảng/biểu đồ số liệu, so sánh lợi thế và hạn chế; kết luận.

## 6. Lộ trình tự học code theo thứ tự

1. `model/Cell.java`, `Maze.java`: học ma trận, hàng-cột và tập láng giềng.
2. `algorithms/Pathfinding.java`, `BFS.java`: hàng đợi và truy vết `parent`.
3. `algorithms/DFS.java`: stack và thứ tự duyệt.
4. `algorithms/Dijkstra.java`: `dist[][]`, cập nhật chi phí, min-heap.
5. `algorithms/AStar.java`: `g`, `h`, `f`.
6. `ui/MazePanel.java`: `paintComponent`, chuột và màu.
7. `ui/MainFrame.java`: `Swing Timer`, nút, bảng so sánh.
8. `util/MazeGenerator.java`, `Benchmark.java`, `tests/...`: kiểm thử, tối ưu, viết báo cáo.

## 7. Các lưu ý kỹ thuật

- Java 21 cần biên dịch với `-encoding UTF-8` do source và giao diện tiếng Việt.
- Java Swing có sẵn trong JDK, **không cần Maven, JavaFX hay cài thư viện đồ thị**.
- Không sửa trực tiếp cấu trúc mê cung trong thuật toán: luôn dùng `Maze.copy()` cho quá trình chạy.
- Benchmark cho mục tiêu học tập; thời gian dưới 1 ms có thể biến động lớn, nên so sánh thêm `Steps`, `Cost`, `Expanded`.
- Dùng GitHub để mỗi thành viên làm việc trên nhánh (branch) riêng, kiểm tra `test.bat` trước khi merge.
