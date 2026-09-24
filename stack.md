# Complete Tech Stack: SwarSatya (स्वरसत्य) - SIH 2026

## Project Name: SwarSatya (स्वरसत्य)
**Meaning**: "Voice Truth" - Detecting AI voice cloning fraud in real-time

**Theme**: Blockchain & Cybersecurity  
**Team**: CypherX 
**Hackathon**: Smart India Hackathon 2026

---

## Core Philosophy
**Python-Centric Architecture**: All major components are Python-based, with minimal native code (Kotlin/Swift) only where absolutely necessary for OS-level hooks.

---

## 1. Core Languages & Runtimes

| Component | Technology | Version | Purpose | Why This Choice |
|-----------|------------|---------|---------|-----------------|
| **Primary Language** | Python | 3.11+ | Backend, ML pipelines, scripts, smart contract interaction | Fast development, rich ML ecosystem, web3.py support |
| **Mobile App Language** | Kotlin + Python (Chaquopy) | Kotlin 1.9, Python 3.10 | Android app with on-device Python ML inference | Run Python models directly on Android without rewriting in TensorFlow Lite |
| **Smart Contract Language** | Solidity | 0.8.19 | Blockchain voice registry & fraud logging | Industry standard, EVM-compatible, extensive tooling |
| **Scripting/Automation** | Bash | 5.x | Deployment scripts, CI/CD pipelines | Standard for DevOps automation |

---

## 2. Machine Learning & AI Stack

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Deep Learning Framework** | PyTorch | 2.1+ | Model training, feature extraction, dynamic graphs | `pip install torch torchvision torchaudio` |
| **Audio Processing** | Torchaudio | 2.1+ | Audio I/O, spectrograms, mel-filters, resampling | `pip install torchaudio` |
| **Signal Processing** | Librosa | 0.10+ | LFCC, MFCC, spectral features, audio visualization | `pip install librosa` |
| **Scientific Computing** | NumPy | 1.24+ | Array operations, numerical computations | `pip install numpy` |
| **Model Architecture** | RawNet3 | Custom | End-to-end spoofing detection on raw audio | Clone from GitHub, load pre-trained weights |
| **Model Architecture** | AASIST | Custom | Graph-based spoofing detection, robust to telephony audio | Clone from GitHub |
| **Voice Activity Detection** | Silero VAD | 0.4+ | Lightweight speech detection (<1% CPU) | `pip install silero-vad` or ONNX model |
| **Data Augmentation** | Audiomentations | 0.17+ | Simulate telephony codecs, noise, compression | `pip install audiomentations` |
| **Model Export** | ONNX | 1.15+ | Convert PyTorch models to cross-platform format | `pip install onnx onnxruntime` |
| **Mobile Inference** | ONNX Runtime Mobile | 1.16+ | Run quantized models on Android NPU | `pip install onnxruntime-gpu` (desktop), Android AAR for mobile |
| **Model Quantization** | PyTorch Quantization | Built-in (2.1+) | INT8 quantization for mobile deployment | `torch.quantization` module |
| **Pre-trained Models** | HuggingFace Transformers | 4.35+ | Access pre-trained speaker verification models | `pip install transformers` |

---

## 3. Backend & API Stack

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Web Framework** | FastAPI | 0.104+ | Async REST API, WebSocket endpoints | `pip install fastapi uvicorn` |
| **ASGI Server** | Uvicorn | 0.24+ | High-performance async server for FastAPI | `pip install uvicorn` |
| **Data Validation** | Pydantic | 2.5+ | Request/response schema validation | `pip install pydantic` |
| **CORS Handling** | FastAPI CORS | Built-in | Enable cross-origin requests from mobile app | `fastapi.middleware.cors` |
| **WebSocket Support** | FastAPI WebSocket | Built-in | Real-time bidirectional communication | `pip install websockets` |
| **Background Tasks** | FastAPI BackgroundTasks | Built-in | Async fraud logging, blockchain writes | `fastapi.BackgroundTasks` |
| **Authentication** | Python-JOSE | 3.0+ | JWT token generation/validation (if needed) | `pip install python-jose[cryptography]` |
| **Password Hashing** | Passlib | 1.7+ | Secure password hashing (for dashboard admin) | `pip install passlib[bcrypt]` |

---

