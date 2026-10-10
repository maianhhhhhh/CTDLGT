# Maze Pathfinding Visualizer — Java Swing Starter

Đây là phiên bản khởi đầu cho đề tài CTDLGT: mô phỏng và so sánh thuật toán tìm đường trong mê cung.

## Yêu cầu
- JDK 21+ (hoặc JDK 17+)
- Visual Studio Code + Extension Pack for Java

## Chạy trong VS Code
1. Giải nén và mở **thư mục** `MazePathfinding_Java_Starter` trong VS Code.
2. Mở `src/Main.java` rồi nhấn **Run** ở trên hàm `main`.

Hoặc chạy trong terminal tại thư mục dự án:

```bash
javac -d out src/Main.java
java -cp out Main
```

## Điều khiển
- Chuột trái: thêm tường
- Chuột phải: xóa tường
- Run BFS: mô phỏng cách BFS thăm các ô và hiện đường ngắn nhất
- Reset: xóa tường và trạng thái tìm kiếm

## Lưu ý
Đây chỉ là **MVP bước 1**: có giao diện Java Swing, tạo/xóa vật cản, BFS và thống kê cơ bản.
Nó **chưa** có DFS, Dijkstra, A*, sinh mê cung ngẫu nhiên và chế độ so sánh. 
Nên tách riêng model, algorithms, gui, utils sau khi nhóm thống nhất giao diện chung.
Thời gian được đo riêng cho BFS, không bao gồm thời gian hiệu ứng hiển thị.
