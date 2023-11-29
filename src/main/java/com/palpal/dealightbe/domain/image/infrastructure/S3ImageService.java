package com.palpal.dealightbe.domain.image.infrastructure;

import static com.palpal.dealightbe.global.error.ErrorCode.EMPTY_IMAGE;
import static com.palpal.dealightbe.global.error.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.palpal.dealightbe.global.error.ErrorCode.INVALID_IMAGE_FORMAT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.exception.ImageIOException;
import com.palpal.dealightbe.domain.image.exception.InvalidFileTypeException;
import com.palpal.dealightbe.global.error.exception.InvalidValueException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

	private final AmazonS3 amazonS3;
	private static final String[] supportedImageExtension = {"jpg", "jpeg", "png"};

	@Value("${cloud.aws.s3.url}")
	private String url;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Override
	public String store(MultipartFile request) {
		log.info("요청으로 받아온 이미지({})를 S3에 저장합니다...", request.getOriginalFilename());
		validateFile(request);
		File file = convertToFile(request);
		String path = store(file);
		log.info("요청한 이미지를 S3에 저장하는 과정을 마칩니다.");

		return path;
	}

	@Override
	public void delete(String request) {
		String fileName = parseFileName(request);

		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, fileName);

		try {
			amazonS3.deleteObject(deleteObjectRequest);
		} catch (SdkClientException exception) {
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}
	}

	private String parseFileName(String path) {
		return path.replaceAll(url, "");
	}

	private String store(File uploadFile) {
		log.info("File을 S3에 저장합니다...");
		String uploadImageUrl = putS3(uploadFile, getFileName());

		removeTemporaryFile(uploadFile);

		log.info("File을 S3(Url: {})에 성공적으로 저장했습니다.", uploadImageUrl);
		return uploadImageUrl;
	}

	private String putS3(File uploadFile, String fileName) {

		try {
			log.info("file({})을 S3에 업로드 합니다...", fileName);
			amazonS3.putObject(
				new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (SdkClientException e) {
			log.warn("Bucket({})에 file({})을 업로드하는 것을 실패했습니다.", bucket, fileName);
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}

		log.info("Bucket({})에 file({})을 업로드하는데 성공했습니다.", bucket, fileName);
		String uploadedImageUrl = amazonS3.getUrl(bucket, fileName).toString();
		log.info("이미지의 경로: {}", uploadedImageUrl);

		return uploadedImageUrl;
	}

	private void removeTemporaryFile(File targetFile) {
		log.info("메모리에 저장된 이미지 파일을 삭제합니다...");
		boolean result = targetFile.delete();
		log.info("삭제 여부: {}", result);

		if (!result) {
			log.info("메모리에 저장된 이미지 파일을 삭제하는데 실패했습니다.");
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}
	}

	private File convertToFile(MultipartFile file) {
		log.info("MultipartFile을 File 타입으로 변경합니다...");
		try {
			File convertFile = new File(file.getOriginalFilename());

			if (convertFile.createNewFile()) {
				log.info("File 데이터를 생성합니다...");
				try (FileOutputStream fileOutputStream = new FileOutputStream(convertFile)) {
					log.info("FileOutputStream으로 file 데이터를 작성합니다...");
					fileOutputStream.write(file.getBytes());
				}
			}
			log.info("File생성에 성공했습니다.");
			return convertFile;
		} catch (IOException ioException) {
			log.warn("File작성에 실패했습니다.");
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}
	}

	private String getExtension(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

		return extension;
	}

	private String getFileName() {
		StringBuilder fileName = new StringBuilder();

		LocalDateTime now = LocalDateTime.now();
		fileName.append(now.format(DateTimeFormatter.ofPattern("yy/MM/dd/")));

		fileName.append(UUID.randomUUID());

		return fileName.toString();
	}

	private void validateFile(MultipartFile file) {
		log.info("이미지의 유효성 검증을 시작합니다...");
		if (file.isEmpty()) {
			log.warn("EMPTY_IMAGE : {}", file.getOriginalFilename());
			throw new InvalidValueException(EMPTY_IMAGE);
		}

		String inputExtension = getExtension(file);
		log.info("파일의 확장자 : {}", inputExtension);
		boolean isExtensionValid = Arrays.stream(supportedImageExtension)
			.anyMatch(extension -> extension.equalsIgnoreCase(inputExtension));
		log.info("확장자의 유효성 여부 : {}", isExtensionValid);
		if (!isExtensionValid) {
			log.warn("INVALID_IMAGE_FORMAT : {}", file.getOriginalFilename());
			throw new InvalidFileTypeException(INVALID_IMAGE_FORMAT);
		}
		log.info("이미지의 유효성 검증을 완료했습니다.");
	}
}