## 4. Blockchain & Web3 Stack

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Blockchain Network** | Polygon Mumbai Testnet | N/A | Smart contract deployment (free testnet MATIC) | No installation, use public RPC |
| **Web3 Library** | Web3.py | 6.11+ | Interact with Ethereum-compatible blockchain | `pip install web3` |
| **Smart Contract ABI** | JSON ABI | N/A | Contract interface definition | Export from Remix IDE |
| **Contract Deployment** | Remix IDE | N/A | Write, compile, deploy Solidity contracts | Browser-based (remix.ethereum.org) |
| **Wallet Management** | Eth-Account | 0.6+ | Manage private keys, sign transactions | `pip install eth-account` |
| **Transaction Signing** | Eth-Keys | 0.3+ | Low-level cryptographic operations | `pip install eth-keys` |
| **Block Explorer** | Polygonscan API | N/A | Verify transactions, view logs | Free API key from polygonscan.com |
| **Testnet MATIC** | Mumbai Faucet | N/A | Get free test tokens for deployment | https://faucet.polygon.technology/ |

---

## 5. Database & Storage

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Primary Database** | SQLite | 3.40+ | Store user sessions, risk scores, metadata | Built-in Python (`sqlite3` module) |
| **ORM** | SQLAlchemy | 2.0+ | Database abstraction, query building | `pip install sqlalchemy` |
| **In-Memory Cache** | Redis | 7.2+ | Store active call sessions, rolling risk scores | `pip install redis` (local or cloud Redis) |
| **Alternative (Cloud)** | PostgreSQL | 15+ | If scaling beyond SQLite (optional) | `pip install psycopg2-binary` |
| **Time-Series Data** | CSV Files | N/A | Log fraud incidents for dashboard (simple for hackathon) | Built-in Python (`csv` module) |

---

## 6. Mobile App (Android) Stack

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **App Framework** | Android Native (Kotlin) | Kotlin 1.9 | UI, permissions, system integration | Android Studio |
| **Python on Android** | Chaquopy | 15.0+ | Run Python code directly in Android app | Add to `build.gradle` |
| **Audio Capture** | Android AudioRecord | API 21+ | Low-latency audio stream from microphone | Android SDK |
| **Alternative Audio** | Oboe (C++) | 1.8+ | Ultra-low latency audio (optional) | Android NDK |
| **ML Runtime** | ONNX Runtime Mobile | 1.16+ | Hardware-accelerated inference (NPU/GPU) | Add `.aar` to Gradle |
| **UI Framework** | Jetpack Compose | 1.5+ | Modern declarative UI | AndroidX library |
| **Overlay Permission** | SYSTEM_ALERT_WINDOW | API 23+ | Show fraud alert over other apps | Android manifest permission |
| **Background Service** | Android Foreground Service | API 26+ | Keep detection running during call | Android manifest |
| **App Package Name** | `com.swarsatya.app` | N/A | Unique Android application ID | Configure in `build.gradle` |

---

## 7. Dashboard & Visualization

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Dashboard Framework** | Streamlit | 1.28+ | Rapid dashboard development (recommended for hackathon) | `pip install streamlit` |
| **Alternative** | React + Vite | 5.0+ | Custom dashboard (more control, more work) | `npm create vite@latest` |
| **Charts** | Plotly | 5.18+ | Interactive graphs (risk scores over time) | `pip install plotly` |
| **Data Tables** | Pandas | 2.1+ | Data manipulation, CSV export | `pip install pandas` |
| **UI Components** | Streamlit Components | Built-in | Buttons, sliders, file uploaders | Built-in Streamlit |
| **Real-Time Updates** | Streamlit Auto-Rerun | Built-in | Auto-refresh dashboard every 5 seconds | `st.autorun()` |
| **Dashboard Title** | "SwarSatya Fraud Dashboard" | N/A | Branding for judging | Configure in `st.set_page_config()` |

---

