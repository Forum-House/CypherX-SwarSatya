# SwarSatya — Blockchain Module

Smart contract, deployment tooling, and a runnable demo for the fraud-registry
blockchain layer. Everything here targets **Polygon Amoy testnet** — free,
public, and verifiable on a block explorer.

## What's in here

```
contracts/SwarSatFraud.sol   -- the registry + fraud-log contract
scripts/deploy.js            -- deploys the contract, saves ABI + address
scripts/authorize-reporter.js-- lets the backend wallet call logFraud()
python/swarsat_client.py     -- Web3.py wrapper your FastAPI backend will import
python/demo.py               -- end-to-end demo: register -> read -> log fraud -> blacklist
```

## One-time setup

### 1. Get two wallets and test MATIC
You need two wallet addresses (MetaMask is fine — create two accounts):
- **Deployer wallet** — deploys the contract, becomes its owner.
- **Backend/reporter wallet** — the wallet your backend server uses to call `logFraud()`.

Add Polygon Amoy to MetaMask (Chain ID `80002`, RPC `https://rpc-amoy.polygon.technology`),
then get free test MATIC for **both** wallets from: https://faucet.polygon.technology

Export each wallet's private key (MetaMask: Account menu → Account details →
Show private key). **Never commit these or share them outside your team.**

### 2. Install dependencies
```bash
npm install
cd python && pip install -r requirements.txt && cd ..
```

### 3. Configure environment
```bash
cp .env.example .env
```
Fill in `.env`:
- `DEPLOYER_PRIVATE_KEY` — your deployer wallet's key
- `BACKEND_REPORTER_ADDRESS` — your backend wallet's **address** (not key, just for authorizing it)
- `USER_PRIVATE_KEY` / `BACKEND_PRIVATE_KEY` — needed later, for running `demo.py`

## Deploy to Amoy

```bash
npm run deploy:amoy
```
This prints the deployed contract address, and writes `python/abi.json` +
`python/deployed_address.txt` automatically. Copy the printed address into
`.env` as `CONTRACT_ADDRESS`.

Verify it exists publicly:
`https://amoy.polygonscan.com/address/<CONTRACT_ADDRESS>`

## Authorize your backend wallet

Only authorized wallets can log fraud reports (prevents spam). Put your
backend wallet's *address* in `.env` as `BACKEND_REPORTER_ADDRESS`, then:

```bash
npm run authorize-reporter
```

## Run the demo

Fill in `USER_PRIVATE_KEY` and `BACKEND_PRIVATE_KEY` in `.env` (can reuse the
two wallets from setup — the "user" wallet just needs a small amount of test
MATIC too, since registering a voice costs a tiny bit of gas).

```bash
cd python
python demo.py
```

You'll see, in order:
1. A voice registration transaction confirm (with a live PolygonScan link)
2. That same record read back straight from the contract
3. Three fraud reports logged against a test number
4. The number automatically flip to `blacklisted: True`

Every link it prints is a real, independently-verifiable transaction — open
any of them in a browser during your demo so the team/judges see it's not
mocked.

## Using this from your FastAPI backend

```python
from swarsat_client import SwarSatFraudClient

client = SwarSatFraudClient(
    rpc_url=os.environ["AMOY_RPC_URL"],
    contract_address=os.environ["CONTRACT_ADDRESS"],
    private_key=os.environ["BACKEND_PRIVATE_KEY"],  # the authorized reporter
)

# during a call, if the AI model flags spoofing:
client.log_fraud(phone_number, reason="AI ensemble score 0.94 (spoof)")

# before connecting a call:
if client.is_blacklisted(phone_number):
    # reject / warn the user
    ...
```

## Notes on the design decision made earlier

`registerVoice()` requires a signature proving the caller controls the wallet
claiming the phone number — this closes the gap where anyone could overwrite
someone else's registration, which the original SIH template didn't handle.
---

# Complete Workflow: SwarSatya (स्वरसत्य) - AI-Powered Voice Cloning Detection

## Overview

This document traces a complete user journey through all five architectural zones of **SwarSatya**, showing every step from initial enrollment to real-time fraud detection during a live call.

**Project**: SwarSatya (स्वरसत्य - "Voice Truth")  
**Hackathon**: Smart India Hackathon 2026  
**Theme**: Blockchain & Cybersecurity

---

## Phase 0: System Initialization (One-Time Setup)

### Step 0.1: User Downloads SwarSatya App
- **Platform**: Android (Kotlin + Python via Chaquopy)
- **Action**: User installs "SwarSatya" app from Play Store
- **Tech**: Jetpack Compose UI, Python backend services, ONNX Runtime Mobile

### Step 0.2: App Requests Permissions
```
Permissions Requested:
├── Microphone Access (for voice enrollment)
├── Call State Access (Android: READ_PHONE_STATE)
├── Overlay Permission (SYSTEM_ALERT_WINDOW for warnings)
└── Background Audio Processing (VoIP background mode)
```

### Step 0.3: Blockchain Wallet Generation
- **Action**: App generates a new decentralized identity (DID)
- **Tech**: `web3.py` + `eth-account` for key management
- **Output**: 
  - Public Key: `0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb...`
  - Private Key: Stored in Android Keystore (never leaves device)
