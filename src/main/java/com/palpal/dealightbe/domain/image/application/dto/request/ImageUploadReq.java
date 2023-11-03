package com.palpal.dealightbe.domain.image.application.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadReq(
	MultipartFile file
) {
}