## 8. DevOps & Deployment

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Version Control** | Git | 2.40+ | Code versioning, collaboration | Pre-installed on most systems |
| **Code Hosting** | GitHub | N/A | Private repo during hackathon | Free account |
| **GitHub Repo Name** | `swarsatya-sih2026` | N/A | Repository identifier | Create on GitHub |
| **Backend Hosting** | Railway | N/A | Deploy FastAPI backend (free tier) | CLI: `npm i -g railway` |
| **Backend Service Name** | `swarsatya-backend` | N/A | Railway service identifier | Configure in Railway dashboard |
| **Alternative Hosting** | Render | N/A | Another free backend hosting option | Web dashboard |
| **Dashboard Hosting** | Streamlit Cloud | N/A | Free hosting for Streamlit apps | GitHub integration |
| **Dashboard App Name** | `swarsatya-dashboard` | N/A | Streamlit Cloud app identifier | Configure in Streamlit Cloud |
| **Containerization** | Docker | 24.0+ | Package backend for consistent deployment | Docker Desktop |
| **Docker Image Name** | `swarsatya-backend:latest` | N/A | Container image tag | Configure in Dockerfile |
| **CI/CD** | GitHub Actions | N/A | Auto-deploy on push to main branch | `.github/workflows/deploy.yml` |
| **Environment Management** | Python venv | Built-in | Isolate project dependencies | `python -m venv venv` |
| **Dependency Management** | Pip | 23.3+ | Install Python packages | Built-in Python |
| **Alternative** | Poetry | 1.7+ | Better dependency management (optional) | `pip install poetry` |

---

## 9. Development Tools

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Code Editor** | VS Code | 1.84+ | Primary IDE with Python extensions | Download from website |
| **Python Extension** | Pylance | Built-in | IntelliSense, linting, type checking | VS Code extension |
| **Jupyter Notebooks** | Jupyter Lab | 4.0+ | Experiment with ML models, data analysis | `pip install jupyterlab` |
| **API Testing** | Postman | 10.0+ | Test FastAPI endpoints | Download from website |
| **Alternative** | Insomnia | 2023.5+ | Lightweight API testing tool | Download from website |
| **Terminal** | Windows Terminal | 1.18+ | Modern terminal for Windows | Microsoft Store |
| **Alternative** | iTerm2 | 3.4+ | Terminal for macOS | Download from website |
| **Blockchain IDE** | Remix IDE | N/A | Write and deploy Solidity contracts | Browser-based |
| **Smart Contract Testing** | Hardhat | 2.19+ | Local blockchain, contract testing (optional) | `npm install --save-dev hardhat` |

---

## 10. Testing & Quality

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Unit Testing** | Pytest | 7.4+ | Test backend logic, API endpoints | `pip install pytest` |
| **API Testing** | Pytest + HTTPX | 0.25+ | Test FastAPI endpoints programmatically | `pip install httpx` |
| **Code Coverage** | Coverage.py | 7.3+ | Measure test coverage | `pip install coverage` |
| **Linting** | Flake8 | 6.1+ | Code style checking | `pip install flake8` |
| **Type Checking** | Mypy | 1.7+ | Static type checking | `pip install mypy` |
| **Code Formatting** | Black | 23.11+ | Auto-format Python code | `pip install black` |
| **Import Sorting** | Isort | 5.12+ | Sort imports automatically | `pip install isort` |

---

## 11. Security & Privacy

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Encryption** | Cryptography | 41.0+ | Encrypt sensitive data at rest | `pip install cryptography` |
| **Secure Memory** | Python `ctypes` | Built-in | Zero out audio buffers (memset_s) | Built-in Python |
| **Hashing** | Hashlib | Built-in | SHA-256 for voiceprint hashing | Built-in Python |
| **Random Generation** | Secrets | Built-in | Cryptographically secure random numbers | Built-in Python |
| **SSL/TLS** | SSL | Built-in | Secure WebSocket connections | Built-in Python |

---

## 12. Monitoring & Logging

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Logging** | Python Logging | Built-in | Structured logging for backend | Built-in Python |
| **Log Aggregation** | Loguru | 0.7+ | Better logging experience | `pip install loguru` |
| **Metrics** | Prometheus Client | 0.19+ | Expose metrics for monitoring (optional) | `pip install prometheus-client` |
| **Visualization** | Grafana | 10.0+ | Dashboard for metrics (optional, advanced) | Self-hosted or cloud |

---

## 13. Audio & Telephony Integration

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Audio Playback** | PyDub | 0.25+ | Play pre-recorded audio for demo | `pip install pydub` |
| **Audio Recording** | SoundDevice | 0.4+ | Cross-platform audio I/O (desktop testing) | `pip install sounddevice` |
| **VoIP Integration** | aiortc | 0.9+ | WebRTC for VoIP call interception (advanced) | `pip install aiortc` |
| **SIP Integration** | Python PJSUA2 | 0.1+ | SIP trunking for enterprise calls (advanced) | `pip install python-pjsua2` |
| **Audio File I/O** | SciPy | 1.11+ | Read/write WAV files, signal processing | `pip install scipy` |