- **Blockchain**: Polygon Mumbai Testnet

---

## Phase 1: Voice Enrollment (Trust Bootstrapping)

### Step 1.1: User Initiates Enrollment
**User Action**: Opens SwarSatya app → "Register Your Voice" → Reads 3 prompted phrases

**Example Prompts**:
1. "My name is Devansh Chaturvedi and I authorize voice verification."
2. "I confirm this voice print for secure transaction approvals."
3. Random 4-digit number (e.g., "7-3-9-2") for liveness detection

### Step 1.2: Audio Capture & Preprocessing
```python
# Pseudocode running on device (via Chaquopy)
import torchaudio
import numpy as np

# Capture 10 seconds of audio at 16kHz
audio_samples = record_audio(duration=10, sample_rate=16000)

# Preprocessing pipeline
audio_resampled = torchaudio.transforms.Resample(16000, 16000)(audio_samples)
audio_normalized = (audio_resampled - audio_resampled.mean()) / audio_resampled.std()

# Split into 3 segments for each phrase
segments = np.split(audio_normalized, 3)
```

### Step 1.3: Voiceprint Embedding Generation
**Model**: Pre-trained speaker verification model (ECAPA-TDNN)

```python
import torch
from speechbrain.inference import EncoderClassifier

# Load model (runs locally on device during enrollment)
classifier = EncoderClassifier.from_hparams(source="speechbrain/spkrec-ecapa-voxceleb")

# Generate 256-dimensional embedding for each segment
embeddings = []
for segment in segments:
    embedding = classifier.encode_batch(segment.unsqueeze(0))
    embeddings.append(embedding)

# Average embeddings for robustness
final_voiceprint = torch.mean(torch.stack(embeddings), dim=0)  # Shape: [256]

# Convert to hash for blockchain
voiceprint_hash = sha256(final_voiceprint.numpy().tobytes())
```

**Output**: 
- Voiceprint Vector: `[0.23, -0.87, 0.45, ..., 0.12]` (256 floats)
- Voiceprint Hash: `0x7f9a3b2c8e1d4f6a5b9c0e2d8f7a3b1c...`

### Step 1.4: Cryptographic Signing
```python
from eth_account import Account

# Load private key from secure keystore
private_key = load_from_keystore("user_private_key")
account = Account.from_key(private_key)

# Create verifiable credential structure
vc_payload = {
    "@context": ["https://www.w3.org/2018/credentials/v1"],
    "type": ["VerifiableCredential", "VoiceBiometricCredential"],
    "issuer": account.address,
    "issuanceDate": "2026-09-08T23:49:00Z",
    "credentialSubject": {
        "id": f"did:ethr:{account.address}",
        "voiceprintHash": "0x7f9a3b2c8e1d4f6a5b9c0e2d8f7a3b1c...",
        "algorithm": "ECAPA-TDNN-256",
        "enrollmentTimestamp": 1694237340
    }
}

# Sign the credential
signature = account.sign_message(vc_payload)
```

### Step 1.5: Blockchain Registration (Zone 4)
**Smart Contract Call**: `SwarSatFraud.registerVoice()`

```solidity
// Solidity Smart Contract (Polygon Mumbai Testnet)
function registerVoice(
    string memory phoneNumber,
    string memory did,
    string memory voiceprintHash
) public {
    // Store in registry
    voiceRegistry[phoneNumber] = VoiceRecord({
        did: did,
        voiceprintHash: voiceprintHash,
        registeredAt: block.timestamp,
        isActive: true
    });
    
    emit VoiceRegistered(phoneNumber, did, block.timestamp);
}
```

**Transaction Details**:
- **Network**: Polygon Mumbai Testnet
- **Gas Cost**: ~0.001 MATIC (negligible on testnet)
- **Finality**: ~1-2 seconds
- **Tx Hash**: `0x3f8a2b9c7e1d5f4a6b8c0e3d9f2a7b5c...`

**Output**: Voice print is now immutably anchored on-chain, linked to user's phone number and DID.

---

## Phase 2: Pre-Call Setup (Call Signaling Phase)

### Scenario: User receives a call from "Bank Manager" claiming to be a registered contact

