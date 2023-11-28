package com.palpal.dealightbe.domain.store.application;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.image.ImageService;
import com.palpal.dealightbe.domain.image.application.dto.request.ImageUploadReq;
import com.palpal.dealightbe.domain.image.application.dto.response.ImageRes;
import com.palpal.dealightbe.domain.item.domain.ItemRepository;
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
import com.palpal.dealightbe.domain.store.domain.StoreDocument;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.domain.store.domain.StoreStatus;
import com.palpal.dealightbe.domain.store.domain.UpdatedStore;
import com.palpal.dealightbe.domain.store.domain.UpdatedStoreRepository;
import com.palpal.dealightbe.domain.store.infrastructure.StoreSearchRepositoryImpl;
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
	private final UpdatedStoreRepository updatedStoreRepository;
	private final StoreSearchRepositoryImpl storeSearchRepositoryImpl;
	private final MemberRepository memberRepository;
	private final ItemRepository itemRepository;
	private final AddressService addressService;
	private final ImageService imageService;

	public StoreCreateRes register(Long providerId, StoreCreateReq req) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		checkDuplicatedStore(providerId);

		Address address = addressService.register(req.addressName(), req.xCoordinate(), req.yCoordinate());

		Store store = StoreCreateReq.toStore(req, address, member);
		storeRepository.save(store);

		return StoreCreateRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoreInfoRes getInfo(Long providerId) {
		Store store = getStoreByProviderId(providerId);

		return StoreInfoRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoreInfoRes getDetails(Long storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", storeId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		return StoreInfoRes.from(store);
	}

	public StoreInfoRes updateInfo(Long providerId, Long storeId, StoreUpdateReq request) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

		Store updateStore = StoreUpdateReq.toStore(request);
		store.updateInfo(updateStore);

		return StoreInfoRes.from(store);
	}

	public StoreStatusRes updateStatus(Long providerId, Long storeId, StoreStatusReq storeStatus) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

		StoreStatus updateStatus = StoreStatus.fromString(storeStatus.storeStatus().toString());
		store.updateStatus(updateStatus);

		statusCheckAndManageUpdatedStore(store);

		deleteClosedStoreItems(store);

		return StoreStatusRes.from(store);
	}


	@Transactional(readOnly = true)
	public StoreStatusRes getStatus(Long providerId, Long storeId) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

		return StoreStatusRes.from(store);
	}

	public ImageRes uploadImage(Long providerId, Long storeId, ImageUploadReq request) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

		String imageUrl = imageService.store(request.file());

		store.updateImage(imageUrl);

		return ImageRes.from(store);
	}

	public ImageRes updateImage(Long providerId, Long storeId, ImageUploadReq req) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

		String image = store.getImage();
		imageService.delete(image);

		String updateImage = imageService.store(req.file());
		store.updateImage(updateImage);

		return ImageRes.from(store);
	}

	public void deleteImage(Long providerId, Long storeId) {
		Store store = validateMemberAndStoreOwnerByProviderIdAndStoreId(providerId, storeId);

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
		Store store = getStoreByProviderId(providerId);

		return StoreByMemberRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoresInfoSliceRes search(double xCoordinate, double yCoordinate, String keyword, String sortBy, Long cursor, Pageable pageable) {
		Slice<Store> stores = storeRepository.findByKeywordAndDistanceWithin3KmAndSortCondition(xCoordinate, yCoordinate, keyword, sortBy, cursor, pageable);

		return StoresInfoSliceRes.from(stores);
	}

	@Transactional(readOnly = true)
	public StoresInfoSliceRes searchToES(double xCoordinate, double yCoordinate, String keyword, Pageable pageable) {
		Slice<StoreDocument> storeDocuments = storeSearchRepositoryImpl.searchStores(xCoordinate, yCoordinate, keyword, pageable);

		return StoresInfoSliceRes.fromDocuments(storeDocuments);
	}

	public void uploadToES() {
		List<UpdatedStore> updatedStoreList = updatedStoreRepository.findAll();

		if (updatedStoreList.isEmpty()) {
			throw new BusinessException(ErrorCode.UPDATABLE_STORE_NOT_EXIST);
		}

		List<StoreDocument> storeDocuments = updatedStoreList.stream()
			.map(updatedStore -> {
				Store store = storeRepository.findById(updatedStore.getId())
					.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_STORE));
				return StoreDocument.from(updatedStore, store);
			})
			.toList();

		storeSearchRepositoryImpl.bulkInsertOrUpdate(storeDocuments);
		updatedStoreRepository.deleteAll();
	}

	private void statusCheckAndManageUpdatedStore(Store store) {
		if (store.getStoreStatus() == StoreStatus.OPENED) {
			UpdatedStore updatedStore = UpdatedStore.from(store);
			updatedStoreRepository.save(updatedStore);
		}
		if (store.getStoreStatus() == StoreStatus.CLOSED) {
			UpdatedStore updatedStore = UpdatedStore.from(store);
			updatedStoreRepository.delete(updatedStore);
		}
	}

	private Store getStoreByProviderId(Long providerId) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findByMemberProviderId(member.getProviderId())
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_PROVIDER_ID : {}", member.getProviderId());
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		store.isSameOwnerAndTheRequester(member, store);

		return store;
	}

	private Store validateMemberAndStoreOwnerByProviderIdAndStoreId(Long providerId, Long storeId) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", storeId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		store.isSameOwnerAndTheRequester(member, store);

		return store;
	}

	private void deleteClosedStoreItems(Store store) {
		if (store.getStoreStatus() == StoreStatus.CLOSED) {
			itemRepository.deleteAllByStoreId(store.getId());
		}
	}

	private void checkDuplicatedStore(Long providerId) {
		storeRepository.findByMemberProviderId(providerId)
			.ifPresent(existingStore -> {
				log.warn("GET:READ:DUPLICATED_STORE: {}", providerId);
				throw new BusinessException(ErrorCode.ALREADY_HAS_STORE);
			});
	}
}
