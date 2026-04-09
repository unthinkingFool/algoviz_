# AlgoViz — Interactive Algorithm & Data Structure Visualizer

![Java](https://img.shields.io/badge/Java-24-orange?logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue?logo=java)
![Maven](https://img.shields.io/badge/Build-Maven-red?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green)

AlgoViz is a desktop application built with **Java 24** and **JavaFX 21** that lets you step through classic algorithms and data structures visually and interactively. Whether you are a student learning DSA for the first time or a developer refreshing your memory, AlgoViz makes the logic behind each algorithm tangible.

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [Module Overview](#module-overview)
- [Authors](#authors)

---

## Features

### Algorithms
| Category | Algorithms |
|---|---|
| **Sorting** | Bubble Sort, Insertion Sort, Selection Sort, Merge Sort, Quick Sort |
| **Searching** | Linear Search, Binary Search |
| **Graph Traversal** | BFS, DFS, Dijkstra Shortest Path, Kruskal MST, Topological Sort, Cycle Detection, Bipartite Detection |
| **Pathfinding** | BFS Pathfinder, Dijkstra Pathfinder (grid-based) |

### Data Structures
| Data Structure | Operations |
|---|---|
| **Binary Search Tree** | Insert, Search, Delete, Successor/Predecessor |
| **Heap** | Insert, Extract Root, Peek, Heap Sort, Build Heap, Min/Max mode |
| **Stack** | Push, Pop, Peek, Search, Clear |
| **Queue** | Enqueue, Dequeue, Peek Front/Rear, Search, Reverse, Clear |
| **Linked List** | Insert Head/Tail/At, Delete Head/Tail/At, Search, Traversal |

### UI & Interaction
- **Play / Step / Pause / Reset** controls on every visualizer
- **Speed slider** to control animation pace
- **Custom array input** or random generation
- **Color-coded** nodes and edges showing visited, processing, and finalized states
- **Comparison and swap counters** on sorting and searching views
- **Grid-based pathfinder** with draggable walls and random wall generation

---

## Prerequisites

Make sure the following are installed on your machine before proceeding:

| Tool | Minimum Version | Download |
|---|---|---|
| **JDK** | 24 | https://adoptium.net or https://jdk.java.net |
| **Apache Maven** | 3.8+ | https://maven.apache.org/download.cgi |
| **Git** | Any | https://git-scm.com |

> **Note:** JavaFX is managed automatically by Maven — you do **not** need to install it separately.

---

## Project Structure

```
dsa_visualize_algoviz/
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java
│       │   └── com/dsa/visualizer/
│       │       ├── Main.java                        # Application entry point
│       │       ├── algorithms/
│       │       │   ├── graph/                       # Graph algorithm classes
│       │       │   │   ├── BFSTraversal.java
│       │       │   │   ├── DFSTraversal.java
│       │       │   │   ├── DijkstraShortestPath.java
│       │       │   │   ├── KruskalMST.java
│       │       │   │   ├── TopologicalSort.java
│       │       │   │   ├── CycleDetection.java
│       │       │   │   └── BipartiteDetection.java
│       │       │   ├── heap/
│       │       │   │   └── Heap.java
│       │       │   ├── linkedlist/
│       │       │   │   └── LinkedListAlgorithm.java
│       │       │   ├── pathfinding/
│       │       │   │   ├── BFS.java
│       │       │   │   └── Dijkstra.java
│       │       │   ├── searching/
│       │       │   │   ├── LinearSearch.java
│       │       │   │   └── BinarySearch.java
│       │       │   ├── sorting/
│       │       │   │   ├── BubbleSort.java
│       │       │   │   ├── InsertionSort.java
│       │       │   │   ├── SelectionSort.java
│       │       │   │   ├── MergeSort.java
│       │       │   │   └── QuickSort.java
│       │       │   ├── stack/
│       │       │   │   └── Stack.java
│       │       │   └── queue/
│       │       │       └── Queue.java
│       │       ├── controllers/                     # JavaFX FXML controllers
│       │       │   ├── MainController.java
│       │       │   ├── SortingController.java
│       │       │   ├── SearchingController.java
│       │       │   ├── GraphController.java
│       │       │   ├── PathfinderController.java
│       │       │   ├── TreeController.java
│       │       │   ├── HeapController.java
│       │       │   ├── StackQueueController.java
│       │       │   ├── LinkedlistController.java
│       │       │   └── AboutController.java
│       │       └── models/                          # Data model classes
│       │           ├── Cell.java
│       │           ├── Edge.java
│       │           ├── GraphNode.java
│       │           ├── TreeNode.java
│       │           ├── StackNode.java
│       │           └── QueueNode.java
│       └── resources/
│           └── fxml/                                # UI layout files
│               ├── main.fxml
│               ├── sorting.fxml
│               ├── searching.fxml
│               ├── graph.fxml
│               ├── pathfinder.fxml
│               ├── tree.fxml
│               ├── heap.fxml
│               ├── stackqueue.fxml
│               ├── linkedlist.fxml
│               └── about.fxml
├── .mvn/wrapper/
│   ├── maven-wrapper.jar
│   └── maven-wrapper.properties
├── pom.xml
└── README.md
```

---

## Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/unthinkingFool/algoviz_.git
cd algoviz_
```

### 2. Verify Java Version

```bash
java -version
```

You should see Java 24 (or later). If not, install the correct JDK and make sure `JAVA_HOME` is set:

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\path\to\jdk-24"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# macOS / Linux
export JAVA_HOME=/path/to/jdk-24
export PATH=$JAVA_HOME/bin:$PATH
```

### 3. Install Dependencies

Maven will download all dependencies (including JavaFX 21.0.6) automatically:

```bash
mvn clean install -DskipTests
```

---

## Running the Application

### Option A — Using the Maven Wrapper (recommended)

```bash
# macOS / Linux
./mvnw clean javafx:run

# Windows
mvnw.cmd clean javafx:run
```

### Option B — Using your system Maven

```bash
mvn clean javafx:run
```

### Option C — From IntelliJ IDEA

1. Open the project folder in IntelliJ IDEA.
2. Let IntelliJ import the Maven project (it will detect `pom.xml` automatically).
3. Wait for indexing and dependency download to complete.
4. Open `src/main/java/com/dsa/visualizer/Main.java`.
5. Click the **Run** button (▶) next to the `main` method.

> **Tip:** If IntelliJ shows JavaFX module errors, go to **Run → Edit Configurations → VM options** and add:
> ```
> --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml
> ```
> This is usually not needed when running through Maven.

---

## Module Overview

### Sorting Visualizer
Select any of the five sorting algorithms, generate a random array or enter a custom one, then use **Play All**, **Step**, or **Pause** to watch bars rearrange in real time. A comparison and swap counter updates live.

### Searching Visualizer
Choose Linear Search or Binary Search, load a custom array or generate one randomly, enter a target value, and watch the algorithm scan through elements with color highlights.

### Graph Visualizer
Add nodes and weighted edges on a canvas, then run BFS, DFS, Dijkstra, Kruskal MST, Topological Sort, Cycle Detection, or Bipartite Detection. Edges and nodes light up as they are processed.

### Pathfinder
Work on a 25×40 grid. Place a start node (green), end node (red), and draw walls by clicking or dragging. Generate random walls at 25% density, then run BFS or Dijkstra to find the shortest path. Supports step-by-step mode.

### Binary Search Tree
Insert integer values to build a BST visually. Search for values (highlighted traversal path), delete nodes (handles all three cases including inorder successor replacement), and watch the tree redraw after every operation.

### Heap Visualizer
Toggle between Min-Heap and Max-Heap mode. Insert values, extract the root, peek, run Heap Sort, or generate a random heap. Each sift-up and sift-down comparison is highlighted.

### Stack & Queue
Push/pop values onto a Stack or enqueue/dequeue from a Queue with animated node highlights. Supports peek, search, clear, and queue reverse operations.

### Linked List
A scrollable canvas showing the full list with HEAD/TAIL labels and index markers. Supports insert at head/tail/index, delete at head/tail/index, search, animated traversal, and random generation.

---

## Authors

| Name | Student ID |
|---|---|
| Swapnil | 2405016 |
| Nazmul Hasan Rafi | 2405017 |

> Built as part of a project :: CSE108: Object Oriented Programming Language Sessional.

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). 
