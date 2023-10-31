package com.palpal.dealightbe.domain.image;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

	String store(MultipartFile request);

	void delete(String request);
}