---

## 14. Documentation & Presentation

| Component | Technology | Version | Purpose | Installation |
|-----------|------------|---------|---------|--------------|
| **Documentation** | Markdown | N/A | README, docs, demo script | Any text editor |
| **Project Logo** | Canva / Figma | N/A | Create SwarSatya logo (optional) | Browser-based |
| **Diagram Tool** | Draw.io | N/A | Create architecture diagrams | Browser-based |
| **Alternative** | Excalidraw | N/A | Hand-drawn style diagrams | Browser-based |
| **Presentation** | Google Slides | N/A | Pitch deck for judging | Free account |
| **Alternative** | Canva | N/A | Professional slide design | Free account |
| **PDF Export** | Pandoc | 3.1+ | Convert Markdown to PDF | Download from website |

---

## Complete requirements.txt (Copy-Paste Ready)

```txt
# SwarSatya - SIH 2026
# Complete Python dependencies

# Core ML & Audio
torch==2.1.0
torchaudio==2.1.0
librosa==0.10.1
numpy==1.24.3
onnx==1.15.0
onnxruntime==1.16.0
silero-vad==0.4.0
audiomentations==0.17.0
transformers==4.35.0

# Backend (FastAPI)
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
websockets==12.0
python-jose==3.0.1
passlib==1.7.4

# Blockchain (Web3)
web3==6.11.0
eth-account==0.6.0
eth-keys==0.3.0

# Database
sqlalchemy==2.0.23
redis==5.0.1
pandas==2.1.3

# Dashboard (Streamlit)
streamlit==1.28.0
plotly==5.18.0

# DevOps
docker==7.0.0
requests==2.31.0
python-dotenv==1.0.0

# Testing
pytest==7.4.3
httpx==0.25.0
coverage==7.3.2

# Code Quality
flake8==6.1.0
mypy==1.7.0
black==23.11.0
isort==5.12.0

# Security
cryptography==41.0.7

# Logging
loguru==0.7.2

# Audio
sounddevice==0.4.6
scipy==1.11.4
pydub==0.25.1

# Utilities
tqdm==4.66.1
matplotlib==3.8.2
seaborn==0.13.0
```

---

## Installation Commands (Quick Setup)

```bash
# 1. Create project directory
mkdir swarsatya-sih2026
cd swarsatya-sih2026

# 2. Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or
venv\Scripts\activate  # Windows

# 3. Install all dependencies
pip install -r requirements.txt

# 4. Install PyTorch with CUDA (for GPU acceleration)
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118

# 5. Verify installation
python -c "import torch; print(f'✅ PyTorch {torch.__version__}')"
python -c "import fastapi; print(f'✅ FastAPI {fastapi.__version__}')"
python -c "import web3; print(f'✅ Web3.py {web3.__version__}')"
python -c "import streamlit; print(f'✅ Streamlit {streamlit.__version__}')"
```

---

## Hardware Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| **Development Machine** | 8GB RAM, i5/Ryzen 5 | 16GB RAM, i7/Ryzen 7, RTX 3060+ |
| **Android Phone** | 4GB RAM, Snapdragon 665 | 6GB RAM, Snapdragon 778G+ (NPU support) |
| **Backend Hosting** | Railway Free (512MB RAM) | Railway Hobby (₹400/mo, 2GB RAM) |
| **Blockchain** | Polygon Mumbai Testnet (free) | Polygon Mumbai Testnet (free) |

---

## Cost Breakdown (Hackathon Budget)

| Item | Cost | Notes |
|------|------|-------|
| **Development** | ₹0 | All tools are open-source |
| **Backend Hosting** | ₹0 | Railway/Render free tier |
| **Dashboard Hosting** | ₹0 | Streamlit Cloud free |
| **Blockchain** | ₹0 | Polygon Mumbai testnet (free MATIC) |
| **Domain Name** | ₹0 | Use Railway/Streamlit subdomains |
| **Android Phone** | ₹0 | Use existing phone or emulator |
| **Total** | **₹0** | Completely free for hackathon |

---

## Tech Stack Summary (One-Liner)

> "**SwarSatya**: Python 3.11 + FastAPI backend, PyTorch/ONNX for on-device AI, Polygon blockchain for immutable logs, Streamlit dashboard, Kotlin/Chaquopy for Android app with native Python ML inference."

