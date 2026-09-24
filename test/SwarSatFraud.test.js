const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("SwarSatFraud", function () {
  let SwarSatFraud, swarSatFraud;
  let owner, reporter, user, attacker;

  beforeEach(async function () {
    [owner, reporter, user, attacker] = await ethers.getSigners();
    SwarSatFraud = await ethers.getContractFactory("SwarSatFraud");
    swarSatFraud = await SwarSatFraud.deploy();
  });

  describe("Deployment", function () {
    it("Should set the right owner", async function () {
      expect(await swarSatFraud.owner()).to.equal(owner.address);
    });

    it("Should have a blacklist threshold of 3", async function () {
      expect(await swarSatFraud.BLACKLIST_THRESHOLD()).to.equal(3);
    });
  });

  describe("Reporter Authorization", function () {
    it("Should authorize a reporter", async function () {
      await expect(swarSatFraud.authorizeReporter(reporter.address))
        .to.emit(swarSatFraud, "ReporterAuthorized")
        .withArgs(reporter.address);
      
      expect(await swarSatFraud.authorizedReporters(reporter.address)).to.be.true;
    });

    it("Should revoke a reporter", async function () {
      await swarSatFraud.authorizeReporter(reporter.address);
      await expect(swarSatFraud.revokeReporter(reporter.address))
        .to.emit(swarSatFraud, "ReporterRevoked")
        .withArgs(reporter.address);
      
      expect(await swarSatFraud.authorizedReporters(reporter.address)).to.be.false;
    });

    it("Should prevent non-owners from authorizing reporters", async function () {
      await expect(swarSatFraud.connect(attacker).authorizeReporter(reporter.address))
        .to.be.revertedWithCustomError(swarSatFraud, "OwnableUnauthorizedAccount");
    });
  });

  describe("Voice Registration", function () {
    it("Should register a voice successfully", async function () {
      const phoneNumber = "+1234567890";
      const voiceprintHash = ethers.keccak256(ethers.toUtf8Bytes("dummy_voiceprint"));
      
      // Compute keccak256(abi.encodePacked(phoneNumber, voiceprintHash))
      const messageHash = ethers.solidityPackedKeccak256(
        ["string", "bytes32"],
        [phoneNumber, voiceprintHash]
      );
      
      // Sign the hash (as bytes)
      const signature = await user.signMessage(ethers.getBytes(messageHash));
      
      await expect(swarSatFraud.connect(user).registerVoice(phoneNumber, voiceprintHash, signature))
        .to.emit(swarSatFraud, "VoiceRegistered")
        .withArgs(phoneNumber, phoneNumber, user.address, voiceprintHash);
        
      const record = await swarSatFraud.getVoiceRecord(phoneNumber);
      expect(record.voiceprintHash).to.equal(voiceprintHash);
      expect(record.owner).to.equal(user.address);
      expect(record.exists).to.be.true;
    });

    it("Should prevent registration with an invalid signature", async function () {
      const phoneNumber = "+1234567890";
      const voiceprintHash = ethers.keccak256(ethers.toUtf8Bytes("dummy_voiceprint"));
      
      const messageHash = ethers.solidityPackedKeccak256(
        ["string", "bytes32"],
        [phoneNumber, voiceprintHash]
      );
      
      // Sign with a different wallet
      const signature = await attacker.signMessage(ethers.getBytes(messageHash));
      
      // But caller is user
      await expect(swarSatFraud.connect(user).registerVoice(phoneNumber, voiceprintHash, signature))
        .to.be.revertedWith("SwarSatFraud: signature does not match caller");
    });
  });

  describe("Fraud Logging & Blacklisting", function () {
    const phoneNumber = "+1234567890";

    beforeEach(async function () {
      await swarSatFraud.authorizeReporter(reporter.address);
    });

    it("Should allow an authorized reporter to log fraud", async function () {
      await expect(swarSatFraud.connect(reporter).logFraud(phoneNumber, "Spam call detected"))
        .to.emit(swarSatFraud, "FraudLogged")
        .withArgs(phoneNumber, phoneNumber, "Spam call detected", reporter.address);
        
      expect(await swarSatFraud.getFraudLogCount(phoneNumber)).to.equal(1);
    });

    it("Should prevent an unauthorized address from logging fraud", async function () {
      await expect(swarSatFraud.connect(attacker).logFraud(phoneNumber, "Fake reason"))
        .to.be.revertedWith("SwarSatFraud: not an authorized reporter");
    });

    it("Should auto-blacklist after BLACKLIST_THRESHOLD logs", async function () {
      // 1st report
      await swarSatFraud.connect(reporter).logFraud(phoneNumber, "Reason 1");
      expect(await swarSatFraud.isBlacklisted(phoneNumber)).to.be.false;
      
      // 2nd report
      await swarSatFraud.connect(reporter).logFraud(phoneNumber, "Reason 2");
      expect(await swarSatFraud.isBlacklisted(phoneNumber)).to.be.false;
      
      // 3rd report
      await expect(swarSatFraud.connect(reporter).logFraud(phoneNumber, "Reason 3"))
        .to.emit(swarSatFraud, "Blacklisted")
        .withArgs(phoneNumber, phoneNumber);
        
      expect(await swarSatFraud.isBlacklisted(phoneNumber)).to.be.true;
    });
  });
});
