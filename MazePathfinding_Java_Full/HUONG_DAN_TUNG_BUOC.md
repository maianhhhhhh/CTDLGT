# HƯỚNG DẪN TỪNG BƯỚC CODE ĐỀ TÀI CTDLGT (JAVA 21 + VS CODE)

## Bạn sẽ làm được gì?

Hoàn thiện một chương trình Java Swing cho phép người dùng tạo mê cung, chạy BFS / DFS / Dijkstra / A*, theo dõi đường đi từng bước, đo hiệu năng và so sánh bằng bảng cùng biểu đồ. **Bộ ZIP đã chứa toàn bộ source code hoàn chỉnh.** Tài liệu này hướng dẫn cách đọc, chạy, hiểu và tự chỉnh sửa từng phần.

## BƯỚC 0 — Chuẩn bị và chạy dự án (15–20 phút)

Bạn đã cài `java 21` và `javac 21`, nên không cần cài lại.

1. Giải nén ZIP vào `D:\CTDLGT\MazePathfinding_Java_Full`.
2. VS Code → File → Open Folder → chọn `D:\CTDLGT\MazePathfinding_Java_Full`.
3. Cài Extension Pack for Java của Microsoft (nếu chưa có).
4. Mở Terminal và chạy `.\run.bat`, hoặc mở `src/maze/Main.java`, nhấn `Run`.
5. Thử tạo tường, chạy BFS, rồi bấm So sánh cả 4.
6. Kiểm thử bằng `.\test.bat`; kết quả kỳ vọng có `[OK]` và `PASS`.

Nếu bạn mở thư mục **CTDLGT cha**, hãy vào Terminal:

```powershell
cd D:\CTDLGT\MazePathfinding_Java_Full
.\run.bat
```

> Không chạy lệnh cũ `javac -d out src/Main.java` nữa: phiên bản mới có nhiều file và `Main.java` nằm tại `src/maze/Main.java`.

---

## BƯỚC 1 — Hiểu cấu trúc dữ liệu (TV1/TV2, 30 phút)

**Mở:** `src/maze/model/Cell.java` và `Maze.java`.

- `Cell(int row, int col)` là tọa độ một ô. Trong Java 21, `record` tự cung cấp `equals`, `hashCode`; dùng an toàn làm key trong `HashMap` / `HashSet`.
- `int[][] terrain` là ma trận: `0` = ô thường, `1` = tường, `2` = ô chi phí cao.
- `start`, `end` là tọa độ người đi bắt đầu/kết thúc.
- `maze.neighbors(cell)` trả về tối đa 4 ô có thể đi (không đi chéo).
- `maze.enterCost(next)` trả `1` nếu ô thường, `5` nếu ô trọng số.

**Bài tự làm:** Thay đổi `WEIGHTED_COST` từ `5` thành `10`, chạy lại và quan sát chi phí của Dijkstra/A*. Đổi lại 5 sau khi thử.

---

## BƯỚC 2 — Thống nhất đầu ra của thuật toán (cả nhóm, 20 phút)

**Mở:** `algorithms/Pathfinding.java`, `model/SearchResult.java`, `algorithms/SearchUtils.java`.

Mọi thuật toán đều triển khai:

```java
public interface Pathfinding {
    String name();
    SearchResult search(Maze maze);
}
```

Mọi kết quả đều có:

- `path`: danh sách các ô của đường đi (Start → End).
- `visitOrder`: thứ tự ô được *lấy ra để mở rộng* (để hoạt ảnh).
- `pathCost`: tổng chi phí đi qua các ô (không tính Start).
- `nanos`: thời gian xử lý từ lúc bắt đầu tới khi dựng xong kết quả.
- `steps()`: số cạnh/di chuyển = `path.size()-1` nếu tìm được đường.

`SearchUtils.result(...)` dùng map `parent` để lần ngược từ End về Start rồi đảo chiều. Một hàm chung giúp cả 4 thuật toán thống nhất cách tính đường đi và chi phí.

---

## BƯỚC 3 — BFS: Thuật toán cơ sở (TV2, 45 phút)

**Mở:** `algorithms/BFS.java`.

1. Dùng `Queue<Cell>` với `ArrayDeque` để lấy phần tử vào trước ra trước (FIFO).
2. Đưa Start vào queue, đánh dấu đã thấy (`seen`).
3. Mỗi vòng lặp, lấy `current = queue.remove()` và lưu vào `expanded`.
4. Nếu `current == End` thì dừng.
5. Nếu chưa tới đích, duyệt từng hàng xóm đi được, chưa thấy thì đánh dấu, lưu `parent[next] = current`, thêm vào queue.
6. Dùng `SearchUtils.result` dựng kết quả.

