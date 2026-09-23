// SPDX-License-Identifier: MIT
pragma solidity ^0.8.19;

import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/// @title SwarSatFraud
/// @notice On-chain voice registry + fraud log for the SwarSatya spoof-detection system.
///         Each phone number is bound to a voiceprint hash and a wallet (DID). Only the
///         wallet that signs the registration message can claim a phone number, preventing
///         anyone from overwriting someone else's registration. Fraud reports come only from
///         authorized backend services; after a threshold of reports a number is auto-blacklisted.
contract SwarSatFraud is Ownable {
    using ECDSA for bytes32;
    using MessageHashUtils for bytes32;

    struct VoiceRecord {
        bytes32 voiceprintHash;
        address owner;
        uint256 registeredAt;
        bool exists;
    }

    struct FraudLog {
        string reason;
        uint256 timestamp;
        address reportedBy;
    }

    /// @dev How many fraud reports before a number is auto-blacklisted.
    uint256 public constant BLACKLIST_THRESHOLD = 3;

    mapping(string => VoiceRecord) private voiceRecords;      // phoneNumber => record
    mapping(string => FraudLog[]) private fraudLogs;           // phoneNumber => reports
    mapping(string => bool) public blacklisted;                // phoneNumber => blacklisted
    mapping(address => bool) public authorizedReporters;       // backend services allowed to log fraud

    event VoiceRegistered(string indexed phoneNumberIndexed, string phoneNumber, address indexed owner, bytes32 voiceprintHash);
    event FraudLogged(string indexed phoneNumberIndexed, string phoneNumber, string reason, address indexed reportedBy);
    event Blacklisted(string indexed phoneNumberIndexed, string phoneNumber);
    event ReporterAuthorized(address indexed reporter);
    event ReporterRevoked(address indexed reporter);

    modifier onlyAuthorizedReporter() {
        require(authorizedReporters[msg.sender] || msg.sender == owner(), "SwarSatFraud: not an authorized reporter");
        _;
    }

    constructor() Ownable(msg.sender) {}

    /// @notice Allow a backend service wallet to submit fraud reports.
    function authorizeReporter(address reporter) external onlyOwner {
        authorizedReporters[reporter] = true;
        emit ReporterAuthorized(reporter);
    }

    /// @notice Revoke a backend service's ability to submit fraud reports.
    function revokeReporter(address reporter) external onlyOwner {
        authorizedReporters[reporter] = false;
        emit ReporterRevoked(reporter);
    }

    /// @notice Register a voiceprint hash for a phone number. The caller must supply a
    ///         signature over keccak256(phoneNumber, voiceprintHash) made by the same wallet
    ///         that is calling this function, proving they control the claimed DID.
    /// @param phoneNumber The user's phone number (used as the record key).
    /// @param voiceprintHash keccak256 hash of the user's voice embedding (never the raw audio).
    /// @param signature ECDSA signature over the message hash, produced by msg.sender's key.
    function registerVoice(
        string calldata phoneNumber,
        bytes32 voiceprintHash,
        bytes calldata signature
    ) external {
        require(!voiceRecords[phoneNumber].exists, "SwarSatFraud: phone number already registered");

        bytes32 messageHash = keccak256(abi.encodePacked(phoneNumber, voiceprintHash));
        address signer = messageHash.toEthSignedMessageHash().recover(signature);
        require(signer == msg.sender, "SwarSatFraud: signature does not match caller");

        voiceRecords[phoneNumber] = VoiceRecord({
            voiceprintHash: voiceprintHash,
            owner: msg.sender,
            registeredAt: block.timestamp,
            exists: true
        });

        emit VoiceRegistered(phoneNumber, phoneNumber, msg.sender, voiceprintHash);
    }

    /// @notice Read back a registered voice record.
    function getVoiceRecord(string calldata phoneNumber)
        external
        view
        returns (bytes32 voiceprintHash, address owner, uint256 registeredAt, bool exists)
    {
        VoiceRecord memory r = voiceRecords[phoneNumber];
        return (r.voiceprintHash, r.owner, r.registeredAt, r.exists);
    }

    /// @notice Submit a fraud report against a phone number. Restricted to authorized backend
    ///         reporters so the log can't be spammed by arbitrary wallets. Auto-blacklists the
    ///         number once BLACKLIST_THRESHOLD reports have accumulated.
    function logFraud(string calldata phoneNumber, string calldata reason) external onlyAuthorizedReporter {
        fraudLogs[phoneNumber].push(FraudLog({
            reason: reason,
            timestamp: block.timestamp,
            reportedBy: msg.sender
        }));

        emit FraudLogged(phoneNumber, phoneNumber, reason, msg.sender);

        if (!blacklisted[phoneNumber] && fraudLogs[phoneNumber].length >= BLACKLIST_THRESHOLD) {
            blacklisted[phoneNumber] = true;
            emit Blacklisted(phoneNumber, phoneNumber);
        }
    }

    /// @notice Number of fraud reports on file for a phone number.
    function getFraudLogCount(string calldata phoneNumber) external view returns (uint256) {
        return fraudLogs[phoneNumber].length;
    }

    /// @notice Read a single fraud report by index.
    function getFraudLog(string calldata phoneNumber, uint256 index)
        external
        view
        returns (string memory reason, uint256 timestamp, address reportedBy)
    {
        FraudLog memory log = fraudLogs[phoneNumber][index];
        return (log.reason, log.timestamp, log.reportedBy);
    }

    /// @notice Whether a phone number is currently blacklisted.
    function isBlacklisted(string calldata phoneNumber) external view returns (bool) {
        return blacklisted[phoneNumber];
    }
}
