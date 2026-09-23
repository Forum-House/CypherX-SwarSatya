"""
Web3.py client for the SwarSatFraud smart contract.

Two roles use this client:
  - A USER wallet: signs and calls register_voice() to claim a phone number.
  - The BACKEND (authorized reporter) wallet: calls log_fraud(), reads records,
    and checks blacklist status.

Run scripts/deploy.js first -- it writes abi.json and deployed_address.txt
into this folder automatically.
"""

import json
import os
from pathlib import Path

from eth_account import Account
from eth_account.messages import encode_defunct
from web3 import Web3

HERE = Path(__file__).parent


class SwarSatFraudClient:
    def __init__(self, rpc_url: str, contract_address: str, private_key: str | None = None, abi_path: Path = HERE / "abi.json"):
        self.w3 = Web3(Web3.HTTPProvider(rpc_url))
        if not self.w3.is_connected():
            raise ConnectionError(f"Could not connect to RPC at {rpc_url}")

        with open(abi_path) as f:
            abi = json.load(f)

        self.contract = self.w3.eth.contract(
            address=Web3.to_checksum_address(contract_address), abi=abi
        )
        self.account = Account.from_key(private_key) if private_key else None

    # ---------- internal helper ----------
    def _send(self, fn, gas: int = 300_000):
        if not self.account:
            raise ValueError("This client was created without a private key -- can't send transactions.")
        tx = fn.build_transaction({
            "from": self.account.address,
            "nonce": self.w3.eth.get_transaction_count(self.account.address),
            "gas": gas,
            "gasPrice": self.w3.eth.gas_price,
        })
        signed = self.account.sign_transaction(tx)
        tx_hash = self.w3.eth.send_raw_transaction(signed.raw_transaction)
        receipt = self.w3.eth.wait_for_transaction_receipt(tx_hash)
        return receipt

    # ---------- writes ----------
    def register_voice(self, phone_number: str, voiceprint_hash: bytes) -> dict:
        """Sign proof-of-ownership and register a voiceprint hash for phone_number.
        Must be called with the USER's private key (the wallet claiming the number)."""
        message_hash = Web3.solidity_keccak(["string", "bytes32"], [phone_number, voiceprint_hash])
        signable = encode_defunct(primitive=message_hash)
        signature = self.account.sign_message(signable).signature

        fn = self.contract.functions.registerVoice(phone_number, voiceprint_hash, signature)
        receipt = self._send(fn)
        return {"tx_hash": receipt.transactionHash.hex(), "block": receipt.blockNumber}

    def log_fraud(self, phone_number: str, reason: str) -> dict:
        """Submit a fraud report. Must be called with an AUTHORIZED REPORTER private key."""
        fn = self.contract.functions.logFraud(phone_number, reason)
        receipt = self._send(fn)
        return {"tx_hash": receipt.transactionHash.hex(), "block": receipt.blockNumber}

    def authorize_reporter(self, reporter_address: str) -> dict:
        """Contract owner only: allow a wallet to call log_fraud()."""
        fn = self.contract.functions.authorizeReporter(Web3.to_checksum_address(reporter_address))
        receipt = self._send(fn)
        return {"tx_hash": receipt.transactionHash.hex(), "block": receipt.blockNumber}

    # ---------- reads (no gas, no signature needed) ----------
    def get_voice_record(self, phone_number: str) -> dict:
        voiceprint_hash, owner, registered_at, exists = self.contract.functions.getVoiceRecord(phone_number).call()
        return {
            "voiceprint_hash": voiceprint_hash.hex(),
            "owner": owner,
            "registered_at": registered_at,
            "exists": exists,
        }

    def get_fraud_log_count(self, phone_number: str) -> int:
        return self.contract.functions.getFraudLogCount(phone_number).call()

    def get_fraud_log(self, phone_number: str, index: int) -> dict:
        reason, timestamp, reported_by = self.contract.functions.getFraudLog(phone_number, index).call()
        return {"reason": reason, "timestamp": timestamp, "reported_by": reported_by}

    def is_blacklisted(self, phone_number: str) -> bool:
        return self.contract.functions.isBlacklisted(phone_number).call()
