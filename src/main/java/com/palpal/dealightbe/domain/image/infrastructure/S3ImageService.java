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
	private static final String supportedImageExtension[] = {"jpg", "jpeg", "png"};

	@Value("${cloud.aws.s3.url}")
	private String url;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Override
	public String store(MultipartFile request) {
		validateFile(request);
		File file = convertToFile(request);
		String path = store(file);
		return path;
	}

	@Override
	public void delete(String request) {
		String fileName = parseFileName(request);

		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, fileName);

		try {
			amazonS3.deleteObject(deleteObjectRequest);
		} catch (SdkClientException exception) {
			log.warn("INTERNAL_SERVER_ERROR : fileName => {}", fileName);
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}
	}

	private String parseFileName(String path) {
		return path.replaceAll(url, "");
	}

	private String store(File uploadFile) {
		String uploadImageUrl = putS3(uploadFile, getFileName());

		removeTemporaryFile(uploadFile);

		return uploadImageUrl;
	}

	private String putS3(File uploadFile, String fileName) {
		try {
			amazonS3.putObject(
				new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (SdkClientException e) {
			log.warn("INTERNAL_SERVER_ERROR : fileName => {}", fileName);
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}

		return amazonS3.getUrl(bucket, fileName).toString();
	}

	private void removeTemporaryFile(File targetFile) {
		boolean result = targetFile.delete();

		if (!result) {
			log.warn("INTERNAL_SERVER_ERROR : targetFile => {}", targetFile.getName());
			throw new ImageIOException(INTERNAL_SERVER_ERROR);
		}
	}

	private File convertToFile(MultipartFile file) {
		try {
			File convertFile = new File(file.getOriginalFilename());

			if (convertFile.createNewFile()) {
				try (FileOutputStream fileOutputStream = new FileOutputStream(convertFile)) {
					fileOutputStream.write(file.getBytes());
				}
			}
			return convertFile;
		} catch (IOException ioException) {
			log.warn("INTERNAL_SERVER_ERROR : file => {}", file.getOriginalFilename());
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
		if (file.isEmpty()) {
			log.warn("EMPTY_IMAGE : file => {}", file.getOriginalFilename());
			throw new InvalidValueException(EMPTY_IMAGE);
		}

		String inputExtension = getExtension(file);
		boolean isExtensionValid = Arrays.stream(supportedImageExtension)
			.anyMatch(extension -> extension.equalsIgnoreCase(inputExtension));

		if (!isExtensionValid) {
			log.warn("INVALID_IMAGE_FORMAT : file => {}", file.getOriginalFilename());
			throw new InvalidFileTypeException(INVALID_IMAGE_FORMAT);
		}
	}
}
