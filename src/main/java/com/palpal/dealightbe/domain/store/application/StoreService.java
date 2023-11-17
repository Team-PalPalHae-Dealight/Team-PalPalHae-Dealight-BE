package com.palpal.dealightbe.domain.store.application;

import java.util.Collections;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreStatusReq;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreUpdateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreByMemberRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreStatusRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoresInfoSliceRes;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.ListSortType;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {

	public static final String DEFAULT_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/free-store-icon.png";

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final AddressService addressService;
	private final ImageService imageService;

	public StoreCreateRes register(Long providerId, StoreCreateReq req) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Address address = addressService.register(req.addressName(), req.xCoordinate(), req.yCoordinate());

		Store store = StoreCreateReq.toStore(req, address, member);

		storeRepository.save(store);

		return StoreCreateRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoreInfoRes getInfo(Long providerId, Long storeId) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		return StoreInfoRes.from(store);
	}

	public StoreInfoRes updateInfo(Long providerId, Long storeId, StoreUpdateReq request) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		Store updateStore = StoreUpdateReq.toStore(request);
		store.updateInfo(updateStore);

		return StoreInfoRes.from(store);
	}

	public StoreStatusRes updateStatus(Long providerId, Long storeId, StoreStatusReq storeStatus) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		store.updateStatus(storeStatus.storeStatus());

		return StoreStatusRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoreStatusRes getStatus(Long providerId, Long storeId) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		return StoreStatusRes.from(store);
	}

	public ImageRes uploadImage(Long providerId, Long storeId, ImageUploadReq request) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		String imageUrl = imageService.store(request.file());

		store.updateImage(imageUrl);

		return ImageRes.from(store);
	}

	public ImageRes updateImage(Long providerId, Long storeId, ImageUploadReq req) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		String image = store.getImage();
		imageService.delete(image);

		String updateImage = imageService.store(req.file());
		store.updateImage(updateImage);

		return ImageRes.from(store);
	}

	public void deleteImage(Long providerId, Long storeId) {
		Store store = validateMemberAndStoreOwner(providerId, storeId);

		String image = store.getImage();
		if (Objects.equals(image, DEFAULT_PATH)) {
			log.warn("DELETE:DELETE:DEFAULT_IMAGE_ALREADY_SET : {}", storeId);
			throw new BusinessException(ErrorCode.DEFAULT_IMAGE_ALREADY_SET);
		}

		imageService.delete(image);
		store.updateImage(DEFAULT_PATH);
	}

	@Transactional(readOnly = true)
	public StoreByMemberRes findByProviderId(Long providerId) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findByMemberProviderId(member.getProviderId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_PROVIDER_ID : {}", member.getProviderId());
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		return StoreByMemberRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoresInfoSliceRes search(double xCoordinate, double yCoordinate, String keyword, String sortBy, Pageable pageable) {
		Slice<Store> stores = new SliceImpl<>(Collections.emptyList(), pageable, false);

		ListSortType sortType = ListSortType.findSortType(sortBy);

		switch (sortType) {
			case DISTANCE:
				stores = storeRepository.findByDistanceWithin3Km(xCoordinate, yCoordinate, keyword, pageable);
				break;
			case DISCOUNT_RATE:
				stores = storeRepository.findByDiscountRate(xCoordinate, yCoordinate, keyword, pageable);
				break;
			case DEADLINE:
				stores = storeRepository.findByDeadLine(xCoordinate, yCoordinate, keyword, pageable);
				break;
		}

		return StoresInfoSliceRes.from(stores);
	}

	private Store validateMemberAndStoreOwner(Long providerId, Long storeId) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", storeId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		store.isSameOwnerAndTheRequester(member, store);

		return store;
	}
}
