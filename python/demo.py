"""
SwarSatya blockchain module -- end-to-end demo.

Walks through the full on-chain flow with real testnet transactions:
  1. A user wallet registers a voiceprint hash for a phone number.
  2. Anyone can read that record back (no gas needed).
  3. The backend (authorized reporter) logs 3 fraud reports against a
     *different* number -> triggers auto-blacklisting.
  4. We confirm the blacklist flag flipped on-chain.

Run this after: (1) deploying the contract with `npm run deploy:amoy`,
and (2) authorizing the backend wallet with `npm run authorize-reporter`.

Required env vars (put them in a .env file in this folder, or export them):
  AMOY_RPC_URL             e.g. https://rpc-amoy.polygon.technology
  CONTRACT_ADDRESS         from deploy.js output / python/deployed_address.txt
  USER_PRIVATE_KEY         wallet that "owns" the demo phone number
  BACKEND_PRIVATE_KEY      the authorized-reporter wallet used by your backend
"""

import hashlib
import os
import time

from dotenv import load_dotenv
from swarsat_client import SwarSatFraudClient

load_dotenv()

RPC_URL = os.environ["AMOY_RPC_URL"]
CONTRACT_ADDRESS = os.environ["CONTRACT_ADDRESS"]
USER_PRIVATE_KEY = os.environ["USER_PRIVATE_KEY"]
BACKEND_PRIVATE_KEY = os.environ["BACKEND_PRIVATE_KEY"]

import time
DEMO_PHONE_LEGIT = f"+9198765{int(time.time()) % 100000:05d}"
DEMO_PHONE_SCAM = "+919876500002"


def explorer_link(tx_hash: str) -> str:
    return f"https://amoy.polygonscan.com/tx/{tx_hash}"


def fake_voiceprint_hash(seed: str) -> bytes:
    """Stand-in for the real ECAPA-TDNN embedding hash -- just for the demo."""
    return hashlib.sha256(seed.encode()).digest()


def main():
    print("=" * 60)
    print("SwarSatya Blockchain Module -- Live Demo (Polygon Amoy)")
    print("=" * 60)

    user_client = SwarSatFraudClient(RPC_URL, CONTRACT_ADDRESS, USER_PRIVATE_KEY)
    backend_client = SwarSatFraudClient(RPC_URL, CONTRACT_ADDRESS, BACKEND_PRIVATE_KEY)

    # ---- Step 1: user registers their voice ----
    print(f"\n[1] Registering voiceprint for {DEMO_PHONE_LEGIT} ...")
    vp_hash = fake_voiceprint_hash(DEMO_PHONE_LEGIT)
    result = user_client.register_voice(DEMO_PHONE_LEGIT, vp_hash)
    print(f"    Confirmed in block {result['block']}")
    print(f"    {explorer_link(result['tx_hash'])}")

    # ---- Step 2: read it back (free, no gas) ----
    print(f"\n[2] Reading record for {DEMO_PHONE_LEGIT} back from chain ...")
    record = user_client.get_voice_record(DEMO_PHONE_LEGIT)
    print(f"    Owner wallet: {record['owner']}")
    print(f"    Registered at (unix): {record['registered_at']}")

    # ---- Step 3: backend logs 3 fraud reports against a different number ----
    print(f"\n[3] Backend logging fraud reports for {DEMO_PHONE_SCAM} ...")
    for i in range(1, 4):
        result = backend_client.log_fraud(DEMO_PHONE_SCAM, f"AI flagged spoofed call #{i}")
        print(f"    Report {i}/3 confirmed -> {explorer_link(result['tx_hash'])}")
        time.sleep(1)

    # ---- Step 4: confirm auto-blacklist ----
    print(f"\n[4] Checking blacklist status for {DEMO_PHONE_SCAM} ...")
    blacklisted = backend_client.is_blacklisted(DEMO_PHONE_SCAM)
    count = backend_client.get_fraud_log_count(DEMO_PHONE_SCAM)
    print(f"    Fraud reports on file: {count}")
    print(f"    Blacklisted: {blacklisted}")

    print("\n" + "=" * 60)
    print("Demo complete. Every step above is a real, verifiable")
    print("transaction on Polygon Amoy -- open any tx link to show judges.")
    print("=" * 60)


if __name__ == "__main__":
    main()