---

## GitHub Repo Structure (Updated)

```
swarsatya-sih2026/
├── README.md                    # Project overview, setup instructions
├── requirements.txt             # All Python dependencies
├── .gitignore                  # Git ignore rules
├── LICENSE                      # MIT License (recommended)
│
├── android-app/                 # SwarSatya Android Application
│   ├── app/
│   │   ├── src/main/java/com/swarsatya/
│   │   │   ├── MainActivity.kt
│   │   │   ├── VoiceDetectionService.kt
│   │   │   └── AlertOverlay.kt
│   │   ├── src/main/res/       # App icons, layouts
│   │   └── build.gradle        # Chaquopy, ONNX Runtime deps
│   └── models/
│       └── rawnet3_quantized.onnx
│
├── backend/                     # FastAPI Backend
│   ├── main.py                 # FastAPI app entry point
│   ├── models.py               # Pydantic models
│   ├── web3_integration.py     # Blockchain interactions
│   ├── contracts/
│   │   └── SwarSatFraud.sol    # Smart contract
│   ├── requirements.txt        # Backend-specific deps
│   └── Dockerfile              # Container config
│
├── dashboard/                   # Streamlit Dashboard
│   ├── app.py                  # Dashboard entry point
│   ├── requirements.txt        # Dashboard-specific deps
│   └── assets/
│       └── swarsatya_logo.png  # Optional logo
│
├── ml/                          # Machine Learning
│   ├── train.py                # Model training script
│   ├── inference.py            # Inference pipeline
│   └── models/
│       └── rawnet3_pretrained.pth
│
├── docs/                        # Documentation
│   ├── architecture.pdf        # System architecture diagram
│   ├── demo_script.md          # Live demo script
│   ├── presentation.pdf        # SIH pitch deck
│   └── api_docs.md             # API documentation
│
└── tests/                       # Test Suite
    ├── test_backend.py         # Backend unit tests
    ├── test_blockchain.py      # Smart contract tests
    └── test_ml.py              # ML inference tests
```

---

## Branding Guidelines (Optional but Recommended)

### **Logo Concept**
- **Visual**: Sound wave + blockchain block + checkmark
- **Colors**: 
  - Primary: Deep Blue (#1E3A8A) - Trust, Security
  - Secondary: Orange (#F97316) - Energy, Innovation
  - Accent: Green (#10B981) - Safety, Verification

### **App Icon**
- **Style**: Material Design 3 (Android)
- **Shape**: Rounded square
- **Symbol**: Abstract sound wave with shield

### **Dashboard Theme**
```python
# Streamlit config (dashboard/app.py)
st.set_page_config(
    page_title="SwarSatya - Fraud Dashboard",
    page_icon="🎯",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS for branding
st.markdown("""
<style>
    .stApp {
        background-color: #f8f9fa;
    }
    .header {
        color: #1E3A8A;
        font-size: 2.5rem;
        font-weight: bold;
    }
</style>
""", unsafe_allow_html=True)
```

---

## Next Steps

1. **Day 1**: Set up virtual environment, install all dependencies
2. **Day 2**: Clone pre-trained RawNet3 model, test inference on sample audio
3. **Day 3**: Build FastAPI backend with 2 endpoints (`/infer`, `/log-fraud`)
4. **Day 4**: Deploy smart contract to Mumbai testnet via Remix
5. **Day 5**: Build Android app skeleton with audio capture
6. **Day 6**: Integrate ONNX model into Android app
7. **Day 7**: Build Streamlit dashboard with SwarSatya branding
8. **Day 8**: End-to-end testing, demo rehearsal

---

## Project Slogan Options

Choose one for your presentation:

1. **"स्वरसत्य: सत्य की आवाज़"** (SwarSatya: The Voice of Truth)
2. **"Detecting Deepfakes, Defending Trust"**
3. **"Your Voice, Verified"**
4. **"Real-Time Voice Cloning Detection for India"**
5. **"AI vs AI: Fighting Voice Cloning with Voice Detection"**

---

This tech stack is **100% Python-centric**, hackathon-ready, and costs **₹0** to run. All tools are open-source with active communities and extensive documentation.

**Project Name**: SwarSatya (स्वरसत्य)  
**Theme**: Blockchain & Cybersecurity  
**Hackathon**: Smart India Hackathon 2026  
**Status**: Ready to Build 🚀
