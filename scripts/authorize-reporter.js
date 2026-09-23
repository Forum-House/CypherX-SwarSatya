// Run after deploy.js. Authorizes the backend service wallet (BACKEND_REPORTER_ADDRESS
// in .env) so it can call logFraud(). Only the contract owner (deployer) can do this.
const hre = require("hardhat");
require("dotenv").config();

async function main() {
  const contractAddress = process.env.CONTRACT_ADDRESS;
  const reporterAddress = process.env.BACKEND_REPORTER_ADDRESS;

  if (!contractAddress) throw new Error("Set CONTRACT_ADDRESS in .env (from deploy.js output)");
  if (!reporterAddress) throw new Error("Set BACKEND_REPORTER_ADDRESS in .env");

  const contract = await hre.ethers.getContractAt("SwarSatFraud", contractAddress);
  const tx = await contract.authorizeReporter(reporterAddress);
  await tx.wait();

  console.log(`Authorized ${reporterAddress} as a fraud reporter.`);
  console.log("Tx:", `https://amoy.polygonscan.com/tx/${tx.hash}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