**Vì sao BFS tìm đường ít bước nhất?** BFS khám phá lần lượt các lớp khoảng cách 0, 1, 2... cạnh, do đó khi lần đầu lấy End ra khỏi hàng đợi thì đã tìm được đường ít cạnh nhất trong đồ thị không trọng số.

**Bài tự làm:** Trong VS Code, đặt breakpoint tại `queue.remove()` và xem từng bước hàng đợi thay đổi.

---

## BƯỚC 4 — DFS: Thuật toán duyệt sâu (TV2, 30 phút)

**Mở:** `algorithms/DFS.java`.

Khác BFS: thay **Queue** bằng **Stack** (`ArrayDeque.push/pop`), mỗi lần ưu tiên đi sâu một nhánh. Có thể tìm đường đi nhưng **không đảm bảo đường ít bước nhất**.

**Bài tự làm:** Vẽ 2 lối từ Start tới End: một lối ngắn và một lối vòng dài; so sánh kết quả BFS và DFS. Đổi thứ tự các hướng trong `Maze.neighbors` để thấy đường DFS thay đổi.

---

## BƯỚC 5 — Dijkstra: Đường đi có chi phí nhỏ nhất (TV3, 60 phút)

**Mở:** `algorithms/Dijkstra.java`.

- Dùng mảng `dist[row][col]` lưu chi phí tốt nhất đã biết từ Start đến từng ô.
- Dùng `PriorityQueue` luôn lấy ô có khoảng cách tốt nhất hiện tại ra trước.
- Khi tới ô bên cạnh `next`, tính `newCost = dist[current] + maze.enterCost(next)`.
- Nếu `newCost` nhỏ hơn chi phí cũ, cập nhật `dist`, `parent` và đẩy trạng thái mới vào hàng đợi ưu tiên.
- Khi End được lấy ra với chi phí tối ưu, dựng đường đi.

**Thử nghiệm cực kỳ quan trọng cho báo cáo:**

Tạo mê cung trống, đặt Start ở `(2,1)` và End ở `(2,5)`. Đánh dấu 3 ô liên tiếp `(2,2)`, `(2,3)`, `(2,4)` với chi phí 5.

- BFS đi thẳng: **4 bước, chi phí 16**.
- Dijkstra đi vòng qua hàng trên/dưới: **6 bước, chi phí 6**.

Nó chứng minh: **ít bước nhất** không phải lúc nào cũng là **ít chi phí nhất**.

---

## BƯỚC 6 — A*: Cải tiến hướng về đích (TV3, 45 phút)

**Mở:** `algorithms/AStar.java`.

A* lưu:

- `g`: chi phí thực tế từ Start đến ô hiện tại.
- `h`: ước lượng từ ô hiện tại tới End, sử dụng khoảng cách Manhattan.
- `f = g + h`: độ ưu tiên trong PriorityQueue.

```java
int h = Math.abs(row - endRow) + Math.abs(col - endCol);
int f = g + h;
```

Vì chi phí nhỏ nhất của một bước là 1 và không đi chéo, ước lượng Manhattan không vượt chi phí thật. Với cài đặt hiện tại, A* vẫn bảo đảm tối ưu tổng chi phí.

**Bài tự làm:** Chạy Dijkstra và A* trên mê cung lớn, ghi chép số ô mở rộng (Expanded) và thời gian. A* thường hướng về đích hiệu quả hơn, nhưng kết quả phụ thuộc mê cung và cách giải quyết nút bằng nhau.

---

## BƯỚC 7 — Lập trình giao diện Java Swing (TV1, 60–90 phút)

**Mở:** `ui/MainFrame.java`, `ui/MazePanel.java`, `ui/EditMode.java`.

| Thành phần | Cách hoạt động |
| --- | --- |
| `JFrame` | Cửa sổ chính |
| `JPanel` | Vùng vẽ mê cung |
| `paintComponent(Graphics)` | Tô màu từng ô dựa trên trạng thái mê cung và hoạt ảnh |
| `MouseAdapter` | Đón click/drag chuột để sửa ô |
| `JComboBox` | Chọn thuật toán, kích thước, chế độ vẽ |
| `JSlider` | Tăng/giảm số ô được vẽ mỗi frame |
| `javax.swing.Timer` | Phát lại `visitOrder` và `path` từng bước trên giao diện |
| `JTable` | Bảng so sánh |
| `SwingWorker` | Chạy đo so sánh ở luồng nền, tránh treo giao diện |

**Tại sao thuật toán tìm đường và vẽ hoạt ảnh phải tách nhau?**

