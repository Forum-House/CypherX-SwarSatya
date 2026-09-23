const hre = require("hardhat");
const fs = require("fs");
const path = require("path");

async function main() {
  console.log("Deploying SwarSatFraud to", hre.network.name, "...");

  const SwarSatFraud = await hre.ethers.getContractFactory("SwarSatFraud");
  const contract = await SwarSatFraud.deploy();
  await contract.waitForDeployment();

  const address = await contract.getAddress();
  console.log("SwarSatFraud deployed at:", address);
  console.log("View on explorer:", `https://amoy.polygonscan.com/address/${address}`);

  // Save address + ABI for the Python client to pick up.
  const artifact = await hre.artifacts.readArtifact("SwarSatFraud");
  const outDir = path.join(__dirname, "..", "python");
  fs.writeFileSync(path.join(outDir, "abi.json"), JSON.stringify(artifact.abi, null, 2));
  fs.writeFileSync(
    path.join(outDir, "deployed_address.txt"),
    address
  );
  console.log("Saved ABI to python/abi.json and address to python/deployed_address.txt");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
