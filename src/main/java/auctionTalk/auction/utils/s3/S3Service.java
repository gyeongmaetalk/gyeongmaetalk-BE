package auctionTalk.auction.utils.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // Presigned Url 생성
    public String generatePresignedPutUrl(String category, String originalFileName) {
        // 파일명 생성 (네가 준 메서드 그대로 사용)
        String fileName = createFileName(category, Objects.requireNonNull(originalFileName));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType("image/webp")
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                builder -> builder
                        .signatureDuration(Duration.ofMinutes(3))
                        .putObjectRequest(putObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    public String generatePresignedGetUrl(String fileKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey) // DB에 저장한 파일 경로 그대로 넣으면 됨
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                builder -> builder
                        .signatureDuration(Duration.ofMinutes(10)) // 조회는 10분 정도 보통 충분함
                        .getObjectRequest(getObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    /**
     * 파일명 생성
     * @param category
     * @param originalFileName
     * @return 작명된 파일 이름
     */
    public String createFileName(String category, String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(".");
        String fileExtension = originalFileName.substring(fileExtensionIndex);
        String fileName = originalFileName.substring(0, fileExtensionIndex);
        String random = String.valueOf(UUID.randomUUID());

        return category + "/" + fileName + "_" + random + fileExtension;
    }

//    /**
//     * 이미지 삭제
//     * @param fileUrl
//     */
//    public void deleteFile(String fileUrl) {
//        String[] deleteUrl = fileUrl.split("/", 4);
//        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, deleteUrl[3]));
//    }
}