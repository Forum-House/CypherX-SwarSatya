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
