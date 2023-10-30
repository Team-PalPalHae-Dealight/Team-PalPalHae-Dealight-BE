package com.palpal.dealightbe.domain.store.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreInfoRes;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
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

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final AddressService addressService;

	public StoreCreateRes register(Long memberId, StoreCreateReq req) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		addressService.register(req.addressName(), req.xCoordinate(), req.yCoordinate());

		Store store = StoreCreateReq.toStore(req);
		store.updateMember(member);
		storeRepository.save(store);

		return StoreCreateRes.from(store);
	}

	@Transactional(readOnly = true)
	public StoreInfoRes getInfo(Long memberId, Long storeId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_STORE_BY_ID : {}", storeId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_STORE);
			});

		isSameOwnerAndTheRequester(member, store);

		return StoreInfoRes.from(store);
	}

	private void isSameOwnerAndTheRequester(Member member, Store store) {
		Long ownerId = store.getMember().getProviderId();
		Long requesterId = member.getProviderId();

		if (!(ownerId == requesterId)) {
			log.warn("GET:READ:NOT_MATCH_OWNER_AND_REQUESTER : ownerId => {} memberId => {}", ownerId, requesterId);
			throw new BusinessException(ErrorCode.NOT_MATCH_OWNER_AND_REQUESTER);
		}
	}
}
