package com.lobai.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.zip.CRC32;

/**
 * HipIdGenerator
 *
 * Human Identity Protocol ID 생성기
 * 포맷: HIP-{VERSION}-{USER_HASH}-{CHECKSUM}
 * 예시: HIP-01-A7F2E9C4-B3A1
 */
public class HipIdGenerator {

    private static final String VERSION = "01";
    private static final String PREFIX = "HIP";

    /**
     * HIP ID 생성
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param createdAt 사용자 생성 시각
     * @return HIP-01-A7F2E9C4-B3A1 형식의 ID
     */
    public static String generate(Long userId, String email, LocalDateTime createdAt) {
        if (userId == null || email == null || createdAt == null) {
            throw new IllegalArgumentException("userId, email, createdAt must not be null");
        }

        // USER_HASH: SHA-256(userId + email + timestamp) -> 앞 8자리
        String rawData = userId + "|" + email + "|" + createdAt.toString();
        String fullHash = DigestUtils.sha256Hex(rawData);
        String userHash = fullHash.substring(0, 8).toUpperCase();

        // CHECKSUM: CRC32(version + userHash) -> 4자리
        String checksum = calculateChecksum(VERSION + userHash);

        return String.format("%s-%s-%s-%s", PREFIX, VERSION, userHash, checksum);
    }

    /**
     * CRC32 기반 체크섬 계산
     *
     * @param input 입력 문자열
     * @return 4자리 HEX 체크섬
     */
    private static String calculateChecksum(String input) {
        CRC32 crc = new CRC32();
        crc.update(input.getBytes());
        long crcValue = crc.getValue();

        // CRC32 값의 하위 16비트를 4자리 HEX로 변환
        String hex = Long.toHexString(crcValue & 0xFFFF).toUpperCase();

        // 4자리 맞추기 (부족하면 0으로 패딩)
        return String.format("%4s", hex).replace(' ', '0');
    }

    /**
     * HIP ID 유효성 검증
     *
     * @param hipId 검증할 HIP ID
     * @return 유효하면 true
     */
    public static boolean validate(String hipId) {
        if (hipId == null || hipId.isEmpty()) {
            return false;
        }

        // 포맷 검증: HIP-XX-XXXXXXXX-XXXX
        String pattern = "^HIP-\\d{2}-[A-F0-9]{8}-[A-F0-9]{4}$";
        if (!hipId.matches(pattern)) {
            return false;
        }

        // 구성 요소 추출
        String[] parts = hipId.split("-");
        if (parts.length != 4) {
            return false;
        }

        String version = parts[1];
        String userHash = parts[2];
        String providedChecksum = parts[3];

        // 체크섬 재계산 및 비교
        String calculatedChecksum = calculateChecksum(version + userHash);
        return calculatedChecksum.equals(providedChecksum);
    }

    /**
     * HIP ID에서 버전 추출
     *
     * @param hipId HIP ID
     * @return 버전 (예: "01")
     */
    public static String extractVersion(String hipId) {
        if (!validate(hipId)) {
            throw new IllegalArgumentException("Invalid HIP ID: " + hipId);
        }
        return hipId.split("-")[1];
    }

    /**
     * HIP ID에서 사용자 해시 추출
     *
     * @param hipId HIP ID
     * @return 사용자 해시 (예: "A7F2E9C4")
     */
    public static String extractUserHash(String hipId) {
        if (!validate(hipId)) {
            throw new IllegalArgumentException("Invalid HIP ID: " + hipId);
        }
        return hipId.split("-")[2];
    }

    /**
     * 현재 HIP 프로토콜 버전 반환
     *
     * @return 현재 버전
     */
    public static String getCurrentVersion() {
        return VERSION;
    }
}
