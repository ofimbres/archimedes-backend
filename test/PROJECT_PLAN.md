# 🎯 Archimedes Educational Platform - Simple Implementation Plan

## 📋 Current Status
- ✅ 513 HTML worksheets (mini-quizzes/)
- ✅ Dynamic worksheet server working
- ✅ Personalized problem generation (LCG algorithm)
- ❓ Need: Simple UI flow for students

## 🏗️ Architecture Decision: **KEEP IT SIMPLE**

### Storage: **LOCAL FIRST** (Not S3 initially)
- **Why**: Faster development, no AWS costs during development
- **Files**: Keep worksheets in `/mini-quizzes/` folder
- **Later**: Can easily move to S3 when ready for production scale

### Single Repo Structure:
```
archimedes-platform/
├── backend/                 # FastAPI server (current dynamic_worksheet_server.py)
│   ├── worksheets/         # Your 513 HTML files
│   ├── api/               # REST endpoints
│   └── main.py            # Server entry point
├── frontend/              # Simple web UI
│   ├── index.html         # Student portal
│   ├── teacher.html       # Teacher dashboard  
│   └── assets/           # CSS, JS
└── README.md
```

## 🔄 Student Flow (Simple Version)

### 1. **Student Portal** (frontend/index.html)
```
[Enter Student ID] → [Browse Topics] → [Select Worksheet] → [Work on Problems] → [Submit & See Score]
```

### 2. **Topic Browse Page**
- Show categories: Algebra (AL), Arithmetic (AR), Calculus (CM), etc.
- Grid of available worksheets with descriptions
- Click to start worksheet

### 3. **Worksheet Page**
- Use your current iframe approach
- Show worksheet in clean UI
- Auto-save progress
- Submit button → extract scores

### 4. **Results Page** 
- Show score breakdown
- Option to retry
- Link to next worksheet

## 🚀 Phase 1: MVP (This Week)

### Day 1-2: Backend API
```python
# Add these endpoints to dynamic_worksheet_server.py

@app.get("/api/topics")
def get_topics():
    # Return categorized list of worksheets
    
@app.get("/api/worksheets/{topic}")  
def get_worksheets_by_topic(topic: str):
    # Return AL01, AL02... for topic "AL"

@app.post("/api/submit/{worksheet_id}/{student_id}")
def submit_worksheet(worksheet_id: str, student_id: str, answers: dict):
    # Store student results (JSON file initially)
```

### Day 3-4: Simple Frontend
```html
<!-- frontend/student-portal.html -->
1. Landing page with student ID input
2. Topic selection grid  
3. Worksheet iframe viewer
4. Results display
```

### Day 5: Connect & Test
- Connect frontend to backend APIs
- Test full student flow
- Fix any issues

## 📁 Data Storage (Simple Approach)

### Student Progress (JSON Files - Local)
```
data/
├── students/
│   ├── 1000.json     # Student 1000's progress
│   ├── 1001.json     # Student 1001's progress
├── results/
│   ├── AL01/
│   │   ├── 1000_results.json
│   │   ├── 1001_results.json
```

### Worksheet Metadata (JSON)
```json
// data/worksheets.json
{
  "AL01": {
    "title": "Basic Algebraic Expressions",
    "category": "Algebra", 
    "difficulty": "Easy",
    "description": "Evaluate expressions with variables"
  }
}
```

## 🎨 UI Flow Details

### 1. Student Portal (`/`)
```
┌─────────────────────────────┐
│     📐 Archimedes Math      │
│                            │
│  Enter Student ID: [1000]  │
│         [Start] →          │
└─────────────────────────────┘
```

### 2. Topic Selection (`/student/{id}`)
```
┌─────────────────────────────┐
│  Welcome, Student 1000!     │
├─────────────────────────────┤
│  📚 Choose a Topic:         │
│                            │
│  [🔢 Algebra]  [➕ Arithmetic] │
│  [📊 Calculus] [📐 Geometry]  │
│  [🔬 Functions] [📈 Graphs]   │
└─────────────────────────────┘
```

### 3. Worksheet List (`/topic/AL`)
```
┌─────────────────────────────┐
│     🔢 Algebra Worksheets    │
├─────────────────────────────┤
│  AL01: Basic Expressions ⭐  │
│  AL02: Linear Equations ⭐⭐  │
│  AL03: Quadratics ⭐⭐⭐      │
│                            │
│  [← Back] [Start AL01 →]    │
└─────────────────────────────┘
```

### 4. Worksheet Viewer (`/worksheet/AL01/1000`)
```
┌─────────────────────────────┐
│  Student 1000 | AL01        │
├─────────────────────────────┤
│ [Your current iframe here]  │
│                            │
│ Progress: ████░░ 4/10       │
│                            │
│ [Save Progress] [Submit]    │
└─────────────────────────────┘
```

## 🔧 Implementation Commands

### 1. Restructure Current Project
```bash
# Create frontend folder
mkdir -p frontend/assets

# Move worksheets 
mkdir -p backend/worksheets
cp mini-quizzes/* backend/worksheets/

# Create data folder
mkdir -p data/students data/results
```

### 2. Enhance Backend
```bash
# Add new API endpoints to dynamic_worksheet_server.py
# Add JSON data handling
# Add CORS for frontend
```

### 3. Create Frontend
```bash
# Create student portal HTML/CSS/JS
# Simple, responsive design
# Connect to backend APIs
```

## 🚀 Next Steps TODAY

1. **Decide**: Keep local storage or move to S3? 
   - **Recommendation**: Start local, move to S3 later
   
2. **Create**: Basic student portal HTML page
   - Just student ID input + topic grid
   
3. **Enhance**: Your current server with topic APIs
   - Add `/api/topics` endpoint
   
4. **Test**: End-to-end student flow
   - Student enters ID → sees topics → clicks worksheet

## ❓ Quick Decisions Needed

1. **Storage**: Local files or S3? → **Recommend: Local first**
2. **UI Framework**: Plain HTML or React? → **Recommend: Plain HTML**
3. **Database**: JSON files or PostgreSQL? → **Recommend: JSON first**
4. **Authentication**: Simple ID or full auth? → **Recommend: Simple ID**

---

**🎯 Goal**: Working student portal by end of week where students can browse topics, select worksheets, and complete them with personalized problems.

**✅ Success Criteria**: Student 1000 and 1001 get different problems for the same worksheet through the web UI.