Nếu thuật toán tự `sleep()` để vẽ từng ô, thời gian xử lý sẽ bị trộn với thời gian animation. Ở dự án này thuật toán chạy tính toán trước, trả về `visitOrder` và `path`, sau đó Swing Timer mới tô dần. Như vậy thời gian so sánh phản ánh phần thuật toán.

**Bài tự làm:** Trong `MazePanel.java`, thay màu `EXPLORED` và `PATH`; đổi giá trị slider ban đầu từ `8` thành `12` trong `MainFrame.java`.

---

## BƯỚC 8 — Sinh mê cung và kiểm thử (TV4, 45 phút)

**Mở:** `util/MazeGenerator.java` và `tests/maze/tests/AlgorithmTests.java`.

`MazeGenerator` tạo mê cung bằng kỹ thuật randomized depth-first carving: khởi đầu toàn tường, đào đường nối các ô lẻ bằng stack; thêm vài nhánh vòng để có nhiều đường lựa chọn; tùy chọn đánh dấu các ô chi phí 5.

Chạy:

```powershell
.\test.bat
```

Các kiểm thử gồm:

1. Mê cung trống: BFS đi đúng số bước ngắn nhất.
2. Mê cung trọng số: Dijkstra và A* tối ưu chi phí, BFS tối ưu bước.
3. Bị chặn hoàn toàn: các thuật toán trả kết quả không có đường.
4. Sinh mê cung ngẫu nhiên nhiều lần: luôn tìm được đường; Dijkstra và A* có chi phí tối ưu bằng nhau.

**Bài tự làm:** Bổ sung bài test nếu thay Start và End sang hai vị trí bất kỳ ở trung tâm mê cung.

---

## BƯỚC 9 — So sánh hiệu năng công bằng (TV4, 40 phút)

**Mở:** `util/Benchmark.java`, `ui/ComparisonChartPanel.java`.

- Lấy một bản sao `Maze snapshot` trước khi benchmark.
- Mỗi thuật toán chạy 5 lần khởi động (warm-up) và 30 lần đo.
- Dùng `System.nanoTime()` trong thuật toán; tính thời gian trung bình (ms).
- Báo cáo `Steps`, `Path Cost`, `Expanded Nodes`, `Average Time`.
- Bảng và biểu đồ không bao gồm thời gian Swing repaint.

**Hạn chế cần nêu trong báo cáo:** Thời gian máy tính ở mức micro/millisecond dao động do JIT, GC, CPU; để kết luận tốt nên chạy nhiều kích thước, lặp nhiều mê cung và so sánh thêm số ô mở rộng.

**Khuyến nghị bộ case thuyết trình:** Mê cung trống · Nhiều tường · Không có đường · Nhiều ô trọng số · 4 kích thước.

---

## BƯỚC 10 — Tổng hợp nhóm / GitHub / báo cáo (cả nhóm)

**Cách dùng Git nhóm đơn giản:**

```powershell
git init
git add .
git commit -m "Initial Java maze pathfinding project"
```

- TV1: nhánh `feature/swing-ui`.
- TV2: nhánh `feature/bfs-dfs`.
- TV3: nhánh `feature/dijkstra-astar`.
- TV4: nhánh `feature/benchmark-tests`.
- Trước khi merge: chạy `.\test.bat` trên máy từng người.

**Báo cáo cuối:**

1. Giới thiệu đề tài.
2. Yêu cầu chức năng, phạm vi, công nghệ.
3. Cấu trúc dữ liệu và thuật toán (pseudocode, độ phức tạp).
4. Sơ đồ kiến trúc, sơ đồ lớp, luồng xử lý.
5. Demo chức năng, bảng và biểu đồ thử nghiệm.
6. Đánh giá BFS/DFS/Dijkstra/A*, hạn chế và hướng phát triển.

## Tóm tắt logic cốt lõi

```text
Người dùng (chỉnh mê cung)
     |
     v
Maze (int[][] terrain; Start; End)
     |
     v
Pathfinding.search(Maze.copy())
     |
     v
SearchResult { path, visitOrder, pathCost, nanos }
     |                             |
     v                             v
Swing Timer (mô phỏng)        Benchmark (30 lần)
     |                             |
     v                             v
MazePanel (vẽ)                JTable + biểu đồ
```

**Lưu ý:** Mã nguồn trong ZIP là sản phẩm chạy được để nhóm đọc và thực hành. Khi nộp đề tài, mỗi thành viên nên tự giải thích được dữ liệu đầu vào, cấu trúc được sử dụng, cách truy vết `parent`, độ phức tạp và lý do thuật toán cho kết quả như vậy.
