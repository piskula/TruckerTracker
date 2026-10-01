package sk.momosilabs.truckTrack.file.storage

import org.springframework.stereotype.Service
import sk.momosilabs.truckTrack.file.model.TruckTrackFile
import sk.momosilabs.truckTrack.file.service.FileStorageService
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream

@Service
class S3FileStorageService(
    private val s3Client: S3Client,
) : FileStorageService {

    override fun upload(file: TruckTrackFile, bucket: String, key: String) {
        ensureBucketExists(bucket)
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType.toString())
                .build(),
            RequestBody.fromBytes(file.content),
        )
    }

    override fun download(bucket: String, key: String): InputStream =
        s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        )

    override fun delete(bucket: String, key: String) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        )
    }

    private fun ensureBucketExists(bucket: String) {
        val exists = try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
            true
        } catch (_: NoSuchBucketException) {
            false
        }
        if (!exists) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
        }
    }
}