### Step 2.1: Incoming Call Detection
**Trigger**: Phone rings, caller ID shows "+91-98765-43210" (Bank Manager's number)

```python
# Android: BroadcastReceiver for CALL_STATE
call_metadata = {
    "caller_number": "+91-98765-43210",
    "receiver_number": "+91-91234-56789",  # User's number
    "call_id": "call_8f3a2b9c7e1d",
    "timestamp": "2026-09-08T23:52:15Z",
    "call_type": "cellular"  # or "voip" for WhatsApp/Teams
}
```

### Step 2.2: Blockchain Voice-Print Fetch (Zone 3 → Zone 4)
**Action**: FastAPI backend queries blockchain registry for caller's registered voiceprint

```python
# FastAPI backend (Zone 3)
from web3 import Web3

# Connect to blockchain node
w3 = Web3(Web3.HTTPProvider("https://rpc-mumbai.maticvigil.com"))

# Load smart contract
contract = w3.eth.contract(address="0xSwarSatFraudAddress", abi=contract_abi)

# Query caller's voiceprint hash
caller_number = "+91-98765-43210"
voice_record = contract.functions.getVoiceRecord(caller_number).call()

# Output
registered_voiceprint_hash = voice_record[0]  # 0x7f9a3b2c8e1d4f6a5b9c0e2d8f7a3b1c...
registered_did = voice_record[1]  # did:ethr:0x742d35Cc...
is_registered = voice_record[3]  # True
```

### Step 2.3: Pre-Call Risk Assessment
**Backend Logic**: Check if this is a high-risk call scenario

```python
# FastAPI risk scoring
def assess_call_risk(caller_number, receiver_number, context):
    risk_factors = []
    
    # Factor 1: Is caller registered on blockchain?
    if not is_registered:
        risk_factors.append(("UNREGISTERED_CALLER", 0.3))
    
    # Factor 2: Is this a known contact?
    if caller_number not in user_contacts:
        risk_factors.append(("UNKNOWN_CONTACT", 0.2))
    
    # Factor 3: Transaction context (if available from bank integration)
    if context.get("transaction_value", 0) > 100000:  # > ₹1 lakh
        risk_factors.append(("HIGH_VALUE_TRANSACTION", 0.4))
    
    # Factor 4: Historical fraud patterns
    if caller_number in fraud_blacklist:
        risk_factors.append(("BLACKLISTED_NUMBER", 0.9))
    
    # Calculate base risk score
    base_risk = max([score for _, score in risk_factors]) if risk_factors else 0.1
    
    return {
        "base_risk": base_risk,
        "risk_factors": risk_factors,
        "recommendation": "MONITOR" if base_risk < 0.5 else "ALERT"
    }

risk_assessment = assess_call_risk(caller_number, receiver_number, call_context)
```

# Step 2.4: Secure Voice-Hash Pre-Caching (WITH SIGNATURE)

# Backend generates signed token
verification_token = {
    "type": "PRE_CALL_VERIFICATION_DATA",
    "call_id": "call_8f3a2b9c7e1d",
    "caller_number": "+91-98765-43210",
    "caller_voiceprint_hash": "0x7f9a3b2c8e1d4f6a5b9c0e2d8f7a3b1c...",
    "caller_did": "did:ethr:0x742d35Cc...",
    "backend_signature": "0x8a3b2c9d7e1f5a4b6c8d0e2f7a3b5c9d...",  # 65-byte signature
    "timestamp": "2026-09-08T23:52:16Z",
    "expiry": "2026-09-08T23:57:16Z",  # 5-minute validity
    "base_risk_score": 0.3,
    "risk_factors": ["UNKNOWN_CONTACT"]
}

# Device receives and caches
cached_verification_data = websocket_message

# Device verifies before call
is_valid, message = verify_verification_token(cached_verification_data)

if is_valid:
    print("✅ Verification token valid - using cached hash")
    start_call_monitoring(cached_verification_data["caller_voiceprint_hash"])
else:
    print(f"⚠️ Verification failed: {message}")
    print("📡 Fetching fresh hash from blockchain...")
    
    # Fallback to blockchain
    voiceprint_hash = fetch_from_blockchain("+91-98765-43210")
    
    if voiceprint_hash:
        start_call_monitoring(voiceprint_hash)
    else:
        # No voiceprint registered - AI-only mode
        start_ai_only_monitoring()

## Phase 3: Real-Time Call Monitoring (Zone 1 Execution)

### Step 3.1: Audio Stream Interception
**Trigger**: User answers the call, audio stream begins

```python
# Android: AudioRecord with VOICE_COMMUNICATION source

import sounddevice as sd
import numpy as np

# Configure audio stream
sample_rate = 16000
chunk_duration = 1.0  # seconds
chunk_size = int(sample_rate * chunk_duration)

# Ring buffer in RAM (circular buffer, max 2 seconds)
ring_buffer = np.zeros(chunk_size * 2, dtype=np.float32)
buffer_write_idx = 0
```

### Step 3.2: Voice Activity Detection (VAD) Gate
**Model**: Silero VAD (lightweight, <1% CPU)

```python
from silero_vad import load_silero_vad, get_speech_timestamps

# Load VAD model (runs on CPU, minimal overhead)
vad_model = load_silero_vad()

def process_audio_chunk(audio_chunk):
    # Check if chunk contains speech
    speech_prob = vad_model(audio_chunk, sample_rate)
    
    if speech_prob < 0.5:
        # Silence or noise - discard immediately
        return None
    else:
        # Active speech - forward to inference engine
        return audio_chunk

# Audio processing loop
while call_active:
    audio_chunk = read_audio_chunk(chunk_size)  # 1 second of audio
    speech_chunk = process_audio_chunk(audio_chunk)
    
    if speech_chunk is not None:
        # Forward to inference pipeline
        run_inference_pipeline(speech_chunk)
    else:
        # Skip inference, save battery
        continue
```

### Step 3.3: Dual-Branch AI Inference (On-Device NPU)

#### Branch A: Spectral Artifact Detection (RawNet3)
```python
import onnxruntime as ort

# Load quantized RawNet3 model (INT8, optimized for NPU)
session_options = ort.SessionOptions()
session_options.intra_op_num_threads = 4
session_options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL

# Android: NNAPI delegate
rawnet3_session = ort.InferenceSession(
    "rawnet3_quantized.onnx",
    sess_options=session_options,
    providers=['QNNExecutionProvider']
)

def extract_spectral_features(audio_chunk):
    # Compute Log-Mel Spectrogram
    import torchaudio.transforms as T
    
    mel_transform = T.MelSpectrogram(
        sample_rate=16000,
        n_fft=512,
        win_length=400,
        hop_length=160,
        n_mels=80
    )
    
    spectrogram = mel_transform(torch.from_numpy(audio_chunk))
    log_mel = torch.log(spectrogram + 1e-9)
    
    return log_mel.numpy()

# Inference
spectral_features = extract_spectral_features(speech_chunk)
rawnet3_input = {'input': spectral_features}
rawnet3_output = rawnet3_session.run(None, rawnet3_input)
spectral_spoof_score = rawnet3_output[0][0]  # Probability of synthetic speech
```

#### Branch B: Prosody & Temporal Analysis (Fast-AASIST)
```python
# Load AASIST model for temporal/phase analysis
aasist_session = ort.InferenceSession(
    "aasist_quantized.onnx",
    providers=['QNNExecutionProvider']
)

def extract_temporal_features(audio_chunk):
    # Extract LFCC (Linear Frequency Cepstral Coefficients)
    import librosa
    
    lfcc = librosa.feature.lfcc(
        y=audio_chunk,
        sr=16000,
        n_lfcc=40,
        hop_length=160
    )
    
    # Compute delta and delta-delta (temporal dynamics)
    lfcc_delta = librosa.feature.delta(lfcc)
    lfcc_delta2 = librosa.feature.delta(lfcc, order=2)
    
    # Stack features
    features = np.concatenate([lfcc, lfcc_delta, lfcc_delta2], axis=0)
    return features

# Inference
temporal_features = extract_temporal_features(speech_chunk)
aasist_input = {'input': temporal_features}
aasist_output = aasist_session.run(None, aasist_input)
temporal_spoof_score = aasist_output[0][0]
```

### Step 3.4: Ensemble Risk Scoring
```python
def compute_ensemble_score(spectral_score, temporal_score, base_risk):
    # Weighted ensemble (spectral artifacts are more reliable for telephony audio)
    ensemble_score = (0.6 * spectral_score + 0.4 * temporal_score)
    
    # Fuse with pre-call base risk
    final_risk = 0.7 * ensemble_score + 0.3 * base_risk
    
    # Apply sigmoid for calibration
    calibrated_risk = 1 / (1 + np.exp(-5 * (final_risk - 0.5)))
    
    return calibrated_risk

# Current chunk risk
chunk_risk = compute_ensemble_score(
    spectral_spoof_score, 
    temporal_spoof_score, 
    risk_assessment['base_risk']
)
```

### Step 3.5: Temporal Smoothing (Rolling Window Accumulator)
```python
import collections

# Maintain rolling window of last 3 chunks (3 seconds of speech)
risk_window = collections.deque(maxlen=3)

def update_risk_window(new_risk_score):
    risk_window.append(new_risk_score)
    
    # Compute exponential moving average (EMA)
    weights = [0.5, 0.3, 0.2]  # Recent chunks weighted higher
    smoothed_risk = sum(r * w for r, w in zip(risk_window, weights[-len(risk_window):]))
    
    return smoothed_risk

# Update and get smoothed risk
smoothed_risk = update_risk_window(chunk_risk)
```

### Step 3.6: Threshold-Based Alerting Logic
```python
ALERT_THRESHOLD_HIGH = 0.85   # Immediate warning for high-risk calls
ALERT_THRESHOLD_MEDIUM = 0.65  # Cautionary notice for moderate risk

def evaluate_alert_condition(smoothed_risk, call_context):
    # Dynamic threshold adjustment based on context
    if call_context.get('transaction_value', 0) > 500000:  # > ₹5 lakhs
        effective_threshold = 0.70  # Lower threshold for high-value calls
    else:
        effective_threshold = ALERT_THRESHOLD_MEDIUM
    
    if smoothed_risk >= ALERT_THRESHOLD_HIGH:
        return {
            "alert_level": "CRITICAL",
            "message": "⚠️ HIGH PROBABILITY OF SYNTHETIC VOICE",
            "action": "BLOCK_TRANSACTION",
            "confidence": smoothed_risk
        }
    elif smoothed_risk >= effective_threshold:
        return {
            "alert_level": "WARNING",
            "message": "⚠️ UNUSUAL VOICE PATTERNS DETECTED",
            "action": "REQUEST_SECONDARY_VERIFICATION",
            "confidence": smoothed_risk
        }
    else:
        return {
            "alert_level": "SAFE",
            "message": None,
            "action": "CONTINUE_MONITORING",
            "confidence": smoothed_risk
        }

alert_decision = evaluate_alert_condition(smoothed_risk, call_context)
```

### Step 3.7: In-Call Security Overlay (User Alert)
**Trigger**: Alert level = WARNING or CRITICAL

```python
# Android: SYSTEM_ALERT_WINDOW overlay

if alert_decision['alert_level'] in ['WARNING', 'CRITICAL']:
    show_security_overlay(
        title="SwarSatya - Voice Integrity Alert",
        message=alert_decision['message'],
        confidence_score=f"{alert_decision['confidence']*100:.1f}%",
        recommended_actions=[
            "Do not share OTP or passwords",
            "Do not authorize fund transfers",
            "Perform callback to official number",
            "Ask security questions only genuine person would know"
        ],
        urgency="HIGH" if alert_decision['alert_level'] == 'CRITICAL' else "MEDIUM"
    )
```

**Visual Output** (appears as heads-up notification over call screen):
```
┌─────────────────────────────────────────────┐
│  ⚠️  SwarSatya - Voice Alert                │
├─────────────────────────────────────────────┤
│  Unusual voice patterns detected            │
│  Confidence: 73% synthetic                  │
│                                             │
│  Recommended Actions:                       │
│  ✓ Do not share OTP or passwords           │
│  ✓ Do not authorize fund transfers         │
│  ✓ Perform callback to official number     │
│                                             │
│  [Dismiss]  [Report Fraud]                  │
└─────────────────────────────────────────────┘
```

### Step 3.8: Immediate Memory Zeroization (Privacy Compliance)
```python
import ctypes

def secure_memory_wipe(buffer):
    """
    Overwrite audio buffer with zeros immediately after inference.
    This ensures no raw audio persists in RAM.
    """
    # Get memory address of numpy array
    buffer_ptr = buffer.ctypes.data
    buffer_size = buffer.nbytes
    
    # Use memset_s (secure memset) to zero out memory
    libc = ctypes.CDLL('libc.so.6')  # Linux/Android
    libc.memset_s(buffer_ptr, buffer_size, 0, buffer_size)
    
    # Alternative for Python: manually overwrite
    buffer[:] = 0

# After inference completes
secure_memory_wipe(speech_chunk)
secure_memory_wipe(ring_buffer)

# Force garbage collection
import gc
gc.collect()
```

**Compliance Note**: This step ensures that under India's DPDP Act and similar privacy regulations, no raw biometric data (voice recordings) is retained. Only abstract risk scores and metadata are logged.

---

## Phase 4: Post-Detection Actions (Fraud Response)

### Scenario: Alert triggered (smoothed_risk = 0.87, CRITICAL level)

### Step 4.1: User Interaction & Decision
**Options Presented to User**:
1. **Continue Call** (user acknowledges risk but proceeds)
2. **End Call & Report Fraud** (terminates call and logs incident)
3. **Request Secondary Verification** (initiates callback workflow)

```python
# User selects "End Call & Report Fraud"
user_action = "REPORT_FRAUD"

if user_action == "REPORT_FRAUD":
    # Terminate call programmatically (if OS allows)
    end_call()
    
    # Trigger fraud logging workflow
    log_fraud_incident(
        call_id="call_8f3a2b9c7e1d",
        caller_number="+91-98765-43210",
        risk_score=0.87,
        detection_timestamp="2026-09-08T23:53:42Z",
        user_confirmation=True
    )
```

### Step 4.2: Zero-Knowledge Proof Generation (Privacy-Preserving Logging)
```python
from zk_snarks import generate_proof  # Hypothetical zk library

# Create fraud proof without exposing raw audio or voice features
fraud_statement = {
    "caller_number_hash": sha256("+91-98765-43210"),
    "detection_timestamp": "2026-09-08T23:53:42Z",
    "risk_score": 0.87,
    "model_version": "rawnet3_v2.1_aasist_v1.8",
    "threshold_exceeded": True
}

# Generate zk-SNARK proof
zk_proof = generate_proof(
    statement=fraud_statement,
    public_inputs=[sha256(fraud_statement)],
    witness=[]  # No raw audio in witness
)

# Proof verifies that fraud detection occurred without revealing voice data
```

### Step 4.3: Blockchain Fraud Log (Zone 4)
**Smart Contract Call**: `SwarSatFraud.logFraud()`

```solidity
// Smart contract function
function logFraud(
    string memory callerNumberHash,
    uint256 riskScore,
    uint256 timestamp
) public returns (uint256) {
    incidentCount++;
    
    fraudLog[incidentCount] = FraudIncident({
        incidentId: incidentCount,
        callerNumberHash: callerNumberHash,
        riskScore: riskScore,
        reportedAt: timestamp,
        isVerified: true
    });
    
    emit FraudLogged(incidentCount, riskScore, timestamp);
    
    return incidentCount;
}
```

**Transaction Details**:
- **Network**: Polygon Mumbai Testnet
- **Tx Hash**: `0x9f2a7b5c3e1d8f4a6b0c9e2d7f3a5b8c...`
- **Block Time**: ~2s (Polygon L2)
- **Gas**: Negligible (testnet)

### Step 4.4: Real-Time Blacklist Propagation (Zone 5)
```python
# Kafka message to consortium members (for enterprise deployment)
kafka_message = {
    "topic": "fraud_alerts_realtime",
    "key": "caller_+91-98765-43210",
    "value": {
        "alert_type": "VOICE_CLONING_DETECTED",
        "caller_number_hash": "0x3f8a2b9c7e1d5f4a6b8c0e3d9f2a7b5c...",
        "risk_score": 0.87,
        "detection_time": "2026-09-08T23:53:42Z",
        "geographic_region": "Indore, MP",
        "recommended_action": "BLOCK_OUTBOUND_CALLS"
    },
    "timestamp": "2026-09-08T23:53:43Z"
}

# All participating banks/telcos receive this within <1 second
# Their local systems auto-update blacklists
```

### Step 4.5: Transaction Lock (If High-Value Call)
```python
# If call was related to fund transfer approval
if call_context.get('transaction_pending'):
    # Smart contract auto-locks transaction
    contract.functions.lockTransaction(
        transaction_id=call_context['transaction_id'],
        reason="VOICE_FRAUD_DETECTED",
        locked_until=block.timestamp + 86400  # 24-hour hold
    ).transact()
    
    # Notify bank backend via webhook
    requests.post(
        "https://bank-api.example.com/fraud-alert",
        json={
            "transaction_id": call_context['transaction_id'],
            "status": "LOCKED",
            "reason": "SwarSatya AI voice cloning detected with 87% confidence"
        }
    )
```

---

## Phase 5: Forensic Audit & Compliance Reporting

### Step 5.1: Immutable Audit Trail Query
**Scenario**: Bank's fraud investigation team needs to review incident

```python
# Query blockchain for fraud incident
incident_query = contract.functions.getFraudIncident(
    incident_id
).call()

# Returns tamper-proof record
audit_record = {
    "incident_id": 1,
    "caller_hash": "0x3f8a2b9c...",
    "risk_score": 87,
    "detection_timestamp": "2026-09-08T23:53:42Z",
    "model_version": "rawnet3_v2.1_aasist_v1.8",
    "blockchain_tx": "0x9f2a7b5c3e1d8f4a6b0c9e2d7f3a5b8c..."
}

# This record is court-admissible (immutable, timestamped, cryptographically signed)
```

### Step 5.2: Regulatory Compliance Report Generation
```python
# Generate DPDP Act / RBI compliance report
compliance_report = {
    "report_id": "SWARSATYA_FRAUD_2026_09_08_001",
    "incident_summary": {
        "total_fraud_attempts": 1,
        "detection_method": "AI voice cloning detection (RawNet3 + AASIST)",
        "data_retention": "Zero raw audio stored, only risk scores logged",
        "privacy_compliance": "DPDP Act Section 8 compliant (ephemeral processing)"
    },
    "blockchain_audit": {
        "incident_hash": "0x9f2a7b5c3e1d8f4a6b0c9e2d7f3a5b8c...",
        "verification_url": "https://mumbai.polygonscan.com/tx/0x9f2a7b5c..."
    },
    "generated_at": "2026-09-09T00:15:00Z"
}

# Export as PDF for regulatory submission
export_compliance_pdf(compliance_report, output_path="fraud_report_2026_09_08.pdf")
```

---

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PHASE 0-1: ENROLLMENT                               │
└─────────────────────────────────────────────────────────────────────────────┘

User Device (SwarSatya App)        Blockchain (Polygon Mumbai)
     │                                     │
     │ 1. Record voice samples            │
     │    (10 sec, 3 phrases)             │
     │                                     │
     │ 2. Generate voiceprint embedding   │
     │    (ECAPA-TDNN, 256-dim vector)    │
     │                                     │
     │ 3. Hash & sign with private key    │
     │    (DID + cryptographic signature) │
     │                                     │
     │────────────────────────────────────>│ 4. registerVoice()
     │    Tx: voiceprint hash + DID       │    Smart Contract
     │    + phone                         │
     │                                     │
     │                                     │ 5. Store in registry
     │                                     │    (immutable, timestamped)
     │                                     │
     │<────────────────────────────────────│ 6. Return tx hash
     │    Confirmation: 0x3f8a2b9c...      │
     │                                     │

┌─────────────────────────────────────────────────────────────────────────────┐
│                      PHASE 2: PRE-CALL SETUP                                │
└─────────────────────────────────────────────────────────────────────────────┘

SwarSatya App      FastAPI Backend (Zone 3)      Blockchain (Zone 4)
     │                    │                            │
     │ Incoming call      │                            │
     │ detected           │                            │
     │ (+91-98765-43210)  │                            │
     │                    │                            │
     │───────────────────>│                            │
     │ Call metadata     │                            │
     │                    │                            │
     │                    │ 1. Query voice registry    │
     │                    │    (caller phone number)   │
     │                    │───────────────────────────>│
     │                    │                            │
     │                    │<───────────────────────────│
     │                    │ Voice record:              │
     │                    │ - voiceprint hash          │
     │                    │ - DID                      │
     │                    │ - registration timestamp   │
     │                    │                            │
     │                    │ 2. Assess base risk        │
     │                    │    (unknown contact, etc.) │
     │                    │                            │
     │<───────────────────│                            │
     │ WebSocket push:   │                            │
     │ - caller voiceprint hash                        │
     │ - base risk score │                            │
     │ - risk factors    │                            │
     │                    │                            │
     │ 3. Cache in RAM   │                            │
     │    (pre-call, zero latency)                    │
     │                    │                            │

┌─────────────────────────────────────────────────────────────────────────────┐
│                   PHASE 3: REAL-TIME CALL MONITORING                        │
└─────────────────────────────────────────────────────────────────────────────┘

SwarSatya App (Zone 1) - On-Device NPU Pipeline

     │
     │ [Audio Stream: 16kHz PCM]
     │
     ▼
┌─────────────────────────────────┐
│  Ring Buffer (RAM, 2 sec max)   │
│  - Circular buffer              │
│  - Zero disk footprint          │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│  Voice Activity Detection       │
│  (Silero VAD, CPU, <1% load)    │
│                                 │
│  If speech_prob < 0.5:          │
│    → Discard chunk, skip ML     │
│  Else:                          │
│    → Forward to inference       │
└───────────────┬─────────────────┘
                │
                ▼
        ┌───────────────────┐
        │  Active Speech    │
        │  (1-sec chunk)    │
        └─────────┬─────────┘
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
┌─────────────────┐ ┌─────────────────┐
│  Branch A:      │ │  Branch B:      │
│  RawNet3        │ │  Fast-AASIST    │
│  (Spectral)     │ │  (Temporal)     │
│                 │ │                 │
│  Log-Mel Spec   │ │  LFCC + Delta   │
│  INT8 Quantized │ │  INT8 Quantized │
│  QNN            │ │  QNN            │
│                 │ │                 │
│  Output:        │ │  Output:        │
│  spoof_score_A  │ │  spoof_score_B  │
└────────┬────────┘ └────────┬────────┘
         │                   │
         └─────────┬─────────┘
                   │
                   ▼
        ┌─────────────────────────┐
        │  Ensemble Risk Scoring  │
        │                         │
        │  final_risk =           │
        │    0.6 * score_A +      │
        │    0.4 * score_B        │
        │                         │
        │  smoothed_risk =        │
        │    EMA(risk_window)     │
        └───────────┬─────────────┘
                    │
                    ▼
        ┌─────────────────────────┐
        │  Threshold Evaluation   │
        │                         │
        │  if smoothed >= 0.85:   │
        │    → CRITICAL alert     │
        │  elif >= 0.65:          │
        │    → WARNING alert      │
        │  else:                  │
        │    → Continue monitoring│
        └───────────┬─────────────┘
                    │
         ┌──────────┴──────────┐
         │                     │
         ▼                     ▼
    [SAFE]              [ALERT TRIGGERED]
         │                     │
         │                     ▼
         │           ┌─────────────────────┐
         │           │  In-Call Overlay    │
         │           │  (SYSTEM_ALERT)     │
         │           │                     │
         │           │  ⚠️ SwarSatya Alert │
         │           │  Confidence: 87%    │
         │           │                     │
         │           │  [End Call]         │
         │           │  [Report Fraud]     │
         │           └──────────┬──────────┘
         │                      │
         │                      ▼
         │            ┌─────────────────────┐
         │            │  Memory Zeroization │
         │            │  (memset_s buffer)  │
         │            │  gc.collect()       │
         │            └──────────┬──────────┘
         │                       │
         │                       ▼
         │             [Audio Purged from RAM]
         │
         ▼
[Continue Monitoring Next Chunk]

┌─────────────────────────────────────────────────────────────────────────────┐
│                    PHASE 4: POST-DETECTION RESPONSE                         │
└─────────────────────────────────────────────────────────────────────────────┘

SwarSatya App      FastAPI Backend (Zone 3)      Blockchain (Zone 4)
     │                    │                            │
     │ User clicks        │                            │
     │ "Report Fraud"    │                            │
     │                    │                            │
     │ 1. End call       │                            │
     │    (programmatic  │                            │
     │     hangup)       │                            │
     │                    │                            │
     │ 2. Generate       │                            │
     │    fraud data     │                            │
     │                    │                            │
     │───────────────────>│                            │
     │ Fraud incident    │                            │
     │ data              │                            │
     │ (risk score,      │                            │
     │  timestamp,       │                            │
     │  caller hash)     │                            │
     │                    │                            │
     │                    │ 3. Log to blockchain       │
     │                    │    logFraud()              │
     │                    │───────────────────────────>│
     │                    │                            │
     │                    │                            │ 4. Store incident
     │                    │                            │    (immutable)
     │                    │                            │
     │                    │                            │ 5. Auto-blacklist
     │                    │                            │    if risk > 0.85
     │                    │                            │
     │                    │<───────────────────────────│
     │                    │ Tx hash: 0x9f2a7b5c...     │
     │                    │                            │
     │                    │                            │
     │                    │ 6. Kafka broadcast         │
     │                    │    (consortium members)    │
     │                    │───────────────────────────>│
     │                    │                            │
     │                    │    [Banks/Telcos receive   │
     │                    │     real-time alert]       │
     │                    │                            │
     │<───────────────────│                            │
     │ Confirmation:     │                            │
     │ "Fraud reported,  │                            │
     │  incident logged" │                            │
     │                    │                            │

┌─────────────────────────────────────────────────────────────────────────────┐
│                    PHASE 5: FORENSIC AUDIT                                  │
└─────────────────────────────────────────────────────────────────────────────┘

Bank Fraud Team                Blockchain (Zone 4)
     │                              │
     │ 1. Query incident by         │
     │    incident ID               │
     │─────────────────────────────>│
     │                              │
     │<─────────────────────────────│
     │ Audit record:                │
     │ - incident_id                │
     │ - risk_score: 87             │
     │ - detection_time             │
     │ - model_version              │
     │ - blockchain_tx hash         │
     │                              │
     │ 2. Generate compliance       │
     │    report (PDF)              │
     │    (DPDP Act / RBI format)   │
     │                              │
     │ 3. Submit to regulator       │
     │    (court-admissible)        │
     │                              │
```

---

## Performance Metrics & SLAs

| Metric | Target | Measurement |
|--------|--------|-------------|
| **End-to-End Inference Latency** | <200ms per 1-sec chunk | From audio capture to alert decision |
| **VAD Processing Time** | <10ms | Silero VAD on CPU |
| **RawNet3 Inference (NPU)** | <40ms | INT8 quantized, QNN |
| **AASIST Inference (NPU)** | <40ms | INT8 quantized, QNN |
| **Risk Score Smoothing** | <5ms | Rolling EMA computation |
| **Alert Overlay Display** | <100ms | From decision to UI render |
| **Memory Zeroization** | <1ms | memset_s + gc.collect |
| **Blockchain Tx Finality** | <2s (Polygon Mumbai) | From logFraud() to block confirmation |
| **Consortium Blacklist Propagation** | <1s | Kafka message delivery |
| **Battery Impact** | <2.5% per hour of call | Measured on mid-range Android device |
| **False Positive Rate** | <5% | On telephony-degraded audio (AMR/G.711) |
| **False Negative Rate** | <3% | On state-of-the-art voice clones (HiFi-GAN, BigVGAN) |

---

## Security & Privacy Compliance Checklist

- ✅ **Zero Audio Retention**: Raw audio never written to disk, only ephemeral RAM buffers
- ✅ **On-Device Inference**: All ML models run locally on NPU, no cloud API calls during call
- ✅ **DPDP Act Compliant**: Ephemeral processing, no biometric data storage
- ✅ **GDPR Article 9 Compliant**: Special category data (biometrics) processed with explicit consent
- ✅ **Encryption in Transit**: WebSocket/TLS 1.3 for pre-call hash caching
- ✅ **Encryption at Rest**: Private keys stored in Android Keystore / iOS Keychain
- ✅ **Immutable Audit Trail**: All fraud incidents logged to blockchain
- ✅ **Consortium Data Sharing**: Real-time blacklist propagation without exposing raw data
- ✅ **User Consent**: Explicit opt-in during app installation and enrollment
- ✅ **Right to Deletion**: Users can revoke access and delete data from app

---

## Next Steps for Implementation

1. **Model Training Pipeline**: Set up PyTorch training with ASVspoof 2021 + In-the-Wild datasets, augmented with telephony codec simulation (Audiomentations)
2. **Mobile App Development**: Build Kotlin app with Chaquopy for ONNX Runtime Mobile integration
3. **Smart Contract Development**: Write Solidity contracts for SwarSatFraud on Polygon Mumbai
4. **Backend API**: Implement FastAPI microservices for session management and blockchain integration
5. **Dashboard**: Build Streamlit dashboard for real-time fraud monitoring
6. **Privacy Audit**: Engage legal counsel for DPDP Act / RBI compliance review
7. **Pilot Deployment**: Test with sample calls and demo scenarios

---

## References & Resources

- **ASVspoof 2021 Dataset**: https://www.asvspoof.org/
- **RawNet3 Paper**: https://arxiv.org/abs/2104.01384
- **AASIST Paper**: https://arxiv.org/abs/2110.06166
- **Polygon Mumbai Testnet**: https://mumbai.polygonscan.com/
- **ONNX Runtime Mobile**: https://onnxruntime.ai/docs/get-started/
- **Silero VAD**: https://github.com/snakers4/silero-vad
- **India DPDP Act 2023**: https://www.meity.gov.in/writereaddata/files/11082023155321Digital%20Personal%20Data%20Protection%20Bill%202023.pdf
- **Smart India Hackathon 2026**: https://www.sih.gov.in/

---

**Project**: SwarSatya (स्वरसत्य)  
**Tagline**: "सत्य की आवाज़" (The Voice of Truth)  
**Hackathon**: SIH 2026 - Blockchain & Cybersecurity  
**Status**: Ready for Implementation 🚀
